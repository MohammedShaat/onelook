package com.example.onelook.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.onelook.data.local.activities.ActivityDao
import com.example.onelook.data.local.activities.LocalActivity
import com.example.onelook.data.local.activitieshistory.ActivityHistoryDao
import com.example.onelook.data.local.activitieshistory.LocalActivityHistory
import com.example.onelook.data.local.supplements.LocalSupplement
import com.example.onelook.data.local.supplements.SupplementDao
import com.example.onelook.data.local.supplementshistory.LocalSupplementHistory
import com.example.onelook.data.local.supplementshistory.SupplementHistoryDao
import com.example.onelook.data.local.todaytasks.TodayTaskDao
import com.example.onelook.data.local.users.LocalUser
import com.example.onelook.data.local.users.UserDao

@Database(
    entities = [
        LocalUser::class,
        LocalSupplement::class,
        LocalActivity::class,
        LocalSupplementHistory::class,
        LocalActivityHistory::class
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
}