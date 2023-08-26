package com.example.onelook.tasks.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface ActivityDao {

    @Query("SELECT * FROM activities ORDER BY created_at DESC")
    fun getActivities(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE id=:id")
    fun getActivityById(id: UUID): Flow<ActivityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activity: List<ActivityEntity>)

    @Update
    suspend fun updateActivity(activity: ActivityEntity)

    @Delete
    suspend fun deleteActivity(activity: ActivityEntity)

    @Query("DELETE FROM activities")
    suspend fun deleteAllActivities()
}
