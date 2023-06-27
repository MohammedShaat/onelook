package com.example.onelook.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.onelook.data.Repository
import com.example.onelook.data.SharedData
import com.example.onelook.util.AlarmManagerHelper
import com.example.onelook.util.DATE_TIME_FORMAT
import com.example.onelook.util.toLocalModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted params: WorkerParameters,
    private val repository: Repository,
    private val alarmManagerHelper: AlarmManagerHelper
) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Timber.i("doWork()")

        val type = inputData.getString("type") ?: return Result.failure()
        val id = inputData.getString("id") ?: return Result.failure()

        if (type == "supplement") {
            val supplement = repository.getSupplements()
                .first().data?.firstOrNull { it.id == UUID.fromString(id) }
                ?: return Result.failure()

            alarmManagerHelper.setAlarmForSupplement(supplement.toLocalModel(), true)

        } else {
            val activity = repository.getActivities()
                .first().data?.firstOrNull { it.id == UUID.fromString(id) }
                ?: return Result.failure()

            alarmManagerHelper.setAlarmForActivity(activity.toLocalModel(), true)
        }

        return Result.success()
    }
}