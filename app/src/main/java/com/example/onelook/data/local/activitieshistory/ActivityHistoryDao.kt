package com.example.onelook.data.local.activitieshistory

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface ActivityHistoryDao {

    @Query("SELECT * FROM activities_history WHERE activity_id=:activityId")
    fun getActivitiesHistory(activityId: UUID): Flow<List<LocalActivityHistory>>

    @Query("SELECT * FROM activities_history WHERE id=:id")
    fun getActivityHistoryById(id: UUID): Flow<LocalActivityHistory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityHistory(activityHistory: LocalActivityHistory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivitiesHistory(activityHistory: List<LocalActivityHistory>)

    @Update
    suspend fun updateActivityHistory(activityHistory: LocalActivityHistory)

    @Delete
    suspend fun deleteActivityHistory(activityHistory: LocalActivityHistory)
}
