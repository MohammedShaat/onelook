package com.example.onelook.di.appmodules

import android.content.Context
import androidx.room.Room
import com.example.onelook.data.local.OneLookDatabase
import com.example.onelook.data.local.activities.ActivityDao
import com.example.onelook.data.local.activitieshistory.ActivityHistoryDao
import com.example.onelook.data.local.supplements.SupplementDao
import com.example.onelook.data.local.supplementshistory.SupplementHistoryDao
import com.example.onelook.data.local.todaytasks.TodayTaskDao
import com.example.onelook.data.local.users.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideOneLookDatabase(@ApplicationContext context: Context): OneLookDatabase {
        synchronized(this) {
            return Room.databaseBuilder(
                context.applicationContext,
                OneLookDatabase::class.java,
                "OneLook.db"
            ).build()

        }
    }

    @Provides
    fun provideUserDao(db: OneLookDatabase): UserDao {
        return db.userDao
    }

    @Provides
    fun provideSupplementDao(db: OneLookDatabase): SupplementDao {
        return db.supplementDao
    }

    @Provides
    fun provideActivityDao(db: OneLookDatabase): ActivityDao {
        return db.activityDao
    }

    @Provides
    fun provideSupplementHistoryDao(db: OneLookDatabase): SupplementHistoryDao {
        return db.supplementHistoryDao
    }

    @Provides
    fun provideActivityHistoryDao(db: OneLookDatabase): ActivityHistoryDao {
        return db.activityHistoryDao
    }

    @Provides
    fun provideTodayTaskDao(db: OneLookDatabase): TodayTaskDao {
        return db.todayTaskDao
    }
}