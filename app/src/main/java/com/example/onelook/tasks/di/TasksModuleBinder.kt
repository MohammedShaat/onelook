package com.example.onelook.tasks.di

import com.example.onelook.tasks.data.repository.ActivityHistoryRepositoryImpl
import com.example.onelook.tasks.data.repository.ActivityRepositoryImpl
import com.example.onelook.tasks.data.repository.SupplementHistoryRepositoryImpl
import com.example.onelook.tasks.data.repository.SupplementRepositoryImpl
import com.example.onelook.tasks.data.repository.TodayTasksRepositoryImpl
import com.example.onelook.tasks.doamin.repository.ActivityHistoryRepository
import com.example.onelook.tasks.doamin.repository.ActivityRepository
import com.example.onelook.tasks.doamin.repository.SupplementHistoryRepository
import com.example.onelook.tasks.doamin.repository.SupplementRepository
import com.example.onelook.tasks.doamin.repository.TodayTasksRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TasksModuleBinder {

    @Binds
    abstract fun bindActivityRepository(
        activityRepositoryImpl: ActivityRepositoryImpl
    ): ActivityRepository

    @Binds
    abstract fun bindSupplementRepository(
        supplementRepositoryImpl: SupplementRepositoryImpl
    ): SupplementRepository

    @Binds
    abstract fun bindSyncRepository(
        todayTasksRepositoryImpl: TodayTasksRepositoryImpl
    ): TodayTasksRepository

    @Binds
    abstract fun bindActivityHistoryRepository(
        activityRepositoryHistoryImpl: ActivityHistoryRepositoryImpl
    ): ActivityHistoryRepository

    @Binds
    abstract fun bindSupplementHistoryRepository(
        supplementRepositoryHistoryImpl: SupplementHistoryRepositoryImpl
    ): SupplementHistoryRepository
}