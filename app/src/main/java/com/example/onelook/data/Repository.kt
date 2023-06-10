package com.example.onelook.data

import com.example.onelook.data.domain.TodayTask
import com.example.onelook.data.local.activities.ActivityDao
import com.example.onelook.data.local.activitieshistory.ActivityHistoryDao
import com.example.onelook.data.local.supplements.SupplementDao
import com.example.onelook.data.local.supplementshistory.SupplementHistoryDao
import com.example.onelook.data.local.todaytasks.TodayTaskDao
import com.example.onelook.data.local.users.LocalUser
import com.example.onelook.data.local.users.UserDao
import com.example.onelook.data.network.todaytasks.TodayTaskApi
import com.example.onelook.util.CustomResult
import com.example.onelook.util.sortByDate
import com.example.onelook.util.toLocalModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

class Repository @Inject constructor(
    private val auth: FirebaseAuth,
    private val todayTaskApi: TodayTaskApi,
    private val todayTaskDao: TodayTaskDao,
    private val userDao: UserDao,
    private val activityDao: ActivityDao,
    private val supplementDao: SupplementDao,
    private val supplementHistoryDao: SupplementHistoryDao,
    private val activityHistoryDao: ActivityHistoryDao
) {

    fun getTodayTasks(
        whileLoading: suspend () -> Unit,
        whileRefreshing: suspend () -> Unit,
        onRefreshSucceeded: suspend () -> Unit,
        onRefreshFailed: suspend (Exception) -> Unit,
        forceRefresh: Boolean = false,
    ) = flow<CustomResult<List<TodayTask>>> {
        val userId = userDao.getUserByFirebaseUid(auth.currentUser!!.uid).id

        // Tries to fetch initial cached data if operation is not force refresh
        if (!forceRefresh) {
            whileLoading()
            val localTodayTasks = (todayTaskDao.getTodaySupplementTasks(userId).first() +
                    todayTaskDao.getTodayActivityTasks(userId).first()).sortByDate()

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
            val supplementTodayTasks = todayTaskDao.getTodaySupplementTasks(userId)
            val activityTodayTasks = todayTaskDao.getTodayActivityTasks(userId)
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
}