package com.example.onelook.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.onelook.R
import com.example.onelook.util.REMINDERS_CHANNEL_ID
import com.example.onelook.util.getNotificationManager
import com.example.onelook.util.sendNotification
import com.example.onelook.workers.ReminderWorker
import timber.log.Timber
import java.util.UUID

class ReminderBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        Timber.i("onReceive")

        val type = intent.getStringExtra("type") ?: return
        val id = intent.getStringExtra("id") ?: return
        val reminder = intent.getStringExtra("reminder") ?: return

        val inputData = Data.Builder().apply {
            putString("type", type)
            putString("id", id)
            putString("reminder", reminder)
        }.build()
        val workRequest = OneTimeWorkRequest.Builder(ReminderWorker::class.java)
            .setInputData(inputData)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            type,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}