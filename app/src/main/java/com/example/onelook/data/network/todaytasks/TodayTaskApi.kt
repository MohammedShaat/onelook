package com.example.onelook.data.network.todaytasks

import retrofit2.http.GET

interface TodayTaskApi {

    companion object {
        const val BASE_URL = "https://onelook-api.fly.dev/api/"
    }

    @GET("today-tasks")
    suspend fun getTodayTasks(): List<NetworkTodayTask>
}