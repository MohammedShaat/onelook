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
object AppModule {

    @Provides
    @Singleton
    fun provideSignInClient(@ApplicationContext context: Context): SignInClient {
        return Identity.getSignInClient(context)
    }

    @Provides
    @Singleton
    @Named("sign_up")
    fun provideSignInRequest(@ApplicationContext context: Context): BeginSignInRequest {
        return BeginSignInRequest.Builder()
            .setAutoSelectEnabled(true)
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(context.getString(R.string.server_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()
    }

    @Provides
    @Singleton
    @Named("login")
    fun provideSignInRequestWithFilter(@ApplicationContext context: Context): BeginSignInRequest {
        return BeginSignInRequest.Builder()
            .setAutoSelectEnabled(true)
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(context.getString(R.string.server_client_id))
                    .setFilterByAuthorizedAccounts(true)
                    .build()
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    @Named("user")
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(UserApi.BASE_URL)
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
    @Named("today_task")
    fun provideTodayTaskRetrofit(headerInterceptor: HeaderInterceptor): Retrofit {
        return Retrofit.Builder()
            .baseUrl(UserApi.BASE_URL)
            .client(OkHttpClient.Builder().addInterceptor(headerInterceptor).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideTodayTaskApi(@Named("today_task") retrofit: Retrofit): TodayTaskApi {
        return retrofit.create(TodayTaskApi::class.java)
    }

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