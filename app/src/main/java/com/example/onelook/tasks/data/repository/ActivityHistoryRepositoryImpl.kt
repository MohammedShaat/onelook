package com.example.onelook.tasks.data.repository

import com.example.onelook.tasks.data.local.ActivityHistoryDao
import com.example.onelook.tasks.data.local.ActivityHistoryEntity
import com.example.onelook.tasks.data.mapper.toActivityHistory
import com.example.onelook.tasks.data.remote.ActivityHistoryApi
import com.example.onelook.tasks.doamin.repository.ActivityHistoryRepository
import com.example.onelook.common.util.Resource
import com.example.onelook.common.util.OperationSource
import com.example.onelook.tasks.data.mapper.toActivityHistoryDto
import com.example.onelook.tasks.doamin.model.DomainActivity
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityHistoryRepositoryImpl @Inject constructor(
    private val activityHistoryDao: ActivityHistoryDao,
    private val activityHistoryApi: ActivityHistoryApi
) : ActivityHistoryRepository {
    override fun createActivityHistory(activityHistoryEntity: ActivityHistoryEntity) = flow {
        emit(Resource.Loading())
        activityHistoryDao.insertActivityHistory(activityHistoryEntity)

        try {
            activityHistoryApi.createActivityHistory(activityHistoryEntity.toActivityHistoryDto())
            Timber.i("ActivityHistory created locally and remotely")
            emit(Resource.Success(OperationSource.LOCAL_AND_REMOTE))
        } catch (exception: Exception) {
            Timber.e("ActivityHistory created locally only\n$exception")
            emit(Resource.Success(OperationSource.LOCAL_ONLY))
        }

    }

    override fun getActivitiesHistory(activity: DomainActivity) = flow {
        activityHistoryDao.getActivitiesHistory(activity.id).collect { list ->
            emit(Resource.Success(list.map { it.toActivityHistory(activity) }))
        }
    }

    override fun updateActivityHistory(activityHistoryEntity: ActivityHistoryEntity) = flow {
        emit(Resource.Loading())
        activityHistoryDao.updateActivityHistory(activityHistoryEntity)

        try {
            activityHistoryApi.updateActivityHistory(activityHistoryEntity.toActivityHistoryDto())
            Timber.i("ActivityHistory updated locally and remotely")
            emit(Resource.Success(OperationSource.LOCAL_AND_REMOTE))
        } catch (exception: Exception) {
            Timber.e("ActivityHistory updated locally only\n$exception")
            emit(Resource.Success(OperationSource.LOCAL_ONLY))
        }
    }
}