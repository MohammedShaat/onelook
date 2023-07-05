package com.example.onelook.data

import android.content.Context
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesManager @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.createDataStore("user_preferences_manager")

    private val allNotificationsKey = preferencesKey<Boolean>("all_notifications")
    private val activitiesNotificationsKey = preferencesKey<Boolean>("activities_notifications")
    private val supplementsNotificationsKey = preferencesKey<Boolean>("supplements_notifications")

    private val preferencesFlow = dataStore.data.catch { exception ->
        Timber.e(exception)
        emptyPreferences()
    }

    fun getAllNotificationsState(): Flow<Boolean> {
        return preferencesFlow.map { preferences ->
            (preferences[allNotificationsKey] ?: true)
        }
    }

    suspend fun changeAllNotificationsState(areEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[allNotificationsKey] = areEnabled

            preferences[activitiesNotificationsKey] = preferences[activitiesNotificationsKey] ?: true
            preferences[supplementsNotificationsKey] = preferences[supplementsNotificationsKey] ?: true
        }
    }

    fun getActivitiesNotificationsState(): Flow<Boolean> {
        return preferencesFlow.map { preferences ->
            (preferences[activitiesNotificationsKey] ?: true) && (preferences[allNotificationsKey] ?: true)
        }
    }

    suspend fun changeActivitiesNotificationsState(areEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[activitiesNotificationsKey] = areEnabled
        }
    }

    fun getSupplementsNotificationsState(): Flow<Boolean> {
        return preferencesFlow.map { preferences ->
            (preferences[supplementsNotificationsKey] ?: true) && (preferences[allNotificationsKey] ?: true)
        }
    }

    suspend fun changeSupplementsNotificationsState(areEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[supplementsNotificationsKey] = areEnabled
        }
    }
}