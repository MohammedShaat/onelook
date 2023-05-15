package com.example.onelook

import android.app.Application
import timber.log.Timber

class OneLookApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}