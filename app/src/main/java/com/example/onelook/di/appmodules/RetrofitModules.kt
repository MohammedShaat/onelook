package com.example.onelook.di.appmodules

import com.example.onelook.data.network.HeaderInterceptor
import com.example.onelook.data.network.activities.ActivityApi
import com.example.onelook.data.network.activitieshistory.ActivityHistoryApi
import com.example.onelook.data.network.supplements.SupplementApi
import com.example.onelook.data.network.supplementshistory.SupplementHistoryApi
import com.example.onelook.data.network.todaytasks.TodayTaskApi
import com.example.onelook.data.network.users.UserApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModules {

    @Provides
    @Singleton
    @Named("user")
    fun provideRetrofit(): Retrofit {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(UserApi.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideUserApi(@Named("user") retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)
    }

    @Provides
    @Singleton
    @Named("headers")
    fun provideTodayTaskRetrofit(headerInterceptor: HeaderInterceptor): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
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