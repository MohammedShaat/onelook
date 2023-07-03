package com.example.onelook.data.local.notifications

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface NotificationDao {

    @Query("SELECT * FROM notifications ORDER BY created_at DESC")
    fun getNotifications(): Flow<List<LocalNotification>>

    @Query("SELECT * FROM notifications WHERE id=:id")
    fun getNotificationById(id: UUID): Flow<LocalNotification>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: LocalNotification)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<LocalNotification>)

    @Update
    suspend fun updateNotification(notification: LocalNotification)

    @Delete
    suspend fun deleteNotification(notification: LocalNotification)
}