package com.example.onelook.tasks.doamin.repository

import com.example.onelook.tasks.data.local.SupplementHistoryEntity
import com.example.onelook.tasks.doamin.model.Supplement
import com.example.onelook.tasks.doamin.model.SupplementHistory
import com.example.onelook.common.util.Resource
import com.example.onelook.common.util.OperationSource
import kotlinx.coroutines.flow.Flow

interface SupplementHistoryRepository {

    fun createSupplementHistory(
        supplementHistoryEntity: SupplementHistoryEntity
    ): Flow<Resource<OperationSource>>

    fun getSupplementsHistory(supplement: Supplement): Flow<Resource<List<SupplementHistory>>>

    fun updateSupplementHistory(
        supplementHistoryEntity: SupplementHistoryEntity
    ): Flow<Resource<OperationSource>>
}