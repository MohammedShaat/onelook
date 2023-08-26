package com.example.onelook.tasks.doamin.repository

import com.example.onelook.tasks.doamin.model.TodayTask
import com.example.onelook.common.util.Resource
import kotlinx.coroutines.flow.Flow

interface TodayTasksRepository {

    fun sync(): Flow<Resource<Unit>>

    fun getTodayTasks(
        onForceRefreshFailed: suspend (Exception) -> Unit = {},
        forceRefresh: Boolean = false,
    ): Flow<Resource<List<TodayTask>>>
}