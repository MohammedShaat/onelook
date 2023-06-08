package com.example.onelook.di

import android.content.Context
import com.example.onelook.R
import com.example.onelook.data.network.HeaderInterceptor
import com.example.onelook.data.network.OneLookApi
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
    fun provideRetrofit(headerInterceptor: HeaderInterceptor): Retrofit {
        return Retrofit.Builder()
            .baseUrl(OneLookApi.BASE_URL)
            .client(OkHttpClient.Builder().addInterceptor(headerInterceptor).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideOneLookApi(retrofit: Retrofit): OneLookApi {
        return retrofit.create(OneLookApi::class.java)
    }
}