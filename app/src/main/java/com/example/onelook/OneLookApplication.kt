package com.example.onelook

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.onelook.di.ApplicationCoroutine
import com.example.onelook.util.getInitialDelay
import com.example.onelook.workers.DailyTasksWorker
import com.facebook.stetho.Stetho
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class OneLookApplication : Application(), Configuration.Provider {

    @Inject
    @ApplicationCoroutine
    lateinit var coroutineScope: CoroutineScope

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        FirebaseApp.initializeApp(this)
        Stetho.initializeWithDefaults(this)
        delayInit()
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    private fun delayInit() = coroutineScope.launch {
        scheduleDailyTasksWorker()
    }

    private fun scheduleDailyTasksWorker() {
        val request = PeriodicWorkRequestBuilder<DailyTasksWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(getInitialDelay(0, 0, true), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            DailyTasksWorker::class.java.name,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}