package com.example.onelook.data.local.supplementshistory

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface SupplementHistoryDao {

    @Query("SELECT * FROM supplements_history WHERE supplement_id=:supplementId")
    fun getSupplementsHistory(supplementId: UUID): Flow<List<LocalSupplementHistory>>

    @Query("SELECT * FROM supplements_history WHERE id=:id")
    fun getSupplementHistoryById(id: UUID): Flow<LocalSupplementHistory>

    @Query("SELECT * FROM supplements_history")
    fun getAllSupplementsHistory(): Flow<List<LocalSupplementHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplementHistory(supplementHistory: LocalSupplementHistory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplementsHistory(supplementHistory: List<LocalSupplementHistory>)

    @Update
    suspend fun updateSupplementHistory(supplementHistory: LocalSupplementHistory)

    @Delete
    suspend fun deleteSupplementHistory(supplementHistory: LocalSupplementHistory)
}
