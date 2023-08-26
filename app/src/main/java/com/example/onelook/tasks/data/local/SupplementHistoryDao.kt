package com.example.onelook.tasks.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface SupplementHistoryDao {

    @Query("SELECT * FROM supplements_history WHERE supplement_id=:supplementId ORDER BY created_at DESC")
    fun getSupplementsHistory(supplementId: UUID): Flow<List<SupplementHistoryEntity>>

    @Query("SELECT * FROM supplements_history WHERE id=:id")
    fun getSupplementHistoryById(id: UUID): Flow<SupplementHistoryEntity>

    @Query("SELECT * FROM supplements_history ORDER BY created_at DESC")
    fun getAllSupplementsHistory(): Flow<List<SupplementHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplementHistory(supplementHistory: SupplementHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplementsHistory(supplementHistory: List<SupplementHistoryEntity>)

    @Update
    suspend fun updateSupplementHistory(supplementHistory: SupplementHistoryEntity)

    @Delete
    suspend fun deleteSupplementHistory(supplementHistory: SupplementHistoryEntity)

    @Query("DELETE FROM supplements_history")
    suspend fun deleteAllSupplementsHistory()
}
