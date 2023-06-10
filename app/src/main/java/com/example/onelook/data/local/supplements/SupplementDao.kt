package com.example.onelook.data.local.supplements

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface SupplementDao {

    @Query("SELECT * FROM supplements WHERE user_id=:userId")
    fun getSupplements(userId: Int): Flow<List<LocalSupplement>>

    @Query("SELECT * FROM supplements WHERE id=:id")
    fun getSupplementById(id: UUID): Flow<LocalSupplement>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplement(supplement: LocalSupplement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplements(supplement: List<LocalSupplement>)

    @Update
    suspend fun updateSupplement(supplement: LocalSupplement)

    @Delete
    suspend fun deleteSupplement(supplement: LocalSupplement)
}
