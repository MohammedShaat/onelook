package com.example.onelook.di.appmodules

import com.example.onelook.data.network.HeaderInterceptor
import com.example.onelook.data.network.TestHeaderInterceptor
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
object TestRetrofitModules {

    @Provides
    @Singleton
    @Named("test_headers")
    fun provideSupplementRetrofit(testHeaderInterceptor: TestHeaderInterceptor): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(testHeaderInterceptor)
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
    @Named("test")
    fun provideSupplementApi(@Named("test_headers") retrofit: Retrofit): SupplementApi {
        return retrofit.create(SupplementApi::class.java)
    }

    @Provides
    @Singleton
    @Named("test")
    fun provideActivityApi(@Named("test_headers") retrofit: Retrofit): ActivityApi {
        return retrofit.create(ActivityApi::class.java)
    }

    @Provides
    @Singleton
    @Named("test")
    fun provideActivityHistoryApi(@Named("test_headers") retrofit: Retrofit): ActivityHistoryApi {
        return retrofit.create(ActivityHistoryApi::class.java)
    }

    @Provides
    @Singleton
    @Named("test")
    fun provideSupplementHistoryApi(@Named("test_headers") retrofit: Retrofit): SupplementHistoryApi {
        return retrofit.create(SupplementHistoryApi::class.java)
    }
}