package com.example.onelook.data.network.users

import retrofit2.http.Body
import retrofit2.http.POST

interface UserApi {

    companion object {
        const val BASE_URL = "https://onelook-api.fly.dev/api/"
    }

    @POST("register")
    suspend fun register(@Body body: NetworkUserRegisterRequest): NetworkUserLoginResult

    @POST("login")
    suspend fun login(@Body body: NetworkUserLoginRequest): NetworkUserLoginResult

    @POST("delete-user")
    suspend fun deleteUser(@Body body: NetworkUserLoginRequest)
}