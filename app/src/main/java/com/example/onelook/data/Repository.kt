package com.example.onelook.data

import com.example.onelook.data.domain.DomainActivity
import com.example.onelook.data.domain.Supplement
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
import com.example.onelook.util.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Integer.min
import java.util.*
import javax.inject.Inject

class Repository @Inject constructor(
//    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
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
) {


    suspend fun loginUserInDatabase(user: LocalUser) {
        userDao.insertUser(user)
    }

    fun getTodayTasks(
        onForceRefreshFailed: suspend (Exception) -> Unit = {},
        forceRefresh: Boolean = false,
    ) = flow<CustomResult<List<TodayTask>>> {
        // Tries to fetch initial cached data
        val localTodayTasks = (todayTaskDao.getTodaySupplementTasks().first() +
                todayTaskDao.getTodayActivityTasks().first()).sortByDate()
        emit(CustomResult.Loading(localTodayTasks))

        // Tries to fetch data from network and store it in cache
        try {
            val networkTodayTasks = todayTaskApi.getTodayTasks()
            activityDao.insertActivities(networkTodayTasks.mapNotNull { it.activity?.toLocalModel() })
            supplementDao.insertSupplements(networkTodayTasks.mapNotNull { it.supplement?.toLocalModel() })
            supplementHistoryDao.insertSupplementsHistory(networkTodayTasks.mapNotNull { it.supplementHistory?.toLocalModel() })
            activityHistoryDao.insertActivitiesHistory(networkTodayTasks.mapNotNull { it.activityHistory?.toLocalModel() })
        } catch (exception: Exception) {
            Timber.e(exception)
            if (forceRefresh)
                onForceRefreshFailed(exception)
        } finally {
            // Fetches final cached data that will be collected
            val supplementTodayTasks = todayTaskDao.getTodaySupplementTasks()
            val activityTodayTasks = todayTaskDao.getTodayActivityTasks()
            val sortedTodayTasks =
                supplementTodayTasks.combine(activityTodayTasks) { tasks1, tasks2 ->
                    CustomResult.Success((tasks1 + tasks2).sortByDate())
                }
            emitAll(sortedTodayTasks)
        }
    }

    fun createSupplement(localSupplement: LocalSupplement) = flow<CustomResult<OperationSource>> {
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
    }

    fun createActivity(localActivity: LocalActivity) = flow<CustomResult<OperationSource>> {
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
    }

    fun getActivities(
        onForceRefreshFailed: suspend (Exception) -> Unit = {},
        forceRefresh: Boolean = false,
    ) = flow<CustomResult<List<DomainActivity>>> {
        // Tries to fetch initial cached data if operation is not force refresh
        val localActivities = activityDao.getActivities().first()
        emit(CustomResult.Loading(localActivities.map { it.toDomainModel() }))

        // Tries to fetch data from network and store it in cache
        try {
            val networkActivities = activityApi.getActivities()
            activityDao.insertActivities(networkActivities.map { it.toLocalModel() })
        } catch (exception: Exception) {
            Timber.e(exception)
            if (forceRefresh)
                onForceRefreshFailed(exception)
        } finally {
            // Fetches final cached data that will be collected
            val activities = activityDao.getActivities().map { list ->
                CustomResult.Success(list.map { it.toDomainModel() })
            }
            emitAll(activities)
        }
    }

    fun getSupplements(
        onForceRefreshFailed: suspend (Exception) -> Unit = {},
        forceRefresh: Boolean = false,
    ) = flow<CustomResult<List<Supplement>>> {
        val localSupplements = supplementDao.getSupplements().first()
        emit(CustomResult.Loading(localSupplements.map { it.toDomainModel() }))

        // Tries to fetch data from network and store it in cache
        try {
            val networkSupplements = supplementApi.getSupplements()
            supplementDao.insertSupplements(networkSupplements.map { it.toLocalModel() })
        } catch (exception: Exception) {
            Timber.e(exception)
            if (forceRefresh)
                onForceRefreshFailed(exception)
        } finally {
            // Fetches final cached data that will be collected
            val activities = supplementDao.getSupplements().map { list ->
                CustomResult.Success(list.map { it.toDomainModel() })
            }
            emitAll(activities)
        }
    }

    fun updateSupplement(localSupplement: LocalSupplement) = flow<CustomResult<OperationSource>> {
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
    }

    fun updateActivity(localActivity: LocalActivity) = flow<CustomResult<OperationSource>> {
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
    }

    fun deleteSupplement(localSupplement: LocalSupplement) = flow<CustomResult<OperationSource>> {
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
    }

    fun deleteActivity(localActivity: LocalActivity) = flow<CustomResult<OperationSource>> {
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
    }

    fun updateSupplementHistory(localSupplementHistory: LocalSupplementHistory) =
        flow<CustomResult<OperationSource>> {
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

    fun updateActivityHistory(localActivityHistory: LocalActivityHistory) =
        flow<CustomResult<OperationSource>> {
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

//    suspend fun sync() = scope.launch {
//        val differedLocalSupplements = async { supplementDao.getSupplements().first() }
//        val differedLocalActivities = async { activityDao.getActivities().first() }
//    }
}