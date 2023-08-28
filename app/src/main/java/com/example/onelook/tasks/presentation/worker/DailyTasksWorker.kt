package com.example.onelook.tasks.presentation.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.onelook.common.data.repository.AppState
import com.example.onelook.common.data.repository.AppStateRepositoryImpl
import com.example.onelook.tasks.data.local.ActivityHistoryEntity
import com.example.onelook.tasks.data.local.SupplementHistoryEntity
import com.example.onelook.tasks.doamin.repository.ActivityRepository
import com.example.onelook.tasks.doamin.repository.SupplementRepository
import com.example.onelook.tasks.doamin.repository.TodayTasksRepository
import com.example.onelook.common.util.AlarmManagerHelper
import com.example.onelook.common.util.Resource
import com.example.onelook.common.util.OperationSource
import com.example.onelook.common.util.format
import com.example.onelook.common.util.isExpired
import com.example.onelook.common.util.isToday
import com.example.onelook.common.util.parseDate
import com.example.onelook.tasks.data.mapper.toSupplementEntity
import com.example.onelook.tasks.doamin.repository.ActivityHistoryRepository
import com.example.onelook.tasks.doamin.repository.SupplementHistoryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import timber.log.Timber
import java.util.Date
import java.util.UUID

@HiltWorker
class DailyTasksWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val appStateRepositoryImpl: AppStateRepositoryImpl,
    private val activityRepository: ActivityRepository,
    private val supplementRepository: SupplementRepository,
    private val activityHistoryRepository: ActivityHistoryRepository,
    private val supplementHistoryRepository: SupplementHistoryRepository,
    private val todayTasksRepository: TodayTasksRepository,
    private val alarmManagerHelper: AlarmManagerHelper,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Timber.i("doWork()")

        // Schedule next worker
        alarmManagerHelper.setAlarmForDailyTasksReceiver(delayHours = 24)

        // Exit if user not logged in
        if (appStateRepositoryImpl.getAppState() != AppState.LOGGED_IN)
            return Result.failure()

        appStateRepositoryImpl.setLastDailyTasksWorkerDate(Date().format)

        val result1 = sync()
        val result2 = createTodaySupplementsHistoryAndCheckCompletion()
        val result3 = createTodayActivitiesHistory()

        return if (Result.retry() in listOf(result1, result2, result3)) Result.retry()
        else Result.success()
    }

    private suspend fun sync(): Result {
        val refresh = todayTasksRepository.sync().last()
        return if (refresh is Resource.Success) Result.success()
        else Result.retry()
    }


    private suspend fun createTodaySupplementsHistoryAndCheckCompletion(): Result {
        val supplements =
            supplementRepository.getSupplements().first().data ?: return Result.retry()
        var localResult = Result.success()
        supplements.forEach { supplement ->
            // Completes expired supplements
            if (isExpired(supplement.createdAt.parseDate, supplement.duration)) {
                val updatedSupplement = supplement.copy(completed = true).toSupplementEntity()
                supplementRepository.updateSupplement(updatedSupplement).collect { result ->
                    if (result is Resource.Success && result.data == OperationSource.LOCAL_ONLY)
                        localResult = Result.retry()
                }//updateSupplement
                return@forEach
            }//isExpired

            // Returns if supplementHistory already exists
            val supplementHistory =
                supplementHistoryRepository.getSupplementsHistory(supplement).first().data
                    ?.firstOrNull()?.takeIf { it.createdAt.parseDate.isToday }
            if (supplementHistory != null)
                return@forEach

            val formattedDate = Date().format
            val supplementHistoryEntity = SupplementHistoryEntity(
                id = UUID.randomUUID(),
                supplementId = supplement.id,
                progress = 0,
                completed = false,
                createdAt = formattedDate,
                updatedAt = formattedDate
            )
            supplementHistoryRepository.createSupplementHistory(supplementHistoryEntity)
                .collect { result ->
                    if (result is Resource.Success && result.data == OperationSource.LOCAL_ONLY)
                        localResult = Result.retry()
                }//createSupplementHistory
        }
        return localResult
    }

    private suspend fun createTodayActivitiesHistory(): Result {
        val activities = activityRepository.getActivities().first().data ?: return Result.retry()
        var localResult = Result.success()
        activities.forEach { activity ->
            // Returns if activityHistory already exists
            val activityHistory =
                activityHistoryRepository.getActivitiesHistory(activity).first().data
                    ?.firstOrNull()?.takeIf { it.createdAt.parseDate.isToday }
            if (activityHistory != null)
                return@forEach

            val formattedDate = Date().format
            val activityHistoryEntity = ActivityHistoryEntity(
                id = UUID.randomUUID(),
                activityId = activity.id,
                progress = "00:00",
                completed = false,
                createdAt = formattedDate,
                updatedAt = formattedDate
            )
            activityHistoryRepository.createActivityHistory(activityHistoryEntity)
                .collect { result ->
                    if (result is Resource.Success && result.data == OperationSource.LOCAL_ONLY)
                        localResult = Result.retry()
                }//createActivityHistory
        }
        return localResult
    }
}