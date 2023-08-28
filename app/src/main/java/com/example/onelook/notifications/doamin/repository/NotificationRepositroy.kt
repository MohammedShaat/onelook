package com.example.onelook.notifications.doamin.repository

import com.example.onelook.notifications.data.local.NotificationEntity
import com.example.onelook.notifications.doamin.model.Notification
import com.example.onelook.common.util.Resource
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {

    fun createNotification(notificationEntity: NotificationEntity): Flow<Resource<Unit>>

    fun getNotifications(): Flow<Resource<List<Notification>>>

    suspend fun resetNotificationsCounter()
}