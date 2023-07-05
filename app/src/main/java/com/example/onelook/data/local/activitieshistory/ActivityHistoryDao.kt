package com.example.onelook.data.local.activitieshistory

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface ActivityHistoryDao {

    @Query("SELECT * FROM activities_history WHERE activity_id=:activityId ORDER BY created_at DESC")
    fun getActivitiesHistory(activityId: UUID): Flow<List<LocalActivityHistory>>

    @Query("SELECT * FROM activities_history WHERE id=:id")
    fun getActivityHistoryById(id: UUID): Flow<LocalActivityHistory>

    @Query("SELECT * FROM activities_history ORDER BY created_at DESC")
    fun getAllActivitiesHistory(): Flow<List<LocalActivityHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityHistory(activityHistory: LocalActivityHistory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivitiesHistory(activityHistory: List<LocalActivityHistory>)

    @Update
    suspend fun updateActivityHistory(activityHistory: LocalActivityHistory)

    @Delete
    suspend fun deleteActivityHistory(activityHistory: LocalActivityHistory)

    @Query("DELETE FROM activities_history")
    suspend fun deleteAllActivitiesHistory()
}
