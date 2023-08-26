package com.example.onelook.tasks.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface SupplementDao {

    @Query("SELECT * FROM supplements WHERE completed=0 ORDER BY created_at DESC")
    fun getSupplements(): Flow<List<SupplementEntity>>

    @Query("SELECT * FROM supplements WHERE id=:id")
    fun getSupplementById(id: UUID): Flow<SupplementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplement(supplement: SupplementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplements(supplement: List<SupplementEntity>)

    @Update
    suspend fun updateSupplement(supplement: SupplementEntity)

    @Delete
    suspend fun deleteSupplement(supplement: SupplementEntity)

    @Query("DELETE FROM supplements")
    suspend fun deleteAllSupplements()
}
