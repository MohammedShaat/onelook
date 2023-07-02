package com.example.onelook.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.onelook.R
import com.example.onelook.data.Repository
import com.example.onelook.data.domain.DomainActivity
import com.example.onelook.data.domain.Supplement
import com.example.onelook.util.AlarmManagerHelper
import com.example.onelook.util.REMINDERS_CHANNEL_ID
import com.example.onelook.util.REMINDER_TIME_ADDITION
import com.example.onelook.util.getNotificationManager
import com.example.onelook.util.sendNotification
import com.example.onelook.util.toLocalModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted params: WorkerParameters,
    private val repository: Repository,
    private val alarmManagerHelper: AlarmManagerHelper
) :
    CoroutineWorker(context, params) {

    private lateinit var type: String
    private lateinit var id: String
    private lateinit var reminder: String
    private lateinit var supplement: Supplement
    private lateinit var activity: DomainActivity

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            Timber.i("doWork()")

            type = inputData.getString("type") ?: return@withContext Result.failure()
            id = inputData.getString("id") ?: return@withContext Result.failure()
            reminder = inputData.getString("reminder") ?: return@withContext Result.failure()

            if (type == "supplement")
                supplement = repository.getSupplements()
                    .first().data?.firstOrNull { it.id == UUID.fromString(id) }
                    ?: return@withContext Result.failure()
            else
                activity = repository.getActivities()
                    .first().data?.firstOrNull { it.id == UUID.fromString(id) }
                    ?: return@withContext Result.failure()


            if (sendNotification() == Result.failure())
                return@withContext Result.failure()

            if (setNextAlarm() == Result.failure())
                return@withContext Result.failure()

            return@withContext Result.success()
        }
    }

    private suspend fun sendNotification(): Result {
        val isCompleted =
            if (type == "supplement")
                repository.getLocalSupplementsHistory(supplement.id)
                    .first().data?.firstOrNull()?.completed ?: return Result.failure()
            else
                repository.getLocalActivitiesHistory(activity.id)
                    .first().data?.firstOrNull()?.completed ?: return Result.failure()
        if (isCompleted)
            return Result.success()

        val name =
            if (type == "supplement") supplement.name
            else activity.type
        val message = when (reminder) {
            "before" -> context.getString(
                if (type == "supplement") R.string.time_remaining_for_supplement
                else R.string.time_remaining_for_activity,
                REMINDER_TIME_ADDITION, name
            )

            "after" -> context.getString(
                if (type == "supplement") R.string.dont_forget_take_supplement
                else R.string.dont_forget_exercise_activity,
                name
            )

            else -> ""
        }
        getNotificationManager(context)?.sendNotification(
            context,
            UUID.fromString(id).hashCode(),
            message,
            REMINDERS_CHANNEL_ID,
        )

        return Result.success()
    }

    private suspend fun setNextAlarm(): Result {
        if (type == "supplement") {
            if (supplement.reminder == "both" && reminder == "before") return Result.success()
            else if (reminder == "before") delay(REMINDER_TIME_ADDITION * 60 * 1000L)

            alarmManagerHelper.setAlarm(supplement.toLocalModel())

        } else {
            if (activity.reminder == "both" && reminder == "before") return Result.success()
            else if (reminder == "before") delay(REMINDER_TIME_ADDITION * 60 * 1000L)

            alarmManagerHelper.setAlarm(activity.toLocalModel())
        }

        return Result.success()
    }
}