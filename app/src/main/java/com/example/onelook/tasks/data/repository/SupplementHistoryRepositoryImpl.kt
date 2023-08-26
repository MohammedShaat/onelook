package com.example.onelook.tasks.data.repository

import com.example.onelook.tasks.data.local.SupplementHistoryDao
import com.example.onelook.tasks.data.local.SupplementHistoryEntity
import com.example.onelook.tasks.data.mapper.toSupplementHistory
import com.example.onelook.tasks.data.mapper.toSupplementHistoryDto
import com.example.onelook.tasks.data.remote.SupplementHistoryApi
import com.example.onelook.tasks.doamin.model.Supplement
import com.example.onelook.tasks.doamin.repository.SupplementHistoryRepository
import com.example.onelook.common.util.Resource
import com.example.onelook.common.util.OperationSource
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupplementHistoryRepositoryImpl @Inject constructor(
    private val supplementHistoryDao: SupplementHistoryDao,
    private val supplementHistoryApi: SupplementHistoryApi,
) : SupplementHistoryRepository {
    override fun createSupplementHistory(supplementHistoryEntity: SupplementHistoryEntity) = flow {
        emit(Resource.Loading())
        supplementHistoryDao.insertSupplementHistory(supplementHistoryEntity)

        try {
            supplementHistoryApi.createSupplementHistory(supplementHistoryEntity.toSupplementHistoryDto())
            Timber.i("SupplementHistory created locally and remotely")
            emit(Resource.Success(OperationSource.LOCAL_AND_REMOTE))
        } catch (exception: Exception) {
            Timber.e("SupplementHistory created locally only\n$exception")
            emit(Resource.Success(OperationSource.LOCAL_ONLY))
        }
    }

    override fun getSupplementsHistory(supplement: Supplement) = flow {
        supplementHistoryDao.getSupplementsHistory(supplement.id).collect { list ->
            emit(Resource.Success(list.map { it.toSupplementHistory(supplement) }))
        }
    }

    override fun updateSupplementHistory(supplementHistoryEntity: SupplementHistoryEntity) = flow {
        emit(Resource.Loading())
        supplementHistoryDao.updateSupplementHistory(supplementHistoryEntity)

        try {
            supplementHistoryApi.updateSupplementHistory(supplementHistoryEntity.toSupplementHistoryDto())
            Timber.i("SupplementHistory updated locally and remotely")
            emit(Resource.Success(OperationSource.LOCAL_AND_REMOTE))
        } catch (exception: Exception) {
            Timber.e("SupplementHistory updated locally only\n$exception")
            emit(Resource.Success(OperationSource.LOCAL_ONLY))
        }
    }
}