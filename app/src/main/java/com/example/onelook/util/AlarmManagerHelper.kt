package com.example.onelook.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.onelook.data.local.activities.LocalActivity
import com.example.onelook.data.local.supplements.LocalSupplement
import com.example.onelook.receivers.ReminderBroadcastReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmManagerHelper @Inject constructor(@ApplicationContext private val context: Context) {

    private val alarmManager =
        ContextCompat.getSystemService(context, AlarmManager::class.java) as AlarmManager

    fun setAlarm(localSupplement: LocalSupplement) {
        // Cancels previous alarms to set new ones or when reminder updated to null
        cancelAlarm(localSupplement)
        localSupplement.reminder ?: return

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra("type", "supplement")
            putExtra("id", localSupplement.id.toString())
        }
        val targetCalendar = getCalendar(localSupplement) ?: return
        // "Reminder before"
        if (localSupplement.reminder in listOf("before", "both")) {
            intent.putExtra("reminder", "before")
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                localSupplement.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val calendarBefore = (targetCalendar.clone() as Calendar).apply {
                add(Calendar.MINUTE, -REMINDER_TIME_ADDITION)
            }
//            val calendarBefore = Calendar.getInstance().apply {
//                add(Calendar.SECOND, 5)
//            }
            if (calendarBefore < Calendar.getInstance()) return
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendarBefore.timeInMillis,
                pendingIntent
            )
            Timber.i("set reminder before:: ${calendarBefore.time}")
        }

        // "Reminder after"
        if (localSupplement.reminder in listOf("after", "both")) {
            intent.putExtra("reminder", "after")
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                localSupplement.id.hashCode() + 1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val calendarAfter = (targetCalendar.clone() as Calendar).apply {
                add(Calendar.MINUTE, REMINDER_TIME_ADDITION)
            }
//            val calendarAfter = Calendar.getInstance().apply {
//                add(Calendar.SECOND, 10)
//            }
            if (calendarAfter < Calendar.getInstance()) return
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendarAfter.timeInMillis,
                pendingIntent
            )
            Timber.i("set reminder after:: ${calendarAfter.time}")
        }
    }

    fun cancelAlarm(localSupplement: LocalSupplement) {
        val reminderIntent = Intent(context, ReminderBroadcastReceiver::class.java)

        val pendingIntentBefore = PendingIntent.getBroadcast(
            context,
            localSupplement.id.hashCode(),
            reminderIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val pendingIntentAfter = PendingIntent.getBroadcast(
            context,
            localSupplement.id.hashCode() + 1,
            reminderIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntentBefore)
        alarmManager.cancel(pendingIntentAfter)
    }

    fun setAlarm(localActivity: LocalActivity) {
        // Cancels previous alarms to set new ones or when reminder updated to null
        cancelAlarm(localActivity)
        localActivity.reminder ?: return

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra("type", "activity")
            putExtra("id", localActivity.id.toString())
        }
        val targetCalendar = getCalendar(localActivity) ?: return
        // "Reminder before"
        if (localActivity.reminder in listOf("before", "both")) {
            intent.putExtra("reminder", "before")
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                localActivity.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val calendarBefore = (targetCalendar.clone() as Calendar).apply {
                add(Calendar.MINUTE, -REMINDER_TIME_ADDITION)
            }
//            val calendarBefore = Calendar.getInstance().apply {
//                add(Calendar.SECOND, 5)
//            }
            if (calendarBefore < Calendar.getInstance()) return
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendarBefore.timeInMillis,
                pendingIntent
            )
            Timber.i("set reminder before:: ${calendarBefore.time}")
        }

        // "Reminder after"
        if (localActivity.reminder in listOf("after", "both")) {
            intent.putExtra("reminder", "after")
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                localActivity.id.hashCode() + 1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val calendarAfter = (targetCalendar.clone() as Calendar).apply {
                add(Calendar.MINUTE, REMINDER_TIME_ADDITION)
            }
//            val calendarAfter = Calendar.getInstance().apply {
//                add(Calendar.SECOND, 10)
//            }
            if (calendarAfter < Calendar.getInstance()) return
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendarAfter.timeInMillis,
                pendingIntent
            )
            Timber.i("set reminder after:: ${calendarAfter.time}")
        }
    }

    fun cancelAlarm(localActivity: LocalActivity) {
        val reminderIntent = Intent(context, ReminderBroadcastReceiver::class.java)

        val pendingIntentBefore = PendingIntent.getBroadcast(
            context,
            localActivity.id.hashCode(),
            reminderIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val pendingIntentAfter = PendingIntent.getBroadcast(
            context,
            localActivity.id.hashCode() + 1,
            reminderIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntentBefore)
        alarmManager.cancel(pendingIntentAfter)
    }

    private fun getCalendar(localSupplement: LocalSupplement): Calendar? {
        val expirationCalendar = Calendar.getInstance().run {
            time = localSupplement.createdAt.parse
            val amount = Regex("""\d+""").find(localSupplement.duration ?: "")?.value?.toInt() ?: 0
            add(Calendar.DAY_OF_YEAR, amount)
            if (time == localSupplement.createdAt.parse) null
            else this
        }

        // When target time is today
        val targetTimeStr = when (val it = localSupplement.timeOfDay) {
            null -> getUpcomingTimeFromDosages(localSupplement.dosage)
            else -> {
                if (it.contains(":")) localSupplement.timeOfDay
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
        val freq = Regex("""\d+""").find(localSupplement.frequency)?.value?.toInt() ?: 1
        val daysUntilToday =
            ((Date().time - localSupplement.createdAt.parse.time) / (1000 * 60 * 60 * 24)).toInt()
        val addDays = freq - daysUntilToday % freq
        targetCalendar.add(Calendar.DAY_OF_YEAR, addDays)
        if (expirationCalendar == null || targetCalendar <= expirationCalendar)
            return targetCalendar

        return null
    }

    private fun getCalendar(localActivity: LocalActivity): Calendar {
        // When target time is today
        val targetTimeStr =
            if (localActivity.timeOfDay.contains(":")) localActivity.timeOfDay
            else localActivity.timeOfDay.dayPartTo24Format()
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
}