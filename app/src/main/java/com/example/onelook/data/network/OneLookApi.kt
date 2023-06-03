package com.example.onelook.data.network

import com.example.onelook.data.network.requests.NetworkLoginAndDeleteUserRequest
import com.example.onelook.data.network.requests.NetworkRegisterRequest
import com.example.onelook.data.network.responses.NetworkDeleteUserResponse
import com.example.onelook.data.network.responses.NetworkRegisterAndLoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface OneLookApi {

    companion object {
        const val BASE_URL = "https://onelook-api.herokuapp.com/api/"
    }

    @POST("register")
    suspend fun register(@Body body: NetworkRegisterRequest): NetworkRegisterAndLoginResponse

    @POST("login")
    suspend fun login(@Body body: NetworkLoginAndDeleteUserRequest): NetworkRegisterAndLoginResponse
    @POST("delete-user")
    suspend fun deleteUser(@Body body: NetworkLoginAndDeleteUserRequest): NetworkDeleteUserResponse
}