package com.example.onelook.tasks.doamin.repository

import com.example.onelook.tasks.data.local.SupplementEntity
import com.example.onelook.tasks.doamin.model.Supplement
import com.example.onelook.common.util.Resource
import com.example.onelook.common.util.OperationSource
import kotlinx.coroutines.flow.Flow

interface SupplementRepository {

    fun createSupplement(supplementEntity: SupplementEntity): Flow<Resource<OperationSource>>

    fun updateSupplement(supplementEntity: SupplementEntity): Flow<Resource<OperationSource>>

    fun deleteSupplement(supplementEntity: SupplementEntity): Flow<Resource<OperationSource>>

    fun getSupplements(
        onForceRefreshFailed: suspend (Exception) -> Unit = {},
        forceRefresh: Boolean = false,
    ): Flow<Resource<List<Supplement>>>
}