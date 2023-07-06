package com.example.onelook.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.onelook.data.AppState
import com.example.onelook.data.AppStateManager
import com.example.onelook.data.Repository
import com.example.onelook.data.local.activitieshistory.LocalActivityHistory
import com.example.onelook.data.local.supplementshistory.LocalSupplementHistory
import com.example.onelook.util.CustomResult
import com.example.onelook.util.OperationSource
import com.example.onelook.util.dateStr
import com.example.onelook.util.isExpired
import com.example.onelook.util.isToday
import com.example.onelook.util.parse
import com.example.onelook.util.toLocalModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import java.util.UUID

@HiltWorker
class DailyTasksWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val appStateManager: AppStateManager,
    private val repository: Repository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (appStateManager.getAppState() != AppState.LOGGED_IN)
            return Result.failure()

        val result1 = createTodaySupplementsHistoryAndCheckCompletion()
        val result2 = createTodayActivitiesHistory()

        return if (Result.retry() in listOf(result1, result2)) Result.retry()
        else Result.success()
    }

    private suspend fun createTodaySupplementsHistoryAndCheckCompletion(): Result {
        val supplements = repository.getSupplements().first().data ?: return Result.retry()
        var localResult = Result.success()
        supplements.forEach { supplement ->
            // Completes expired supplements
            if (isExpired(supplement.createdAt.parse, supplement.duration)) {
                val updatedSupplement = supplement.copy(completed = true).toLocalModel()
                repository.updateSupplement(updatedSupplement).collect { result ->
                    if (result is CustomResult.Success && result.data == OperationSource.LOCAL_ONLY)
                        localResult = Result.retry()
                }//updateSupplement
                return@forEach
            }//isExpired

            // Returns if supplementHistory already exists
            val supplementHistory = repository.getSupplementsHistory(supplement).first().data
                ?.firstOrNull()?.takeIf { it.createdAt.parse.isToday }
            if (supplementHistory != null)
                return@forEach

            val dateStr = dateStr()
            val localSupplementHistory = LocalSupplementHistory(
                id = UUID.randomUUID(),
                supplementId = supplement.id,
                progress = 0,
                completed = false,
                createdAt = dateStr,
                updatedAt = dateStr
            )
            repository.createSupplementHistory(localSupplementHistory).collect { result ->
                if (result is CustomResult.Success && result.data == OperationSource.LOCAL_ONLY)
                    localResult = Result.retry()
            }//createSupplementHistory
        }
        return localResult
    }

    private suspend fun createTodayActivitiesHistory(): Result {
        val activities = repository.getActivities().first().data ?: return Result.retry()
        var localResult = Result.success()
        activities.forEach { activity ->
            // Returns if activityHistory already exists
            val activityHistory = repository.getActivitiesHistory(activity).first().data
                ?.firstOrNull()?.takeIf { it.createdAt.parse.isToday }
            if (activityHistory != null)
                return@forEach

            val dateStr = dateStr()
            val localActivityHistory = LocalActivityHistory(
                id = UUID.randomUUID(),
                activityId = activity.id,
                progress = "00:00",
                completed = false,
                createdAt = dateStr,
                updatedAt = dateStr
            )
            repository.createActivityHistory(localActivityHistory).collect { result ->
                if (result is CustomResult.Success && result.data == OperationSource.LOCAL_ONLY)
                    localResult = Result.retry()
            }//createActivityHistory
        }
        return localResult
    }
}