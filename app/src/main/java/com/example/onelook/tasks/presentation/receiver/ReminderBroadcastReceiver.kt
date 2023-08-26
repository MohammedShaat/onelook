package com.example.onelook.tasks.presentation.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.onelook.tasks.presentation.worker.ReminderWorker
import timber.log.Timber

class ReminderBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        Timber.i("onReceive")

        val type = intent.getStringExtra("type") ?: return
        val id = intent.getStringExtra("id") ?: return
        val reminder = intent.getStringExtra("reminder") ?: return
        val supposedTimeMillis =
            intent.getLongExtra("supposedTimeMillis", 0).takeIf { it > 0 } ?: return

        val inputData = Data.Builder().apply {
            putString("type", type)
            putString("id", id)
            putString("reminder", reminder)
            putLong("supposedTimeMillis", supposedTimeMillis)
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