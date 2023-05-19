package com.example.onelook.data

import android.content.Context
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationLaunchStateManager @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.createDataStore("application_launch_state_manager")
    private val applicationLaunchStateKey =
        preferencesKey<Boolean>("application_launch_state")

    suspend fun isFinished(): Boolean {
        val state = dataStore.data.catch { throwable ->
            Timber.e(throwable)
            emit(emptyPreferences())
        }.map { preferences ->
            preferences[applicationLaunchStateKey] ?: true
        }.first()
        return state
    }

    suspend fun updateApplicationLaunchState() {
        dataStore.edit { preferences ->
            preferences[applicationLaunchStateKey] = false
        }
    }

}