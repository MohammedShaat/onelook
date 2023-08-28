package com.example.onelook.authentication.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface UserApi {

    companion object {
        const val BASE_URL = "https://onelook-api.fly.dev/api/"
    }

    @POST("register")
    suspend fun register(@Body body: UserRegisterRequest): UserLoginResultDto

    @POST("login")
    suspend fun login(@Body body: UserLoginRequest): UserLoginResultDto

    @POST("delete-user")
    suspend fun deleteUser(@Body body: UserLoginRequest)
}