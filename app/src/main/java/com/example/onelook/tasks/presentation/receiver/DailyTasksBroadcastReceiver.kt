package com.example.onelook.tasks.presentation.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.onelook.common.data.repository.AppStateRepositoryImpl
import com.example.onelook.common.di.ApplicationCoroutine
import com.example.onelook.tasks.presentation.worker.DailyTasksWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class DailyTasksBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var appStateRepositoryImpl: AppStateRepositoryImpl

    @Inject
    @ApplicationCoroutine
    lateinit var scope: CoroutineScope

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        Timber.i("onReceive")
        scope.launch {
            appStateRepositoryImpl.setDailyTasksReceiverAlarmExists(false)
            val workRequest = OneTimeWorkRequest.Builder(DailyTasksWorker::class.java).build()
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}