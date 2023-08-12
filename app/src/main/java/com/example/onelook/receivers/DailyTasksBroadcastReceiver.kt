package com.example.onelook.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.onelook.data.AppStateManager
import com.example.onelook.di.ApplicationCoroutine
import com.example.onelook.workers.DailyTasksWorker
import com.example.onelook.workers.ReminderWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class DailyTasksBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var appStateManager: AppStateManager

    @Inject
    @ApplicationCoroutine
    lateinit var scope: CoroutineScope

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        Timber.i("onReceive")
        scope.launch {
            appStateManager.setDailyTasksReceiverAlarmExists(false)
            val workRequest = OneTimeWorkRequest.Builder(DailyTasksWorker::class.java).build()
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}