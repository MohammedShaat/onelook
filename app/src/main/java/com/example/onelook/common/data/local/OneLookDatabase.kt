package com.example.onelook.common.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.onelook.tasks.data.local.ActivityDao
import com.example.onelook.tasks.data.local.ActivityEntity
import com.example.onelook.tasks.data.local.ActivityHistoryDao
import com.example.onelook.tasks.data.local.ActivityHistoryEntity
import com.example.onelook.notifications.data.local.NotificationEntity
import com.example.onelook.notifications.data.local.NotificationDao
import com.example.onelook.tasks.data.local.SupplementEntity
import com.example.onelook.tasks.data.local.SupplementDao
import com.example.onelook.tasks.data.local.SupplementHistoryEntity
import com.example.onelook.tasks.data.local.SupplementHistoryDao
import com.example.onelook.tasks.data.local.TodayTaskDao
import com.example.onelook.authentication.data.local.UserEntity
import com.example.onelook.authentication.data.local.UserDao

@Database(
    entities = [
        UserEntity::class,
        SupplementEntity::class,
        ActivityEntity::class,
        SupplementHistoryEntity::class,
        ActivityHistoryEntity::class,
        NotificationEntity::class
    ],
    version = 1
)
abstract class OneLookDatabase : RoomDatabase() {

    abstract val userDao: UserDao
    abstract val supplementDao: SupplementDao
    abstract val activityDao: ActivityDao
    abstract val supplementHistoryDao: SupplementHistoryDao
    abstract val activityHistoryDao: ActivityHistoryDao
    abstract val todayTaskDao: TodayTaskDao
    abstract val notificationDao: NotificationDao
}