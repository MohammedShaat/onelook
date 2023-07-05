package com.example.onelook.data

import android.content.Context
import androidx.datastore.preferences.clear
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import com.example.onelook.util.DATE_SEVENTIES
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
class AppStateManager @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.createDataStore("app_state_manager")

    private val appStateKey =
        preferencesKey<String>("app_state")
    private val accessTokenKey = preferencesKey<String>("access_token")
    private val lastSyncDateKey = preferencesKey<String>("last_sync_date")
    private val unreadNotifications = preferencesKey<Int>("unread_notifications")

    private val preferencesFlow = dataStore.data.catch { exception ->
        Timber.e(exception)
        emit(emptyPreferences())
    }

    suspend fun getAppState(): AppState {
        val preferences = preferencesFlow.first()
        return AppState.valueOf(
            preferences[appStateKey] ?: AppState.FIRST_LAUNCH.name
        )
    }

    suspend fun updateAppState(state: AppState) {
        dataStore.edit { preferences ->
            preferences[appStateKey] = state.name
        }
    }

    suspend fun getAccessToken(): String {
        return preferencesFlow.first()[accessTokenKey]!!
    }

    suspend fun setAccessToken(accessToken: String) {
        dataStore.edit { preferences ->
            preferences[accessTokenKey] = accessToken
        }
    }

    suspend fun getLastSyncDate(): String {
        return preferencesFlow.first()[lastSyncDateKey] ?: DATE_SEVENTIES
    }

    suspend fun setLastSyncDate(date: String) {
        dataStore.edit { preferences ->
            preferences[lastSyncDateKey] = date
        }
    }

    fun getUnreadNotifications(): Flow<Int> {
        return preferencesFlow.map { preferences ->
            preferences[unreadNotifications] ?: 0
        }
    }

    suspend fun increaseUnreadNotifications() {
        dataStore.edit { preferences ->
            preferences[unreadNotifications] = getUnreadNotifications().first() + 1
        }
    }

    suspend fun decreaseUnreadNotifications() {
        dataStore.edit { preferences ->
            preferences[unreadNotifications] = max(getUnreadNotifications().first() - 1, 0)
        }
    }

    suspend fun clearUnreadNotifications() {
        dataStore.edit { preferences ->
            preferences[unreadNotifications] = 0
        }
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

enum class AppState {
    FIRST_LAUNCH, LOGGED_IN, LOGGED_OUT
}