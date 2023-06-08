package com.example.onelook.data.network

import com.example.onelook.data.network.models.NetworkSupplement
import com.example.onelook.data.network.requests.NetworkLoginAndDeleteUserRequestBody
import com.example.onelook.data.network.requests.NetworkRegisterRequestBody
import com.example.onelook.data.network.responses.NetworkDeleteUserResponse
import com.example.onelook.data.network.responses.NetworkRegisterAndLoginResponse
import com.example.onelook.data.network.responses.NetworkTodayTasksResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface OneLookApi {

    companion object {
        const val BASE_URL = "https://onelook-api.fly.dev/api/"
    }

    @POST("register")
    suspend fun register(@Body body: NetworkRegisterRequestBody): NetworkRegisterAndLoginResponse

    @POST("login")
    suspend fun login(@Body body: NetworkLoginAndDeleteUserRequestBody): NetworkRegisterAndLoginResponse

    @POST("delete-user")
    suspend fun deleteUser(@Body body: NetworkLoginAndDeleteUserRequestBody): NetworkDeleteUserResponse

    @GET("today-tasks")
    suspend fun getTodayTasks(): List<NetworkTodayTasksResponse>

    @GET("supplements")
    suspend fun getSupplements(): List<NetworkSupplement>
}