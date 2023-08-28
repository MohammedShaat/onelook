package com.example.onelook.tasks.doamin.repository

import com.example.onelook.tasks.data.local.ActivityEntity
import com.example.onelook.tasks.doamin.model.DomainActivity
import com.example.onelook.common.util.Resource
import com.example.onelook.common.util.OperationSource
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {

    fun createActivity(activityEntity: ActivityEntity): Flow<Resource<OperationSource>>

    fun updateActivity(activityEntity: ActivityEntity): Flow<Resource<OperationSource>>

    fun deleteActivity(activityEntity: ActivityEntity): Flow<Resource<OperationSource>>

    fun getActivities(
        onForceRefreshFailed: suspend (Exception) -> Unit = {},
        forceRefresh: Boolean = false,
    ): Flow<Resource<List<DomainActivity>>>
}