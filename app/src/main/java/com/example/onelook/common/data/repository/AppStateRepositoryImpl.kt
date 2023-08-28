package com.example.onelook.common.data.repository

import android.content.Context
import androidx.datastore.preferences.clear
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import com.example.onelook.common.doamin.repository.AppStateRepository
import com.example.onelook.common.util.DATE_SEVENTIES
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.lang.Integer.max
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppStateRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : AppStateRepository {

    private val dataStore = context.createDataStore("app_state_manager")

    private val appStateKey = preferencesKey<String>("app_state")
    private val accessTokenKey = preferencesKey<String>("access_token")
    private val lastSyncDateKey = preferencesKey<String>("last_sync_date")
    private val unreadNotifications = preferencesKey<Int>("unread_notifications")
    private val lastDailyTasksWorkerDateKey = preferencesKey<String>("last_daily_tasks_worker_date")
    private val dailyTasksReceiverAlarmExists =
        preferencesKey<Boolean>("daily_tasks_receiver_alarm_exists")
    private val initialDailyTasksReceiverAlarmExists =
        preferencesKey<Boolean>("initial_daily_tasks_receiver_alarm_exists")

    private val preferencesFlow = dataStore.data.catch { exception ->
        Timber.e(exception)
        emit(emptyPreferences())
    }

    override suspend fun getAppState(): AppState {
        val preferences = preferencesFlow.first()
        return AppState.valueOf(
            preferences[appStateKey] ?: AppState.FIRST_LAUNCH.name
        )
    }

    override suspend fun updateAppState(state: AppState) {
        dataStore.edit { preferences ->
            preferences[appStateKey] = state.name
        }
    }

    override suspend fun getAccessToken(): String? {
        return preferencesFlow.first()[accessTokenKey]
    }

    override suspend fun setAccessToken(accessToken: String) {
        dataStore.edit { preferences ->
            preferences[accessTokenKey] = accessToken
        }
    }

    override suspend fun getLastSyncDate(): String {
        return preferencesFlow.first()[lastSyncDateKey] ?: DATE_SEVENTIES
    }

    override suspend fun setLastSyncDate(date: String) {
        dataStore.edit { preferences ->
            preferences[lastSyncDateKey] = date
        }
    }

    override fun getLastDailyTasksWorkerDate(): Flow<String> {
        return preferencesFlow.map { preferences ->
            preferences[lastDailyTasksWorkerDateKey] ?: DATE_SEVENTIES
        }
    }

    override suspend fun setLastDailyTasksWorkerDate(date: String) {
        dataStore.edit { preferences ->
            preferences[lastDailyTasksWorkerDateKey] = date
        }
    }

    override fun getUnreadNotifications(): Flow<Int> {
        return preferencesFlow.map { preferences ->
            preferences[unreadNotifications] ?: 0
        }
    }

    override suspend fun increaseUnreadNotifications() {
        dataStore.edit { preferences ->
            preferences[unreadNotifications] = getUnreadNotifications().first() + 1
        }
    }

    override suspend fun decreaseUnreadNotifications() {
        dataStore.edit { preferences ->
            preferences[unreadNotifications] = max(getUnreadNotifications().first() - 1, 0)
        }
    }

    override suspend fun clearUnreadNotifications() {
        dataStore.edit { preferences ->
            preferences[unreadNotifications] = 0
        }
    }

    override suspend fun getDailyTasksReceiverAlarmExists(): Boolean {
        return preferencesFlow.first()[dailyTasksReceiverAlarmExists] ?: false
    }

    override suspend fun setDailyTasksReceiverAlarmExists(exists: Boolean) {
        dataStore.edit { preferences ->
            preferences[dailyTasksReceiverAlarmExists] = exists
        }
    }

    override suspend fun getInitialDailyTasksReceiverAlarmExists(): Boolean {
        return preferencesFlow.first()[initialDailyTasksReceiverAlarmExists] ?: false
    }

    override suspend fun setInitialDailyTasksReceiverAlarmExists(exists: Boolean) {
        dataStore.edit { preferences ->
            preferences[initialDailyTasksReceiverAlarmExists] = exists
        }
    }

    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

enum class AppState {
    FIRST_LAUNCH, LOGGED_IN, LOGGED_OUT
}