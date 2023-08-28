package com.example.onelook.tasks.doamin.repository

import com.example.onelook.tasks.data.local.ActivityHistoryEntity
import com.example.onelook.tasks.doamin.model.ActivityHistory
import com.example.onelook.tasks.doamin.model.DomainActivity
import com.example.onelook.common.util.Resource
import com.example.onelook.common.util.OperationSource
import kotlinx.coroutines.flow.Flow

interface ActivityHistoryRepository {

    fun createActivityHistory(
        activityHistoryEntity: ActivityHistoryEntity
    ): Flow<Resource<OperationSource>>

    fun getActivitiesHistory(activity: DomainActivity): Flow<Resource<List<ActivityHistory>>>

    fun updateActivityHistory(
        activityHistoryEntity: ActivityHistoryEntity
    ): Flow<Resource<OperationSource>>
}