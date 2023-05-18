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
        preferencesKey<String>("application_launch_state")

    private val applicationLaunchStateFlow = dataStore.data.catch { throwable ->
        Timber.e(throwable)
        emit(emptyPreferences())
    }.map { preferences ->
        val state = ApplicationLaunchState.valueOf(
            preferences[applicationLaunchStateKey] ?: ApplicationLaunchState.FirstLaunch.name
        )
        state
    }

    suspend fun isFirstLaunched(): Boolean {
        return applicationLaunchStateFlow.first() == ApplicationLaunchState.FirstLaunch
    }

    suspend fun updateApplicationLaunchState() {
        dataStore.edit { preferences ->
            preferences[applicationLaunchStateKey] = ApplicationLaunchState.SubsequentLaunch.name
        }
    }

}

enum class ApplicationLaunchState {
    FirstLaunch, SubsequentLaunch
}