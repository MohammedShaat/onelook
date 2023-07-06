package com.example.onelook.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.onelook.data.AppState
import com.example.onelook.data.AppStateManager
import com.example.onelook.data.Repository
import com.example.onelook.util.CustomResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.last

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val appStateManager: AppStateManager,
    private val repository: Repository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (appStateManager.getAppState() != AppState.LOGGED_IN)
            return Result.failure()

        return sync()
    }

    private suspend fun sync(): Result {
        val refresh = repository.sync().last()
        return if (refresh is CustomResult.Success) Result.success()
        else Result.retry()
    }
}