package com.example.onelook.data

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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class Repository @Inject constructor(
    private val auth: FirebaseAuth,
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

    fun getTodayTasks(
        whileLoading: suspend () -> Unit,
        whileRefreshing: suspend () -> Unit,
        onRefreshSucceeded: suspend () -> Unit,
        onRefreshFailed: suspend (Exception) -> Unit,
        forceRefresh: Boolean = false,
    ) = flow<CustomResult<List<TodayTask>>> {
        // Tries to fetch initial cached data if operation is not force refresh
        if (!forceRefresh) {
            whileLoading()
            val localTodayTasks = (todayTaskDao.getTodaySupplementTasks().first() +
                    todayTaskDao.getTodayActivityTasks().first()).sortByDate()

            // Indicates that is refreshing if there is cached data, otherwise still loading
            if (localTodayTasks.isNotEmpty()) {
                whileRefreshing()
                emit(CustomResult.Refreshing(localTodayTasks))
            }
        } else {
            whileRefreshing()
        }
        // Tries to fetch data from network and store it in cache
        try {
            val networkTodayTasks = todayTaskApi.getTodayTasks()
            activityDao.insertActivities(networkTodayTasks.mapNotNull { it.activity?.toLocalModel() })
            supplementDao.insertSupplements(networkTodayTasks.mapNotNull { it.supplement?.toLocalModel() })
            supplementHistoryDao.insertSupplementsHistory(networkTodayTasks.mapNotNull { it.supplementHistory?.toLocalModel() })
            activityHistoryDao.insertActivitiesHistory(networkTodayTasks.mapNotNull { it.activityHistory?.toLocalModel() })
            onRefreshSucceeded()
        } catch (exception: Exception) {
            Timber.e(exception)
            onRefreshFailed(exception)
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

    suspend fun loginUserInDatabase(user: LocalUser) {
        userDao.insertUser(user)
    }

    fun createSupplement(
        localSupplement: LocalSupplement,
        creationDateTime: String
    ) = flow<CustomResult<OperationSource>> {
        emit(CustomResult.Loading())
        supplementDao.insertSupplement(localSupplement)
        val localSupplementHistory = LocalSupplementHistory(
            id = UUID.randomUUID(),
            supplementId = localSupplement.id,
            progress = 0,
            completed = false,
            createdAt = creationDateTime,
            updatedAt = creationDateTime,
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

    fun createActivity(
        localActivity: LocalActivity,
        creationDateTime: String
    ) = flow<CustomResult<OperationSource>> {
        emit(CustomResult.Loading())
        activityDao.insertActivity(localActivity)
        val localActivityHistory = LocalActivityHistory(
            id = UUID.randomUUID(),
            activityId = localActivity.id,
            progress = "00:00",
            completed = false,
            createdAt = creationDateTime,
            updatedAt = creationDateTime,
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
}