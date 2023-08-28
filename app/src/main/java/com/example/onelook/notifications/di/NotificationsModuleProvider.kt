package com.example.onelook.notifications.di

import com.example.onelook.common.data.local.OneLookDatabase
import com.example.onelook.notifications.data.local.NotificationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object NotificationsModuleProvider {

    @Provides
    fun provideNotificationDao(db: OneLookDatabase): NotificationDao {
        return db.notificationDao
    }
}