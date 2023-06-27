package com.example.onelook.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.onelook.R
import com.example.onelook.util.REMINDERS_CHANNEL_ID
import com.example.onelook.util.getNotificationManager
import com.example.onelook.util.sendNotification
import com.example.onelook.workers.ReminderWorker
import java.util.UUID

class ReminderBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val type = intent.getStringExtra("type") ?: return
        val name = intent.getStringExtra("name") ?: return
        val id = intent.getStringExtra("id") ?: return

        val message = context.getString(
            if (type == "supplement") R.string.time_to_take_supplement
            else R.string.time_to_exercise, name
        )
        getNotificationManager(context)?.sendNotification(
            context,
            UUID.fromString(id).hashCode(),
            message,
            REMINDERS_CHANNEL_ID,
        )

        val inputData = Data.Builder().apply {
            putString("type", type)
            putString("id", id)
        }.build()
        val workRequest = OneTimeWorkRequest.Builder(ReminderWorker::class.java)
            .setInputData(inputData)
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)


    }
}