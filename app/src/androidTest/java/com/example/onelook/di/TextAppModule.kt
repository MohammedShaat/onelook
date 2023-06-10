package com.example.onelook.di

import android.content.Context
import androidx.room.Room
import com.example.onelook.R
import com.example.onelook.data.local.OneLookDatabase
import com.example.onelook.data.local.activities.ActivityDao
import com.example.onelook.data.local.activitieshistory.ActivityHistoryDao
import com.example.onelook.data.local.supplements.SupplementDao
import com.example.onelook.data.local.supplementshistory.SupplementHistoryDao
import com.example.onelook.data.local.todaytasks.TodayTaskDao
import com.example.onelook.data.local.users.UserDao
import com.example.onelook.data.network.HeaderInterceptor
import com.example.onelook.data.network.todaytasks.TodayTaskApi
import com.example.onelook.data.network.users.UserApi
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TextAppModule {
    @Provides
    @Singleton
    @Named("test")
    fun provideOneLookDatabase(@ApplicationContext context: Context): OneLookDatabase {
        synchronized(this) {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                OneLookDatabase::class.java,
            ).build()
        }
    }

    @Provides
    @Named("test")
    fun provideUserDao(@Named("test") db: OneLookDatabase): UserDao {
        return db.userDao
    }

    @Provides
    @Named("test")
    fun provideSupplementDao(@Named("test") db: OneLookDatabase): SupplementDao {
        return db.supplementDao
    }

    @Provides
    @Named("test")
    fun provideActivityDao(@Named("test") db: OneLookDatabase): ActivityDao {
        return db.activityDao
    }

    @Provides
    @Named("test")
    fun provideSupplementHistoryDao(@Named("test") db: OneLookDatabase): SupplementHistoryDao {
        return db.supplementHistoryDao
    }

    @Provides
    @Named("test")
    fun provideActivityHistoryDao(@Named("test") db: OneLookDatabase): ActivityHistoryDao {
        return db.activityHistoryDao
    }

    @Provides
    @Named("test")
    fun provideTodayTaskDao(@Named("test") db: OneLookDatabase): TodayTaskDao {
        return db.todayTaskDao
    }
}