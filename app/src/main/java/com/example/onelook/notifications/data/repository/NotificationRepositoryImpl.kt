package com.example.onelook.notifications.data.repository

import com.example.onelook.common.data.repository.AppStateRepositoryImpl
import com.example.onelook.notifications.data.local.NotificationDao
import com.example.onelook.notifications.data.local.NotificationEntity
import com.example.onelook.notifications.doamin.repository.NotificationRepository
import com.example.onelook.tasks.data.local.ActivityDao
import com.example.onelook.tasks.data.local.ActivityHistoryDao
import com.example.onelook.tasks.data.local.SupplementDao
import com.example.onelook.tasks.data.local.SupplementHistoryDao
import com.example.onelook.tasks.data.mapper.toActivityHistory
import com.example.onelook.tasks.data.mapper.toDomainActivity
import com.example.onelook.tasks.data.mapper.toSupplement
import com.example.onelook.tasks.data.mapper.toSupplementHistory
import com.example.onelook.tasks.doamin.model.SupplementHistory
import com.example.onelook.common.util.Resource
import com.example.onelook.notifications.data.mapper.toNotification
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao,
    private val supplementDao: SupplementDao,
    private val activityDao: ActivityDao,
    private val supplementHistoryDao: SupplementHistoryDao,
    private val activityHistoryDao: ActivityHistoryDao,
    private val appStateRepositoryImpl: AppStateRepositoryImpl,
) : NotificationRepository {
    override fun createNotification(notificationEntity: NotificationEntity) = flow {
        emit(Resource.Loading())
        notificationDao.insertNotification(notificationEntity)
        emit(Resource.Success(Unit))
    }

    override fun getNotifications() = flow {
        emit(Resource.Loading())
        notificationDao.getNotifications().collect { localNotifications ->

            val notifications = localNotifications.mapNotNull { localNotification ->
                val history =
                    if (localNotification.historyType == SupplementHistory::class.java.name) {
                        val localSupplementHistory =
                            supplementHistoryDao.getSupplementHistoryById(localNotification.historyId)
                                .firstOrNull() ?: return@mapNotNull null
                        val supplement =
                            supplementDao.getSupplementById(localSupplementHistory.supplementId)
                                .firstOrNull()?.toSupplement() ?: return@mapNotNull null
                        localSupplementHistory.toSupplementHistory(supplement)

                    } else {
                        val localActivityHistory =
                            activityHistoryDao.getActivityHistoryById(localNotification.historyId)
                                .firstOrNull() ?: return@mapNotNull null
                        val activity =
                            activityDao.getActivityById(localActivityHistory.activityId)
                                .firstOrNull()?.toDomainActivity() ?: return@mapNotNull null
                        localActivityHistory.toActivityHistory(activity)
                    }
                localNotification.toNotification(history)
            }

            emit(Resource.Success(notifications))
        }
    }

    override suspend fun resetNotificationsCounter() {
        appStateRepositoryImpl.clearUnreadNotifications()
    }
}