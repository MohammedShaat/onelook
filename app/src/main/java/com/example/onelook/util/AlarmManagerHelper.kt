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
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmManagerHelper @Inject constructor(@ApplicationContext private val context: Context) {

    private val alarmManager =
        ContextCompat.getSystemService(context, AlarmManager::class.java) as AlarmManager

    fun setAlarmForSupplement(localSupplement: LocalSupplement, next: Boolean = false) {
        localSupplement.reminder ?: return
        val reminderIntent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra("type", "supplement")
            putExtra("name", localSupplement.name)
            putExtra("id", localSupplement.id.toString())
        }
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
        val time = when (val it = localSupplement.timeOfDay) {
            null -> getTimeFromDosagesNumber(0, localSupplement.dosage)
            else -> {
                if (it.contains(":")) localSupplement.timeOfDay
                else it.dayPartTo24Format()
            }
        }

        // "Reminder before"
        if (localSupplement.reminder in listOf("before", "both")) {
            val calendar = Calendar.getInstance().apply {
//                set(Calendar.HOUR_OF_DAY, time.getHours())
//                set(Calendar.MINUTE, time.getMinutes() + REMINDER_TIME_ADDITION)
//                set(Calendar.SECOND, 0)
                set(Calendar.SECOND, get(Calendar.SECOND) + 10)
            }
            if (timeNowDate() > calendar.time) return
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntentBefore
            )
            // Cancels "reminder after" if not set when supplement updated
            if (localSupplement.reminder != "both")
                alarmManager.cancel(pendingIntentAfter)
        }

        // "Reminder after"
        if (localSupplement.reminder in listOf("after", "both")) {
            val calendar = Calendar.getInstance().apply {
//                set(Calendar.HOUR_OF_DAY, time.getHours())
//                set(Calendar.MINUTE, time.getMinutes() + REMINDER_TIME_ADDITION)
//                set(Calendar.SECOND, 0)
                set(Calendar.SECOND, get(Calendar.SECOND) + 15)
            }
            if (timeNowDate() > calendar.time) return
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntentAfter
            )
            // Cancels "reminder before" if not set when supplement updated
            if (localSupplement.reminder != "both")
                alarmManager.cancel(pendingIntentBefore)
        }
    }//setAlarmForSupplementHistory

    fun cancelAlarmOfSupplement(localSupplement: LocalSupplement) {
        localSupplement.reminder ?: return
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
    }//cancelAlarmOfSupplement

    fun setAlarmForActivity(localActivity: LocalActivity, next: Boolean = false) {
        localActivity.reminder ?: return
        val reminderIntent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra("type", "supplement")
            putExtra("name", localActivity.type)
            putExtra("id", localActivity.id.toString())
        }
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
        val time =
            if (localActivity.timeOfDay.contains(":"))
                localActivity.timeOfDay
            else
                localActivity.timeOfDay.dayPartTo24Format()

        // "Reminder before"
        if (localActivity.reminder in listOf("before", "both")) {
            val calendar = Calendar.getInstance().apply {
//                set(Calendar.HOUR_OF_DAY, time.getHours())
//                set(Calendar.MINUTE, time.getMinutes() + REMINDER_TIME_ADDITION)
//                set(Calendar.SECOND, 0)
                set(Calendar.SECOND, get(Calendar.SECOND) + 10)
            }
            if (timeNowDate() > calendar.time) return
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntentBefore
            )
            // Cancels "reminder after" if not set when supplement updated
            if (localActivity.reminder != "both")
                alarmManager.cancel(pendingIntentAfter)
        }

        // "Reminder after"
        if (localActivity.reminder in listOf("after", "both")) {
            val calendar = Calendar.getInstance().apply {
//                set(Calendar.HOUR_OF_DAY, time.getHours())
//                set(Calendar.MINUTE, time.getMinutes() + REMINDER_TIME_ADDITION)
//                set(Calendar.SECOND, 0)
                set(Calendar.SECOND, get(Calendar.SECOND) + 15)
            }
            if (timeNowDate() > calendar.time) return
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntentAfter
            )
            // Cancels "reminder before" if not set when supplement updated
            if (localActivity.reminder != "both")
                alarmManager.cancel(pendingIntentBefore)
        }
    }//setAlarmForActivity

    fun cancelAlarmOfActivity(localActivity: LocalActivity) {
        localActivity.reminder ?: return
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
    }//cancelAlarmOfActivity

}