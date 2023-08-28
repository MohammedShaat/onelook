package com.example.onelook.common.data.remote

import com.example.onelook.common.data.repository.AppStateRepositoryImpl
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeaderInterceptor @Inject constructor(
    private val appStateRepositoryImpl: AppStateRepositoryImpl
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = runBlocking{ appStateRepositoryImpl.getAccessToken() }
        val newRequest = chain.request().newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", "Bearer $accessToken")
            .build()
        return chain.proceed(newRequest)
    }
}