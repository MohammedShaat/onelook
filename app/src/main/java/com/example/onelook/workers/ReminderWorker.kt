package com.example.onelook.workers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.onelook.R
import com.example.onelook.data.AppStateManager
import com.example.onelook.data.Repository
import com.example.onelook.data.domain.DomainActivity
import com.example.onelook.data.domain.Supplement
import com.example.onelook.data.local.notifications.LocalNotification
import com.example.onelook.ui.mainactivity.MainActivity
import com.example.onelook.util.ACTION_OPEN_ACTIVITY_NOTIFICATION
import com.example.onelook.util.ACTION_OPEN_SUPPLEMENT_NOTIFICATION
import com.example.onelook.util.ACTION_OPEN_TIMER
import com.example.onelook.util.AlarmManagerHelper
import com.example.onelook.util.DATE_TIME_FORMAT
import com.example.onelook.util.NOTIFICATION_TASK_REQ
import com.example.onelook.util.REMINDERS_CHANNEL_ID
import com.example.onelook.util.REMINDER_TIME_ADDITION
import com.example.onelook.util.getNotificationManager
import com.example.onelook.util.sendNotification
import com.example.onelook.util.toLocalModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted params: WorkerParameters,
    private val repository: Repository,
    private val alarmManagerHelper: AlarmManagerHelper,
    private val appStateManager: AppStateManager
) :
    CoroutineWorker(context, params) {

    private lateinit var type: String
    private lateinit var id: String
    private lateinit var reminder: String
    private var supplement: Supplement? = null
    private var activity: DomainActivity? = null

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            Timber.i("doWork() started")

            type = inputData.getString("type") ?: return@withContext Result.failure()
            id = inputData.getString("id") ?: return@withContext Result.failure()
            reminder = inputData.getString("reminder") ?: return@withContext Result.failure()
            supplement = repository.getSupplements().first().data
                ?.firstOrNull { it.id == UUID.fromString(id) }
            activity = repository.getActivities().first().data
                ?.firstOrNull { it.id == UUID.fromString(id) }

            sendNotification()
            setNextAlarm()

            Timber.i("doWork() finished")
            return@withContext Result.success()
        }
    }

    private suspend fun sendNotification() {
        val supplementHistory = supplement?.let {
            repository.getSupplementsHistory(it).first().data?.firstOrNull()
        }
        val activityHistory = activity?.let {
            repository.getActivitiesHistory(it).first().data?.firstOrNull()
        }
        // Not sending notification if task is completed
        if ((supplementHistory?.completed ?: activityHistory?.completed) == true)
            return

        Timber.i("sendNotification() ${supplement?.let { "supplement" } ?: "activity"}")
        val message =
            if (reminder == "before") context.getString(
                supplement?.let { R.string.time_remaining_for_supplement }
                    ?: R.string.time_remaining_for_activity,
                REMINDER_TIME_ADDITION,
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
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        getNotificationManager(context)?.sendNotification(
            context,
            UUID.fromString(id).hashCode(),
            message,
            REMINDERS_CHANNEL_ID,
            pendingIntent
        )

        appStateManager.increaseUnreadNotifications()


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

            alarmManagerHelper.setAlarm(supplement!!.toLocalModel())

        } ?: let {
            if (activity!!.reminder == "both" && reminder == "before") return
            else if (reminder == "before") delay(REMINDER_TIME_ADDITION * 60 * 1000L)

            alarmManagerHelper.setAlarm(activity!!.toLocalModel())
        }
    }

    private suspend fun saveNotification(message: String, historyId: UUID, historyType: String) {
        val localNotification = LocalNotification(
            id = UUID.randomUUID(),
            message = message,
            historyId = historyId,
            historyType = historyType,
            createdAt = SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault()).format(Date())
        )
        repository.createNotification(localNotification).collect()
    }
}