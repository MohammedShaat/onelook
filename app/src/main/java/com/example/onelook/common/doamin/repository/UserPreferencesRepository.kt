package com.example.onelook.common.doamin.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun getAllNotificationsState(): Flow<Boolean>

    suspend fun changeAllNotificationsState(areEnabled: Boolean)

    fun getActivitiesNotificationsState(): Flow<Boolean>

    suspend fun changeActivitiesNotificationsState(areEnabled: Boolean)

    fun getSupplementsNotificationsState(): Flow<Boolean>

    suspend fun changeSupplementsNotificationsState(areEnabled: Boolean)
}