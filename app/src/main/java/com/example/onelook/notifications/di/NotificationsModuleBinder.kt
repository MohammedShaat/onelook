package com.example.onelook.notifications.di

import com.example.onelook.notifications.data.repository.NotificationRepositoryImpl
import com.example.onelook.notifications.doamin.repository.NotificationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationsModuleBinder {

    @Binds
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository
}