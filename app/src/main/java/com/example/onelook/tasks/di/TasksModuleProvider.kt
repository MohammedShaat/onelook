package com.example.onelook.tasks.di

import android.content.Context
import androidx.room.Room
import com.example.onelook.authentication.data.remote.UserApi
import com.example.onelook.common.data.local.OneLookDatabase
import com.example.onelook.common.data.remote.HeaderInterceptor
import com.example.onelook.tasks.data.local.ActivityDao
import com.example.onelook.tasks.data.local.ActivityHistoryDao
import com.example.onelook.tasks.data.local.SupplementDao
import com.example.onelook.tasks.data.local.SupplementHistoryDao
import com.example.onelook.tasks.data.local.TodayTaskDao
import com.example.onelook.tasks.data.remote.ActivityApi
import com.example.onelook.tasks.data.remote.ActivityHistoryApi
import com.example.onelook.tasks.data.remote.SupplementApi
import com.example.onelook.tasks.data.remote.SupplementHistoryApi
import com.example.onelook.tasks.data.remote.TodayTaskApi
import com.facebook.stetho.okhttp3.StethoInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TasksModuleProvider {

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

    @Provides
    @Singleton
    @Named("headers")
    fun provideTodayTaskRetrofit(headerInterceptor: HeaderInterceptor): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .connectTimeout(0, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .writeTimeout(0, TimeUnit.SECONDS)
            .addNetworkInterceptor(StethoInterceptor())
            .build()

        return Retrofit.Builder()
            .baseUrl(UserApi.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideTodayTaskApi(@Named("headers") retrofit: Retrofit): TodayTaskApi {
        return retrofit.create(TodayTaskApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSupplementApi(@Named("headers") retrofit: Retrofit): SupplementApi {
        return retrofit.create(SupplementApi::class.java)
    }

    @Provides
    @Singleton
    fun provideActivityApi(@Named("headers") retrofit: Retrofit): ActivityApi {
        return retrofit.create(ActivityApi::class.java)
    }

    @Provides
    @Singleton
    fun provideActivityHistoryApi(@Named("headers") retrofit: Retrofit): ActivityHistoryApi {
        return retrofit.create(ActivityHistoryApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSupplementHistoryApi(@Named("headers") retrofit: Retrofit): SupplementHistoryApi {
        return retrofit.create(SupplementHistoryApi::class.java)
    }
}