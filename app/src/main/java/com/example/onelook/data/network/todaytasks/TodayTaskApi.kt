package com.example.onelook.data.network.todaytasks

import retrofit2.http.GET

interface TodayTaskApi {

    @GET("today-tasks")
    suspend fun getTodayTasks(): List<NetworkTodayTask>
}