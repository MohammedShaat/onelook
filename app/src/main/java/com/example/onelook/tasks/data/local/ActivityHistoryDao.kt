package com.example.onelook.tasks.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface ActivityHistoryDao {

    @Query("SELECT * FROM activities_history WHERE activity_id=:activityId ORDER BY created_at DESC")
    fun getActivitiesHistory(activityId: UUID): Flow<List<ActivityHistoryEntity>>

    @Query("SELECT * FROM activities_history WHERE id=:id")
    fun getActivityHistoryById(id: UUID): Flow<ActivityHistoryEntity>

    @Query("SELECT * FROM activities_history ORDER BY created_at DESC")
    fun getAllActivitiesHistory(): Flow<List<ActivityHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityHistory(activityHistory: ActivityHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivitiesHistory(activityHistory: List<ActivityHistoryEntity>)

    @Update
    suspend fun updateActivityHistory(activityHistory: ActivityHistoryEntity)

    @Delete
    suspend fun deleteActivityHistory(activityHistory: ActivityHistoryEntity)

    @Query("DELETE FROM activities_history")
    suspend fun deleteAllActivitiesHistory()
}
