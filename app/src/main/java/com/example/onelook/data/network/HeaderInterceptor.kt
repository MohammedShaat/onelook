package com.example.onelook.data.network

import com.example.onelook.data.AppStateManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class HeaderInterceptor @Inject constructor(
    private val appStateManager: AppStateManager
) : Interceptor {

    companion object {
        private val segmentsWithoutHeaders = listOf("login", "register", "delete-user")
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        return if (isNotModifiable(chain.request().url.pathSegments)) {
            chain.proceed(chain.request())
        } else {
            val accessToken = getAccessToken()
            val newRequest = chain.request().newBuilder()
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
            chain.proceed(newRequest)
        }
    }

    private fun isNotModifiable(pathSegments: List<String>): Boolean {
        return segmentsWithoutHeaders.any { segment ->
            segment in pathSegments
        }
    }

    private fun getAccessToken(): String {
        return runBlocking { appStateManager.getAccessToken() }
    }
}