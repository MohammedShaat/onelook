package com.example.onelook

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.onelook.data.AppStateManager
import com.example.onelook.di.ApplicationCoroutine
import com.example.onelook.util.AlarmManagerHelper
import com.facebook.stetho.Stetho
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

@HiltAndroidApp
class OneLookApplication : Application(), Configuration.Provider {

    @Inject
    @ApplicationCoroutine
    lateinit var coroutineScope: CoroutineScope

    @Inject
    lateinit var alarmManagerHelper: AlarmManagerHelper

    @Inject
    lateinit var appStateManager: AppStateManager

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

    private suspend fun scheduleDailyTasksWorker() {
        val tomorrowCalendar = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.HOUR_OF_DAY, 0)
            add(Calendar.DAY_OF_YEAR, 1)
        }
        val milliSeconds = tomorrowCalendar.timeInMillis - Calendar.getInstance().timeInMillis
        alarmManagerHelper.setAlarmForDailyTasksReceiver(delayMilliSeconds = milliSeconds.toInt())
    }
}