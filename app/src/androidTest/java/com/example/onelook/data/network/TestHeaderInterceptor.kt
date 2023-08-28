package com.example.onelook.data.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestHeaderInterceptor @Inject constructor() : Interceptor {

    companion object {
        var accessToken = "32|ueu5lcYdR8S7Nj4dE95yj0voKc3NI05jpqVunvZQ"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest = chain.request().newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", "Bearer $accessToken")
            .build()
        return chain.proceed(newRequest)
    }
}