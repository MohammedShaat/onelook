package com.example.onelook.tasks.data.repository

import com.example.onelook.common.data.repository.AppStateRepositoryImpl
import com.example.onelook.timer.data.local.Timer
import com.example.onelook.tasks.data.local.ActivityDao
import com.example.onelook.tasks.data.local.ActivityHistoryDao
import com.example.onelook.tasks.data.local.SupplementDao
import com.example.onelook.tasks.data.local.SupplementHistoryDao
import com.example.onelook.tasks.data.local.TodayTaskDao
import com.example.onelook.tasks.data.mapper.toActivityDto
import com.example.onelook.tasks.data.mapper.toActivityEntity
import com.example.onelook.tasks.data.mapper.toActivityHistoryDto
import com.example.onelook.tasks.data.mapper.toActivityHistoryEntity
import com.example.onelook.tasks.data.mapper.toSupplementDto
import com.example.onelook.tasks.data.mapper.toSupplementEntity
import com.example.onelook.tasks.data.mapper.toSupplementHistoryDto
import com.example.onelook.tasks.data.mapper.toSupplementHistoryEntity
import com.example.onelook.tasks.data.remote.ActivityApi
import com.example.onelook.tasks.data.remote.ActivityHistoryApi
import com.example.onelook.tasks.data.remote.SupplementApi
import com.example.onelook.tasks.data.remote.SupplementHistoryApi
import com.example.onelook.tasks.doamin.model.TodayTask
import com.example.onelook.tasks.doamin.repository.TodayTasksRepository
import com.example.onelook.common.util.AlarmManagerHelper
import com.example.onelook.common.util.Resource
import com.example.onelook.common.util.format
import com.example.onelook.common.util.parseDate
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodayTasksRepositoryImpl @Inject constructor(
    private val appState: AppStateRepositoryImpl,
    private val activityApi: ActivityApi,
    private val supplementApi: SupplementApi,
    private val supplementHistoryApi: SupplementHistoryApi,
    private val activityHistoryApi: ActivityHistoryApi,
    private val activityDao: ActivityDao,
    private val supplementDao: SupplementDao,
    private val supplementHistoryDao: SupplementHistoryDao,
    private val activityHistoryDao: ActivityHistoryDao,
    private val todayTaskDao: TodayTaskDao,
    private val alarmManagerHelper: AlarmManagerHelper
) : TodayTasksRepository {

    override fun getTodayTasks(
        onForceRefreshFailed: suspend (Exception) -> Unit,
        forceRefresh: Boolean
    ) = flow<Resource<List<TodayTask>>> {
        val todayTasks = todayTaskDao.getTodaySupplementTasks()
            .combine(todayTaskDao.getTodayActivityTasks()) { list1, list2 -> list1 + list2 }

        if (forceRefresh) {
            emit(Resource.Loading(todayTasks.first()))
            val refresh = sync().last()
            if (refresh is Resource.Success) {
                todayTasks.collect { list ->
                    emit(Resource.Success(list))
                }
            } else if (refresh is Resource.Failure) {
                Timber.e(refresh.exception)
                onForceRefreshFailed(refresh.exception!!)
                todayTasks.collect { list ->
                    emit(Resource.Failure(refresh.exception, list))
                }
            }
        } else {
            todayTasks.collect {
                emit(Resource.Success(it))
            }
        }
    }

    override fun sync() = flow {
        try {
            Timber.i("sync started")
            Timer.isSyncing.value = true
            val lastSyncDate = appState.getLastSyncDate().parseDate

            // Fetches changes after last sync
            // Local data
            val localSupplements = supplementDao.getSupplements().first()
            val localActivities = activityDao.getActivities().first()
            val localSupplementsHistory = supplementHistoryDao.getAllSupplementsHistory().first()
            val localActivitiesHistory = activityHistoryDao.getAllActivitiesHistory().first()
            // Network data
            val networkSupplements = supplementApi.getSupplements()
            val networkActivities = activityApi.getActivities()
            val networkSupplementsHistory = supplementHistoryApi.getSupplementsHistory()
            val networkActivitiesHistory = activityHistoryApi.getActivitiesHistory()


            // Performs syncing
            //
            localActivities.forEach { localActivity ->
                // Checks if update, add, or delete
                val networkActivity =
                    networkActivities.firstOrNull { it.id == localActivity.id }
                when {
                    // Updated on local or network
                    networkActivity != null -> {
                        if (localActivity.updatedAt > networkActivity.updatedAt)
                            activityApi.updateActivity(localActivity.toActivityDto())
                        else if (networkActivity.updatedAt > localActivity.updatedAt) {
                            activityDao.updateActivity(networkActivity.toActivityEntity())
                            alarmManagerHelper.setAlarm(networkActivity.toActivityEntity())
                        }
                    }
                    // Added on local
                    localActivity.createdAt.parseDate > lastSyncDate -> {
                        activityApi.createActivity(localActivity.toActivityDto())
                    }
                    // Deleted on network
                    else -> {
                        activityDao.deleteActivity(localActivity)
                        alarmManagerHelper.cancelAlarm(localActivity)
                    }
                }
            }//localActivities

            //
            localSupplements.forEach { localSupplement ->
                // Checks if update, add, or delete
                val networkSupplement =
                    networkSupplements.firstOrNull { it.id == localSupplement.id }
                when {
                    // Updated on local or network
                    networkSupplement != null -> {
                        if (localSupplement.updatedAt > networkSupplement.updatedAt)
                            supplementApi.updateSupplement(localSupplement.toSupplementDto())
                        else if (networkSupplement.updatedAt > localSupplement.updatedAt) {
                            supplementDao.updateSupplement(networkSupplement.toSupplementEntity())
                            alarmManagerHelper.setAlarm(networkSupplement.toSupplementEntity())
                        }
                        return@forEach
                    }
                    // Added on local
                    localSupplement.createdAt.parseDate > lastSyncDate -> {
                        supplementApi.createSupplement(localSupplement.toSupplementDto())
                    }
                    // Deleted on network
                    else -> {
                        supplementDao.deleteSupplement(localSupplement)
                        alarmManagerHelper.cancelAlarm(localSupplement)
                    }
                }
            }//localSupplements

            //
            localActivitiesHistory.forEach { localActivityHistory ->
                // Checks if update, add, or delete
                val networkActivityHistory =
                    networkActivitiesHistory.firstOrNull { it.id == localActivityHistory.id }
                when {
                    // Updated on local or network
                    networkActivityHistory != null -> {
                        if (localActivityHistory.updatedAt > networkActivityHistory.updatedAt)
                            activityHistoryApi.updateActivityHistory(localActivityHistory.toActivityHistoryDto())
                        else if (networkActivityHistory.updatedAt > localActivityHistory.updatedAt)
                            activityHistoryDao.updateActivityHistory(networkActivityHistory.toActivityHistoryEntity())
                    }
                    // Added on local
                    localActivityHistory.createdAt.parseDate > lastSyncDate -> {
                        activityHistoryApi.createActivityHistory(localActivityHistory.toActivityHistoryDto())
                    }
                    // Deleted on network
                    else -> {
                        activityHistoryDao.deleteActivityHistory(localActivityHistory)
                    }
                }
            }//localActivitiesHistory

            //
            localSupplementsHistory.forEach { localSupplementHistory ->
                // Checks if update, add, or delete
                val networkSupplementHistory =
                    networkSupplementsHistory.firstOrNull { it.id == localSupplementHistory.id }
                when {
                    // Updated on local or network
                    networkSupplementHistory != null -> {
                        if (localSupplementHistory.updatedAt > networkSupplementHistory.updatedAt)
                            supplementHistoryApi.updateSupplementHistory(localSupplementHistory.toSupplementHistoryDto())
                        else if (networkSupplementHistory.updatedAt > localSupplementHistory.updatedAt)
                            supplementHistoryDao.updateSupplementHistory(
                                networkSupplementHistory.toSupplementHistoryEntity()
                            )
                    }
                    // Added on local
                    localSupplementHistory.createdAt.parseDate > lastSyncDate -> {
                        supplementHistoryApi.createSupplementHistory(localSupplementHistory.toSupplementHistoryDto())
                    }
                    // Deleted on network
                    else -> {
                        supplementHistoryDao.deleteSupplementHistory(localSupplementHistory)
                    }
                }
            }//localSupplementsHistory

            //
            networkActivities.forEach { networkActivity ->
                // Checks only if add or delete since we already checked updates
                localActivities.firstOrNull { it.id == networkActivity.id }
                    ?:
                    // Added on network
                    if (networkActivity.createdAt.parseDate > lastSyncDate) {
                        activityDao.insertActivity(networkActivity.toActivityEntity())
                        alarmManagerHelper.setAlarm(networkActivity.toActivityEntity())
                    }
                    // Deleted on local
                    else
                        activityApi.deleteActivity(networkActivity.id)
            }//networkActivities

            //
            networkSupplements.forEach { networkSupplement ->
                // Checks only if add or delete since we already checked updates
                localSupplements.firstOrNull { it.id == networkSupplement.id }
                    ?:
                    // Added on network
                    if (networkSupplement.createdAt.parseDate > lastSyncDate) {
                        supplementDao.insertSupplement(networkSupplement.toSupplementEntity())
                        alarmManagerHelper.setAlarm(networkSupplement.toSupplementEntity())
                    }
                    // Deleted on local
                    else
                        supplementApi.deleteSupplement(networkSupplement.id)
            }//networkSupplements

            //
            networkActivitiesHistory.forEach { networkActivityHistory ->
                // Checks only if add or delete since we already checked updates
                localActivitiesHistory.firstOrNull { it.id == networkActivityHistory.id }
                    ?:
                    // Added on network
                    if (networkActivityHistory.createdAt.parseDate > lastSyncDate)
                        activityHistoryDao.insertActivityHistory(networkActivityHistory.toActivityHistoryEntity())
                    // Deleted on local
                    else
                        activityHistoryApi.deleteActivityHistory(networkActivityHistory.id)
            }//networkActivitiesHistory

            //
            networkSupplementsHistory.forEach { networkSupplementHistory ->
                // Checks only if add or delete since we already checked updates
                localSupplementsHistory.firstOrNull { it.id == networkSupplementHistory.id }
                    ?:
                    // Added on network
                    if (networkSupplementHistory.createdAt.parseDate > lastSyncDate)
                        supplementHistoryDao.insertSupplementHistory(networkSupplementHistory.toSupplementHistoryEntity())
                    // Deleted on local
                    else
                        supplementHistoryApi.deleteSupplementHistory(networkSupplementHistory.id)
            }//networkSupplementsHistory

            appState.setLastSyncDate(Date().format)

            Timber.i("sync succeeded")
            emit(Resource.Success(Unit))
        } catch (exception: Exception) {
            Timber.i("sync failed\n$exception")
            emit(Resource.Failure(exception))
        } finally {
            Timer.isSyncing.value = false
        }
    }
}