package com.example.onelook.common.doamin.repository

import com.example.onelook.common.data.repository.AppState
import kotlinx.coroutines.flow.Flow

interface AppStateRepository {

    suspend fun getAppState(): AppState

    suspend fun updateAppState(state: AppState)

    suspend fun getAccessToken(): String?

    suspend fun setAccessToken(accessToken: String)

    suspend fun getLastSyncDate(): String

    suspend fun setLastSyncDate(date: String)

    fun getLastDailyTasksWorkerDate(): Flow<String>

    suspend fun setLastDailyTasksWorkerDate(date: String)

    fun getUnreadNotifications(): Flow<Int>

    suspend fun increaseUnreadNotifications()

    suspend fun decreaseUnreadNotifications()

    suspend fun clearUnreadNotifications()

    suspend fun getDailyTasksReceiverAlarmExists(): Boolean

    suspend fun setDailyTasksReceiverAlarmExists(exists: Boolean)

    suspend fun getInitialDailyTasksReceiverAlarmExists(): Boolean

    suspend fun setInitialDailyTasksReceiverAlarmExists(exists: Boolean)

    suspend fun clear()
}