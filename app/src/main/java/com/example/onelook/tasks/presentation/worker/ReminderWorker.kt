package com.example.onelook.tasks.presentation.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.onelook.R
import com.example.onelook.common.data.repository.AppStateRepositoryImpl
import com.example.onelook.common.data.repository.UserPreferencesRepositoryImpl
import com.example.onelook.tasks.doamin.model.DomainActivity
import com.example.onelook.tasks.doamin.model.Supplement
import com.example.onelook.notifications.data.local.NotificationEntity
import com.example.onelook.notifications.doamin.repository.NotificationRepository
import com.example.onelook.tasks.doamin.repository.ActivityRepository
import com.example.onelook.tasks.doamin.repository.SupplementRepository
import com.example.onelook.common.presentation.MainActivity
import com.example.onelook.common.util.ACTION_OPEN_ACTIVITY_NOTIFICATION
import com.example.onelook.common.util.ACTION_OPEN_SUPPLEMENT_NOTIFICATION
import com.example.onelook.common.util.AlarmManagerHelper
import com.example.onelook.common.util.NOTIFICATION_TASK_REQ
import com.example.onelook.common.util.REMINDERS_CHANNEL_ID
import com.example.onelook.common.util.REMINDER_TIME_ADDITION
import com.example.onelook.common.util.format
import com.example.onelook.common.util.getNotificationManager
import com.example.onelook.common.util.sendNotification
import com.example.onelook.tasks.data.mapper.toActivityEntity
import com.example.onelook.tasks.data.mapper.toSupplementEntity
import com.example.onelook.tasks.doamin.repository.ActivityHistoryRepository
import com.example.onelook.tasks.doamin.repository.SupplementHistoryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Calendar
import java.util.UUID

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted params: WorkerParameters,
    private val activityRepository: ActivityRepository,
    private val supplementRepository: SupplementRepository,
    private val activityHistoryRepository: ActivityHistoryRepository,
    private val supplementHistoryRepository: SupplementHistoryRepository,
    private val notificationRepository: NotificationRepository,
    private val alarmManagerHelper: AlarmManagerHelper,
    private val appStateRepositoryImpl: AppStateRepositoryImpl,
    private val userPreferencesRepositoryImpl: UserPreferencesRepositoryImpl,
) :
    CoroutineWorker(context, params) {

    private lateinit var type: String
    private lateinit var id: String
    private lateinit var reminder: String
    private var supposedTimeMillis: Long = 0
    private var supplement: Supplement? = null
    private var activity: DomainActivity? = null

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            Timber.i("doWork() started")

            type = inputData.getString("type") ?: return@withContext Result.failure()
            id = inputData.getString("id") ?: return@withContext Result.failure()
            reminder = inputData.getString("reminder") ?: return@withContext Result.failure()
            supposedTimeMillis = inputData.getLong("supposedTimeMillis", 0).takeIf { it > 0 }
                ?: return@withContext Result.failure()
            supplement = supplementRepository.getSupplements().first().data
                ?.firstOrNull { it.id == UUID.fromString(id) }
            activity = activityRepository.getActivities().first().data
                ?.firstOrNull { it.id == UUID.fromString(id) }
            supplement ?: activity ?: return@withContext Result.failure()

            sendNotificationAndSaveIt()
            setNextAlarm()

            Timber.i("doWork() finished")
            return@withContext Result.success()
        }
    }

    private suspend fun sendNotificationAndSaveIt() {
        if (!areNotificationsEnabled() || isNotificationLate()) return

        val supplementHistory = supplement?.let {
            supplementHistoryRepository.getSupplementsHistory(it).first().data?.firstOrNull()
        }
        val activityHistory = activity?.let {
            activityHistoryRepository.getActivitiesHistory(it).first().data?.firstOrNull()
        }
        // Not send or save notification if task completed
        if (supplementHistory?.completed ?: activityHistory!!.completed)
            return

        Timber.i("sendNotification() ${supplement?.let { "supplement" } ?: "activity"}")
        val message =
            if (reminder == "before") context.getString(
                supplement?.let { R.string.time_remaining_for_supplement }
                    ?: R.string.time_remaining_for_activity,
                supplement?.name ?: activity!!.type
            )
            else context.getString(
                supplement?.let { R.string.dont_forget_take_supplement }
                    ?: R.string.dont_forget_exercise_activity,
                supplement?.name ?: activity!!.type
            )
        val taskIntent = Intent(context, MainActivity::class.java).apply {
            action = supplement?.let { ACTION_OPEN_SUPPLEMENT_NOTIFICATION }
                ?: ACTION_OPEN_ACTIVITY_NOTIFICATION
            putExtra("supplement_history", supplementHistory)
            putExtra("activity_history", activityHistory)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_TASK_REQ,
            taskIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        getNotificationManager(context)?.sendNotification(
            context,
            UUID.fromString(id).hashCode(),
            message,
            REMINDERS_CHANNEL_ID,
            pendingIntent
        )

        appStateRepositoryImpl.increaseUnreadNotifications()
        // Stores notification in db
        saveNotification(
            message,
            supplementHistory?.id ?: activityHistory!!.id,
            supplementHistory?.javaClass?.name ?: activityHistory!!.javaClass.name
        )
    }

    private suspend fun setNextAlarm() {
        supplement?.let {
            if (supplement!!.reminder == "both" && reminder == "before") return
            else if (reminder == "before") delay(REMINDER_TIME_ADDITION * 60 * 1000L)

            alarmManagerHelper.setAlarm(supplement!!.toSupplementEntity())

        } ?: let {
            if (activity!!.reminder == "both" && reminder == "before") return
            else if (reminder == "before") delay(REMINDER_TIME_ADDITION * 60 * 1000L)

            alarmManagerHelper.setAlarm(activity!!.toActivityEntity())
        }
    }

    private suspend fun saveNotification(message: String, historyId: UUID, historyType: String) {
        val creationCalendar = Calendar.getInstance().apply { timeInMillis = supposedTimeMillis }
        val notificationEntity = NotificationEntity(
            id = UUID.randomUUID(),
            message = message,
            historyId = historyId,
            historyType = historyType,
            createdAt = creationCalendar.time.format
        )
        notificationRepository.createNotification(notificationEntity).collect()
    }

    private suspend fun areNotificationsEnabled(): Boolean {
        return supplement?.let { userPreferencesRepositoryImpl.getSupplementsNotificationsState().first() }
            ?: userPreferencesRepositoryImpl.getActivitiesNotificationsState().first()
    }

    private fun isNotificationLate(): Boolean {
        val supposedCalendar = Calendar.getInstance().apply {
            timeInMillis = supposedTimeMillis
            set(Calendar.MINUTE, get(Calendar.MINUTE) + 1)
        }
        return Calendar.getInstance() > supposedCalendar
    }
}