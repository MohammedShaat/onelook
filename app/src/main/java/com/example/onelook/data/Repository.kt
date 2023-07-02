package com.example.onelook.data

import android.content.Context
import com.example.onelook.data.domain.TodayTask
import com.example.onelook.data.local.activities.ActivityDao
import com.example.onelook.data.local.activities.LocalActivity
import com.example.onelook.data.local.activitieshistory.ActivityHistoryDao
import com.example.onelook.data.local.activitieshistory.LocalActivityHistory
import com.example.onelook.data.local.supplements.LocalSupplement
import com.example.onelook.data.local.supplements.SupplementDao
import com.example.onelook.data.local.supplementshistory.LocalSupplementHistory
import com.example.onelook.data.local.supplementshistory.SupplementHistoryDao
import com.example.onelook.data.local.todaytasks.TodayTaskDao
import com.example.onelook.data.local.users.LocalUser
import com.example.onelook.data.local.users.UserDao
import com.example.onelook.data.network.activities.ActivityApi
import com.example.onelook.data.network.activitieshistory.ActivityHistoryApi
import com.example.onelook.data.network.supplements.SupplementApi
import com.example.onelook.data.network.supplementshistory.SupplementHistoryApi
import com.example.onelook.data.network.todaytasks.TodayTaskApi
import com.example.onelook.util.AlarmManagerHelper
import com.example.onelook.util.CustomResult
import com.example.onelook.util.OperationSource
import com.example.onelook.util.parse
import com.example.onelook.util.dateStr
import com.example.onelook.util.toDomainModel
import com.example.onelook.util.toLocalModel
import com.example.onelook.util.toNetworkModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import timber.log.Timber
import java.lang.Integer.min
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
    private val appState: AppStateManager,
    private val todayTaskApi: TodayTaskApi,
    private val todayTaskDao: TodayTaskDao,
    private val activityApi: ActivityApi,
    private val supplementApi: SupplementApi,
    private val supplementHistoryApi: SupplementHistoryApi,
    private val activityHistoryApi: ActivityHistoryApi,
    private val userDao: UserDao,
    private val activityDao: ActivityDao,
    private val supplementDao: SupplementDao,
    private val supplementHistoryDao: SupplementHistoryDao,
    private val activityHistoryDao: ActivityHistoryDao,
    @ApplicationContext private val context: Context,
    private val alarmManagerHelper: AlarmManagerHelper
) {


    suspend fun loginUserInDatabase(user: LocalUser) {
        userDao.insertUser(user)
    }

    fun getTodayTasks(
        onForceRefreshFailed: suspend (Exception) -> Unit = {},
        forceRefresh: Boolean = false,
    ) = flow<CustomResult<List<TodayTask>>> {
        val todayTasks = todayTaskDao.getTodaySupplementTasks()
            .combine(todayTaskDao.getTodayActivityTasks()) { list1, list2 -> list1 + list2 }

        if (forceRefresh) {
            emit(CustomResult.Loading(todayTasks.first()))
            val refresh = sync().last()
            if (refresh is CustomResult.Success) {
                todayTasks.collect { list ->
                    emit(CustomResult.Success(list))
                }
            } else if (refresh is CustomResult.Failure) {
                Timber.e(refresh.exception)
                onForceRefreshFailed(refresh.exception!!)
                todayTasks.collect { list ->
                    emit(CustomResult.Failure(refresh.exception, list))
                }
            }
        } else {
            todayTasks.collect {
                emit(CustomResult.Success(it))
            }
        }
    }

    fun createActivity(localActivity: LocalActivity) = flow {
        emit(CustomResult.Loading())
        activityDao.insertActivity(localActivity)
        val localActivityHistory = LocalActivityHistory(
            id = UUID.randomUUID(),
            activityId = localActivity.id,
            progress = "00:00",
            completed = false,
            createdAt = localActivity.createdAt,
            updatedAt = localActivity.updatedAt,
        )
        activityHistoryDao.insertActivityHistory(localActivityHistory)

        try {
            activityApi.createActivity(localActivity.toNetworkModel())
            activityHistoryApi.createActivityHistory(localActivityHistory.toNetworkModel())
            Timber.i("Activity and ActivityHistory created locally and remotely")
            emit(CustomResult.Success(OperationSource.LOCAL_AND_REMOTE))
        } catch (exception: Exception) {
            Timber.e("Activity and ActivityHistory created locally only\n$exception")
            emit(CustomResult.Success(OperationSource.LOCAL_ONLY))
        }

        alarmManagerHelper.setAlarm(localActivity)
    }

    fun createSupplement(localSupplement: LocalSupplement) = flow {
        emit(CustomResult.Loading())
        supplementDao.insertSupplement(localSupplement)
        val localSupplementHistory = LocalSupplementHistory(
            id = UUID.randomUUID(),
            supplementId = localSupplement.id,
            progress = 0,
            completed = false,
            createdAt = localSupplement.createdAt,
            updatedAt = localSupplement.updatedAt,
        )
        supplementHistoryDao.insertSupplementHistory(localSupplementHistory)

        try {
            supplementApi.createSupplement(localSupplement.toNetworkModel())
            supplementHistoryApi.createSupplementHistory(localSupplementHistory.toNetworkModel())
            Timber.i("Supplement and SupplementHistory created locally and remotely")
            emit(CustomResult.Success(OperationSource.LOCAL_AND_REMOTE))
        } catch (exception: Exception) {
            Timber.e("Supplement and SupplementHistory created locally only\n$exception")
            emit(CustomResult.Success(OperationSource.LOCAL_ONLY))
        }

        alarmManagerHelper.setAlarm(localSupplement)
    }

    fun getActivities(
        onForceRefreshFailed: suspend (Exception) -> Unit = {},
        forceRefresh: Boolean = false,
    ) = flow {
        val activities = activityDao.getActivities()

        if (forceRefresh) {
            emit(CustomResult.Loading(activities.first().map { it.toDomainModel() }))
            val refresh = sync().last()
            if (refresh is CustomResult.Success) {
                activities.collect { list ->
                    emit(CustomResult.Success(list.map { it.toDomainModel() }))
                }
            } else if (refresh is CustomResult.Failure) {
                Timber.e(refresh.exception)
                onForceRefreshFailed(refresh.exception!!)
                activities.collect { list ->
                    emit(CustomResult.Failure(refresh.exception, list.map { it.toDomainModel() }))
                }
            }
        } else {
            activities.collect { list ->
                emit(CustomResult.Success(list.map { it.toDomainModel() }))
            }
        }
    }

    fun getSupplements(
        onForceRefreshFailed: suspend (Exception) -> Unit = {},
        forceRefresh: Boolean = false,
    ) = flow {
        val supplements = supplementDao.getSupplements()

        if (forceRefresh) {
            val refresh = sync().last()
            if (refresh is CustomResult.Success) {
                supplements.collect { list ->
                    emit(CustomResult.Success(list.map { it.toDomainModel() }))
                }
            } else if (refresh is CustomResult.Failure) {
                Timber.e(refresh.exception)
                onForceRefreshFailed(refresh.exception!!)
                supplements.collect { list ->
                    emit(CustomResult.Failure(refresh.exception, list.map { it.toDomainModel() }))
                }
            }
        } else {
            supplements.collect { list ->
                emit(CustomResult.Success(list.map { it.toDomainModel() }))
            }
        }
    }

    fun updateActivity(localActivity: LocalActivity) = flow {
        emit(CustomResult.Loading())
        activityDao.updateActivity(localActivity)

        activityHistoryDao.getActivitiesHistory(localActivity.id).first().firstOrNull()
            ?.let { localActivityHistory ->
                val newProgress = minOf(localActivityHistory.progress, localActivity.duration)
                activityHistoryDao.updateActivityHistory(
                    localActivityHistory.copy(
                        progress = newProgress,
                        completed = newProgress == localActivity.duration
                    )
                )
            }

        try {
            activityApi.updateActivity(localActivity.toNetworkModel())
            Timber.i("Activity updated locally and remotely")
            emit(CustomResult.Success(OperationSource.LOCAL_AND_REMOTE))
        } catch (exception: Exception) {
            Timber.e("Activity updated locally only\n$exception")
            emit(CustomResult.Success(OperationSource.LOCAL_ONLY))
        }

        alarmManagerHelper.setAlarm(localActivity)
    }

    fun updateSupplement(localSupplement: LocalSupplement) = flow {
        emit(CustomResult.Loading())
        supplementDao.updateSupplement(localSupplement)

        supplementHistoryDao.getSupplementsHistory(localSupplement.id).first().firstOrNull()
            ?.let { localSupplementHistory ->
                val newProgress = min(localSupplementHistory.progress, localSupplement.dosage)
                supplementHistoryDao.updateSupplementHistory(
                    localSupplementHistory.copy(
                        progress = newProgress,
                        completed = newProgress == localSupplement.dosage
                    )
                )
            }

        try {
            supplementApi.updateSupplement(localSupplement.toNetworkModel())
            Timber.i("Supplement updated locally and remotely")
            emit(CustomResult.Success(OperationSource.LOCAL_AND_REMOTE))
        } catch (exception: Exception) {
            Timber.e("Supplement updated locally only\n$exception")
            emit(CustomResult.Success(OperationSource.LOCAL_ONLY))
        }

        alarmManagerHelper.setAlarm(localSupplement)
    }

    fun deleteActivity(localActivity: LocalActivity) = flow {
        emit(CustomResult.Loading())
        activityDao.deleteActivity(localActivity)

        activityHistoryDao.getActivitiesHistory(localActivity.id).first()
            .forEach { localActivityHistory ->
                activityHistoryDao.deleteActivityHistory(localActivityHistory)
            }

        try {
            activityApi.deleteActivity(localActivity.id)
            Timber.i("Activity deleted locally and remotely")
            emit(CustomResult.Success(OperationSource.LOCAL_AND_REMOTE))
        } catch (exception: Exception) {
            Timber.e("Activity deleted locally only\n$exception")
            emit(CustomResult.Success(OperationSource.LOCAL_ONLY))
        }

        alarmManagerHelper.cancelAlarm(localActivity)
    }

    fun deleteSupplement(localSupplement: LocalSupplement) = flow {
        emit(CustomResult.Loading())
        supplementDao.deleteSupplement(localSupplement)

        supplementHistoryDao.getSupplementsHistory(localSupplement.id).first()
            .forEach { localActivityHistory ->
                supplementHistoryDao.deleteSupplementHistory(localActivityHistory)
            }

        try {
            supplementApi.deleteSupplement(localSupplement.id)
            Timber.i("Supplement deleted locally and remotely")
            emit(CustomResult.Success(OperationSource.LOCAL_AND_REMOTE))
        } catch (exception: Exception) {
            Timber.e("Supplement deleted locally only\n$exception")
            emit(CustomResult.Success(OperationSource.LOCAL_ONLY))
        }

        alarmManagerHelper.cancelAlarm(localSupplement)
    }

    fun updateActivityHistory(localActivityHistory: LocalActivityHistory) =
        flow {
            emit(CustomResult.Loading())
            activityHistoryDao.updateActivityHistory(localActivityHistory)

            try {
                activityHistoryApi.updateActivityHistory(localActivityHistory.toNetworkModel())
                Timber.i("ActivityHistory updated locally and remotely")
                emit(CustomResult.Success(OperationSource.LOCAL_AND_REMOTE))
            } catch (exception: Exception) {
                Timber.e("ActivityHistory updated locally only\n$exception")
                emit(CustomResult.Success(OperationSource.LOCAL_ONLY))
            }
        }

    fun updateSupplementHistory(localSupplementHistory: LocalSupplementHistory) =
        flow {
            emit(CustomResult.Loading())
            supplementHistoryDao.updateSupplementHistory(localSupplementHistory)

            try {
                supplementHistoryApi.updateSupplementHistory(localSupplementHistory.toNetworkModel())
                Timber.i("SupplementHistory updated locally and remotely")
                emit(CustomResult.Success(OperationSource.LOCAL_AND_REMOTE))
            } catch (exception: Exception) {
                Timber.e("SupplementHistory updated locally only\n$exception")
                emit(CustomResult.Success(OperationSource.LOCAL_ONLY))
            }
        }

    fun sync() = flow {
        try {
            Timber.i("sync started")
            SharedData.isSyncing.value = true
            val lastSyncDate = appState.getLastSyncDate().parse

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
                            activityApi.updateActivity(localActivity.toNetworkModel())
                        else
                            activityDao.updateActivity(networkActivity.toLocalModel())
                    }
                    // Added on local
                    localActivity.createdAt.parse > lastSyncDate -> {
                        activityApi.createActivity(localActivity.toNetworkModel())
                    }
                    // Deleted on network
                    else -> {
                        activityDao.deleteActivity(localActivity)
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
                            supplementApi.updateSupplement(localSupplement.toNetworkModel())
                        else
                            supplementDao.updateSupplement(networkSupplement.toLocalModel())
                        return@forEach
                    }
                    // Added on local
                    localSupplement.createdAt.parse > lastSyncDate -> {
                        supplementApi.createSupplement(localSupplement.toNetworkModel())
                    }
                    // Deleted on network
                    else -> {
                        supplementDao.deleteSupplement(localSupplement)
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
                            activityHistoryApi.updateActivityHistory(localActivityHistory.toNetworkModel())
                        else
                            activityHistoryDao.updateActivityHistory(networkActivityHistory.toLocalModel())
                    }
                    // Added on local
                    localActivityHistory.createdAt.parse > lastSyncDate -> {
                        activityHistoryApi.createActivityHistory(localActivityHistory.toNetworkModel())
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
                            supplementHistoryApi.updateSupplementHistory(localSupplementHistory.toNetworkModel())
                        else
                            supplementHistoryDao.updateSupplementHistory(
                                networkSupplementHistory.toLocalModel()
                            )
                    }
                    // Added on local
                    localSupplementHistory.createdAt.parse > lastSyncDate -> {
                        supplementHistoryApi.createSupplementHistory(localSupplementHistory.toNetworkModel())
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
                    if (networkActivity.createdAt.parse > lastSyncDate)
                        activityDao.insertActivity(networkActivity.toLocalModel())
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
                    if (networkSupplement.createdAt.parse > lastSyncDate)
                        supplementDao.insertSupplement(networkSupplement.toLocalModel())
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
                    if (networkActivityHistory.createdAt.parse > lastSyncDate)
                        activityHistoryDao.insertActivityHistory(networkActivityHistory.toLocalModel())
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
                    if (networkSupplementHistory.createdAt.parse > lastSyncDate)
                        supplementHistoryDao.insertSupplementHistory(networkSupplementHistory.toLocalModel())
                    // Deleted on local
                    else
                        supplementHistoryApi.deleteSupplementHistory(networkSupplementHistory.id)
            }//networkSupplementsHistory

            appState.setLastSyncDate(dateStr())

            Timber.i("sync succeeded")
            emit(CustomResult.Success(Unit))
        } catch (exception: Exception) {
            Timber.i("sync failed\n$exception")
            emit(CustomResult.Failure(exception))
        } finally {
            SharedData.isSyncing.value = false
        }
    }

    fun getLocalSupplementsHistory(supplementId: UUID) = flow {
        supplementHistoryDao.getSupplementsHistory(supplementId).collect {
            emit(CustomResult.Success(it))
        }
    }

    fun getLocalActivitiesHistory(activityId: UUID) = flow {
        activityHistoryDao.getActivitiesHistory(activityId).collect {
            emit(CustomResult.Success(it))
        }
    }
}