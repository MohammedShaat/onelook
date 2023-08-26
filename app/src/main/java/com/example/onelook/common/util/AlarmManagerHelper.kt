package com.example.onelook.common.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.onelook.common.data.repository.AppStateRepositoryImpl
import com.example.onelook.tasks.data.local.ActivityEntity
import com.example.onelook.tasks.data.local.SupplementEntity
import com.example.onelook.tasks.presentation.receiver.DailyTasksBroadcastReceiver
import com.example.onelook.tasks.presentation.receiver.ReminderBroadcastReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmManagerHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appStateRepositoryImpl: AppStateRepositoryImpl,
) {

    private val alarmManager =
        ContextCompat.getSystemService(context, AlarmManager::class.java) as AlarmManager

    fun setAlarm(supplementEntity: SupplementEntity) {
        // Cancels previous alarms to set new ones or when reminder updated to null
        cancelAlarm(supplementEntity)
        supplementEntity.reminder ?: return

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra("type", "supplement")
            putExtra("id", supplementEntity.id.toString())
        }
        val targetCalendar = getCalendar(supplementEntity) ?: return
        // "Reminder before"
        if (supplementEntity.reminder in listOf("before", "both")) {
            val calendarBefore = (targetCalendar.clone() as Calendar).apply {
                add(Calendar.MINUTE, -REMINDER_TIME_ADDITION)
            }
//            val calendarBefore = Calendar.getInstance().apply {
//                add(Calendar.SECOND, 5)
//            }
            intent.putExtra("reminder", "before")
            intent.putExtra("supposedTimeMillis", calendarBefore.timeInMillis)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                supplementEntity.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            if (calendarBefore < Calendar.getInstance()) return
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendarBefore.timeInMillis,
                pendingIntent
            )
            Timber.i("set supplement reminder before:: ${calendarBefore.time}")
        }

        // "Reminder after"
        if (supplementEntity.reminder in listOf("after", "both")) {
            val calendarAfter = (targetCalendar.clone() as Calendar).apply {
                add(Calendar.MINUTE, REMINDER_TIME_ADDITION)
            }
//            val calendarAfter = Calendar.getInstance().apply {
//                add(Calendar.SECOND, 10)
//            }
            intent.putExtra("reminder", "after")
            intent.putExtra("supposedTimeMillis", calendarAfter.timeInMillis)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                supplementEntity.id.hashCode() + 1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            if (calendarAfter < Calendar.getInstance()) return
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendarAfter.timeInMillis,
                pendingIntent
            )
            Timber.i("set supplement reminder after:: ${calendarAfter.time}")
        }
    }

    fun cancelAlarm(supplementEntity: SupplementEntity) {
        val reminderIntent = Intent(context, ReminderBroadcastReceiver::class.java)

        val pendingIntentBefore = PendingIntent.getBroadcast(
            context,
            supplementEntity.id.hashCode(),
            reminderIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val pendingIntentAfter = PendingIntent.getBroadcast(
            context,
            supplementEntity.id.hashCode() + 1,
            reminderIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntentBefore)
        alarmManager.cancel(pendingIntentAfter)
    }

    fun setAlarm(activityEntity: ActivityEntity) {
        // Cancels previous alarms to set new ones or when reminder updated to null
        cancelAlarm(activityEntity)
        activityEntity.reminder ?: return

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra("type", "activity")
            putExtra("id", activityEntity.id.toString())
        }
        val targetCalendar = getCalendar(activityEntity) ?: return
        // "Reminder before"
        if (activityEntity.reminder in listOf("before", "both")) {
            val calendarBefore = (targetCalendar.clone() as Calendar).apply {
                add(Calendar.MINUTE, -REMINDER_TIME_ADDITION)
            }
//            val calendarBefore = Calendar.getInstance().apply {
//                add(Calendar.SECOND, 5)
//            }
            intent.putExtra("reminder", "before")
            intent.putExtra("supposedTimeMillis", calendarBefore.timeInMillis)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                activityEntity.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            if (calendarBefore < Calendar.getInstance()) return
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendarBefore.timeInMillis,
                pendingIntent
            )
            Timber.i("set activity reminder before:: ${calendarBefore.time}")
        }

        // "Reminder after"
        if (activityEntity.reminder in listOf("after", "both")) {
            val calendarAfter = (targetCalendar.clone() as Calendar).apply {
                add(Calendar.MINUTE, REMINDER_TIME_ADDITION)
            }
//            val calendarAfter = Calendar.getInstance().apply {
//                add(Calendar.SECOND, 10)
//            }
            intent.putExtra("reminder", "after")
            intent.putExtra("supposedTimeMillis", calendarAfter.timeInMillis)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                activityEntity.id.hashCode() + 1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            if (calendarAfter < Calendar.getInstance()) return
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendarAfter.timeInMillis,
                pendingIntent
            )
            Timber.i("set activity reminder after:: ${calendarAfter.time}")
        }
    }

    fun cancelAlarm(activityEntity: ActivityEntity) {
        val reminderIntent = Intent(context, ReminderBroadcastReceiver::class.java)

        val pendingIntentBefore = PendingIntent.getBroadcast(
            context,
            activityEntity.id.hashCode(),
            reminderIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val pendingIntentAfter = PendingIntent.getBroadcast(
            context,
            activityEntity.id.hashCode() + 1,
            reminderIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntentBefore)
        alarmManager.cancel(pendingIntentAfter)
    }

    private fun getCalendar(supplementEntity: SupplementEntity): Calendar? {
        val expirationCalendar =
            getExpirationCalendar(supplementEntity.createdAt.parseDate, supplementEntity.duration)

        // When target time is today
        val targetTimeStr = when (val it = supplementEntity.timeOfDay) {
            null -> getUpcomingTimeFromDosages(supplementEntity.dosage)
            else -> {
                if (it.contains(":")) supplementEntity.timeOfDay
                else it.dayPartTo24Format()
            }
        }
        val targetCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetTimeStr.getHours())
            set(Calendar.MINUTE, targetTimeStr.getMinutes())
            set(Calendar.SECOND, 0)
        }
        if (targetCalendar > Calendar.getInstance() &&
            (expirationCalendar == null || targetCalendar <= expirationCalendar)
        )
            return targetCalendar

        // When target time is in next days
        val freq = Regex("""\d+""").find(supplementEntity.frequency)?.value?.toInt() ?: 1
        val daysUntilToday =
            ((Date().time - supplementEntity.createdAt.parseDate.time) / (1000 * 60 * 60 * 24)).toInt()
        val addDays = freq - daysUntilToday % freq
        targetCalendar.add(Calendar.DAY_OF_YEAR, addDays)
        if (expirationCalendar == null || targetCalendar <= expirationCalendar)
            return targetCalendar

        return null
    }

    private fun getCalendar(activityEntity: ActivityEntity): Calendar {
        // When target time is today
        val targetTimeStr =
            if (activityEntity.timeOfDay.contains(":")) activityEntity.timeOfDay
            else activityEntity.timeOfDay.dayPartTo24Format()
        val targetCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetTimeStr.getHours())
            set(Calendar.MINUTE, targetTimeStr.getMinutes())
            set(Calendar.SECOND, 0)
        }
        if (targetCalendar > Calendar.getInstance())
            return targetCalendar

        // When target time is tomorrow
        targetCalendar.add(Calendar.DAY_OF_YEAR, 1)
        return targetCalendar
    }

    suspend fun setAlarmForDailyTasksReceiver(
        delayMilliSeconds: Int = 0,
        delaySeconds: Int = 0,
        delayMinutes: Int = 0,
        delayHours: Int = 0
    ) {
        if (appStateRepositoryImpl.getDailyTasksReceiverAlarmExists())
            return

        appStateRepositoryImpl.setDailyTasksReceiverAlarmExists(true)

        val calendar = Calendar.getInstance().apply {
            add(Calendar.MILLISECOND, delayMilliSeconds)
            add(Calendar.SECOND, delaySeconds)
            add(Calendar.MINUTE, delayMinutes)
            add(Calendar.HOUR_OF_DAY, delayHours)
        }
        val intent = Intent(context, DailyTasksBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DAILY_TASKS_RECEIVER_PENDING_INTENT_REQ,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
}