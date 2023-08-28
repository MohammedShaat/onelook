package com.example.onelook.authentication.di

import android.content.Context
import com.example.onelook.R
import com.example.onelook.authentication.data.local.UserDao
import com.example.onelook.authentication.data.remote.UserApi
import com.example.onelook.common.data.local.OneLookDatabase
import com.facebook.stetho.okhttp3.StethoInterceptor
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
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthenticationModuleProvider {

    @Provides
    @Singleton
    @Named("user")
    fun provideRetrofit(): Retrofit {
        val client = OkHttpClient.Builder()
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
    fun provideUserApi(@Named("user") retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)
    }

    @Provides
    fun provideUserDao(db: OneLookDatabase): UserDao {
        return db.userDao
    }

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
}