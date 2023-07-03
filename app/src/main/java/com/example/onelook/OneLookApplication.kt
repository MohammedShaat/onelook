package com.example.onelook

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.BackoffPolicy
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.onelook.di.ApplicationCoroutine
import com.example.onelook.util.getInitialDelay
import com.example.onelook.workers.DailyTasksWorker
import com.facebook.stetho.Stetho
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs

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
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val request = PeriodicWorkRequestBuilder<DailyTasksWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .setInitialDelay(getInitialDelay(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            DailyTasksWorker::class.java.name,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}