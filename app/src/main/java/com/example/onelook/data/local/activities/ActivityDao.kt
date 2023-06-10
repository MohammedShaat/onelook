package com.example.onelook.data.local.activities

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface ActivityDao {

    @Query("SELECT * FROM activities WHERE user_id=:userId")
    fun getActivities(userId: Int): Flow<List<LocalActivity>>

    @Query("SELECT * FROM activities WHERE id=:id")
    fun getActivityById(id: UUID): Flow<LocalActivity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: LocalActivity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activity: List<LocalActivity>)

    @Update
    suspend fun updateActivity(activity: LocalActivity)

    @Delete
    suspend fun deleteActivity(activity: LocalActivity)
}
