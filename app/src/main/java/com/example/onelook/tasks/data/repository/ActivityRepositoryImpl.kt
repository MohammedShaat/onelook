package com.example.onelook.tasks.data.repository

import com.example.onelook.tasks.data.local.ActivityDao
import com.example.onelook.tasks.data.local.ActivityEntity
import com.example.onelook.tasks.data.local.ActivityHistoryDao
import com.example.onelook.tasks.data.local.ActivityHistoryEntity
import com.example.onelook.tasks.data.remote.ActivityApi
import com.example.onelook.tasks.data.remote.ActivityHistoryApi
import com.example.onelook.tasks.doamin.repository.ActivityRepository
import com.example.onelook.tasks.doamin.repository.TodayTasksRepository
import com.example.onelook.common.util.AlarmManagerHelper
import com.example.onelook.common.util.Resource
import com.example.onelook.common.util.OperationSource
import com.example.onelook.tasks.data.mapper.toDomainActivity
import com.example.onelook.tasks.data.mapper.toActivityHistoryDto
import com.example.onelook.tasks.data.mapper.toActivityDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepositoryImpl @Inject constructor(
    private val activityDao: ActivityDao,
    private val activityHistoryDao: ActivityHistoryDao,
    private val activityApi: ActivityApi,
    private val activityHistoryApi: ActivityHistoryApi,
    private val todayTasksRepository: TodayTasksRepository,
    private val alarmManagerHelper: AlarmManagerHelper,
) : ActivityRepository {
    override fun createActivity(activityEntity: ActivityEntity) = flow {
        emit(Resource.Loading())
        activityDao.insertActivity(activityEntity)

        val activityHistoryEntity = ActivityHistoryEntity(
            id = UUID.randomUUID(),
            activityId = activityEntity.id,
            progress = "00:00",
            completed = false,
            createdAt = activityEntity.createdAt,
            updatedAt = activityEntity.updatedAt,
        )
        activityHistoryDao.insertActivityHistory(activityHistoryEntity)

        try {
            activityApi.createActivity(activityEntity.toActivityDto())
            activityHistoryApi.createActivityHistory(activityHistoryEntity.toActivityHistoryDto())
            Timber.i("Activity and ActivityHistory created locally and remotely")
            emit(Resource.Success(OperationSource.LOCAL_AND_REMOTE))
        } catch (exception: Exception) {
            Timber.e("Activity and ActivityHistory created locally only\n$exception")
            emit(Resource.Success(OperationSource.LOCAL_ONLY))
        }

        alarmManagerHelper.setAlarm(activityEntity)
    }

    override fun deleteActivity(activityEntity: ActivityEntity) = flow {
        emit(Resource.Loading())
        activityDao.deleteActivity(activityEntity)

        activityHistoryDao.getActivitiesHistory(activityEntity.id).first()
            .forEach { localActivityHistory ->
                activityHistoryDao.deleteActivityHistory(localActivityHistory)
            }

        try {
            activityApi.deleteActivity(activityEntity.id)
            Timber.i("Activity deleted locally and remotely")
            emit(Resource.Success(OperationSource.LOCAL_AND_REMOTE))
        } catch (exception: Exception) {
            Timber.e("Activity deleted locally only\n$exception")
            emit(Resource.Success(OperationSource.LOCAL_ONLY))
        }

        alarmManagerHelper.cancelAlarm(activityEntity)
    }

    override fun getActivities(
        onForceRefreshFailed: suspend (Exception) -> Unit,
        forceRefresh: Boolean
    ) = flow {
        val activities = activityDao.getActivities()

        if (forceRefresh) {
            emit(Resource.Loading(activities.first().map { it.toDomainActivity() }))
            val refresh = todayTasksRepository.sync().last()
            if (refresh is Resource.Success) {
                activities.collect { list ->
                    emit(Resource.Success(list.map { it.toDomainActivity() }))
                }
            } else if (refresh is Resource.Failure) {
                Timber.e(refresh.exception)
                onForceRefreshFailed(refresh.exception!!)
                activities.collect { list ->
                    emit(Resource.Failure(refresh.exception, list.map { it.toDomainActivity() }))
                }
            }
        } else {
            activities.collect { list ->
                emit(Resource.Success(list.map { it.toDomainActivity() }))
            }
        }
    }


    override fun updateActivity(activityEntity: ActivityEntity) = flow {
        emit(Resource.Loading())
        activityDao.updateActivity(activityEntity)

        activityHistoryDao.getActivitiesHistory(activityEntity.id).first().firstOrNull()
            ?.let { localActivityHistory ->
                val newProgress = minOf(localActivityHistory.progress, activityEntity.duration)
                activityHistoryDao.updateActivityHistory(
                    localActivityHistory.copy(
                        progress = newProgress,
                        completed = newProgress == activityEntity.duration
                    )
                )
            }

        try {
            activityApi.updateActivity(activityEntity.toActivityDto())
            Timber.i("Activity updated locally and remotely")
            emit(Resource.Success(OperationSource.LOCAL_AND_REMOTE))
        } catch (exception: Exception) {
            Timber.e("Activity updated locally only\n$exception")
            emit(Resource.Success(OperationSource.LOCAL_ONLY))
        }

        alarmManagerHelper.setAlarm(activityEntity)
    }
}