package com.example.onelook.tasks.data.repository

import com.example.onelook.tasks.data.local.SupplementDao
import com.example.onelook.tasks.data.local.SupplementEntity
import com.example.onelook.tasks.data.local.SupplementHistoryDao
import com.example.onelook.tasks.data.local.SupplementHistoryEntity
import com.example.onelook.tasks.data.remote.SupplementApi
import com.example.onelook.tasks.data.remote.SupplementHistoryApi
import com.example.onelook.tasks.doamin.repository.SupplementRepository
import com.example.onelook.tasks.doamin.repository.TodayTasksRepository
import com.example.onelook.common.util.AlarmManagerHelper
import com.example.onelook.common.util.Resource
import com.example.onelook.common.util.OperationSource
import com.example.onelook.tasks.data.mapper.toSupplement
import com.example.onelook.tasks.data.mapper.toSupplementHistoryDto
import com.example.onelook.tasks.data.mapper.toSupplementDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupplementRepositoryImpl @Inject constructor(
    private val supplementDao: SupplementDao,
    private val supplementApi: SupplementApi,
    private val supplementHistoryDao: SupplementHistoryDao,
    private val supplementHistoryApi: SupplementHistoryApi,
    private val todayTasksRepository: TodayTasksRepository,
    private val alarmManagerHelper: AlarmManagerHelper,
) : SupplementRepository {
    override fun createSupplement(supplementEntity: SupplementEntity) = flow {
        emit(Resource.Loading())
        supplementDao.insertSupplement(supplementEntity)
        val supplementHistoryEntity = SupplementHistoryEntity(
            id = UUID.randomUUID(),
            supplementId = supplementEntity.id,
            progress = 0,
            completed = false,
            createdAt = supplementEntity.createdAt,
            updatedAt = supplementEntity.updatedAt,
        )
        supplementHistoryDao.insertSupplementHistory(supplementHistoryEntity)

        try {
            supplementApi.createSupplement(supplementEntity.toSupplementDto())
            supplementHistoryApi.createSupplementHistory(supplementHistoryEntity.toSupplementHistoryDto())
            Timber.i("Supplement and SupplementHistory created locally and remotely")
            emit(Resource.Success(OperationSource.LOCAL_AND_REMOTE))
        } catch (exception: Exception) {
            Timber.e("Supplement and SupplementHistory created locally only\n$exception")
            emit(Resource.Success(OperationSource.LOCAL_ONLY))
        }

        alarmManagerHelper.setAlarm(supplementEntity)
    }

    override fun updateSupplement(supplementEntity: SupplementEntity) = flow {
        emit(Resource.Loading())
        supplementDao.updateSupplement(supplementEntity)

        supplementHistoryDao.getSupplementsHistory(supplementEntity.id).first().firstOrNull()
            ?.let { localSupplementHistory ->
                val newProgress =
                    Integer.min(localSupplementHistory.progress, supplementEntity.dosage)
                supplementHistoryDao.updateSupplementHistory(
                    localSupplementHistory.copy(
                        progress = newProgress,
                        completed = newProgress == supplementEntity.dosage
                    )
                )
            }

        try {
            supplementApi.updateSupplement(supplementEntity.toSupplementDto())
            Timber.i("Supplement updated locally and remotely")
            emit(Resource.Success(OperationSource.LOCAL_AND_REMOTE))
        } catch (exception: Exception) {
            Timber.e("Supplement updated locally only\n$exception")
            emit(Resource.Success(OperationSource.LOCAL_ONLY))
        }

        alarmManagerHelper.setAlarm(supplementEntity)
    }

    override fun deleteSupplement(supplementEntity: SupplementEntity) = flow {
        emit(Resource.Loading())
        supplementDao.deleteSupplement(supplementEntity)

        supplementHistoryDao.getSupplementsHistory(supplementEntity.id).first()
            .forEach { localActivityHistory ->
                supplementHistoryDao.deleteSupplementHistory(localActivityHistory)
            }

        try {
            supplementApi.deleteSupplement(supplementEntity.id)
            Timber.i("Supplement deleted locally and remotely")
            emit(Resource.Success(OperationSource.LOCAL_AND_REMOTE))
        } catch (exception: Exception) {
            Timber.e("Supplement deleted locally only\n$exception")
            emit(Resource.Success(OperationSource.LOCAL_ONLY))
        }

        alarmManagerHelper.cancelAlarm(supplementEntity)
    }

    override fun getSupplements(
        onForceRefreshFailed: suspend (Exception) -> Unit,
        forceRefresh: Boolean
    ) = flow {
        val supplements = supplementDao.getSupplements()

        if (forceRefresh) {
            val refresh = todayTasksRepository.sync().last()
            if (refresh is Resource.Success) {
                supplements.collect { list ->
                    emit(Resource.Success(list.map { it.toSupplement() }))
                }
            } else if (refresh is Resource.Failure) {
                Timber.e(refresh.exception)
                onForceRefreshFailed(refresh.exception!!)
                supplements.collect { list ->
                    emit(Resource.Failure(refresh.exception, list.map { it.toSupplement() }))
                }
            }
        } else {
            supplements.collect { list ->
                emit(Resource.Success(list.map { it.toSupplement() }))
            }
        }
    }
}