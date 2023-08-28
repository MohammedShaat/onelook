package com.example.onelook.tasks.data.remote

import retrofit2.http.GET

interface TodayTaskApi {

    @GET("today-tasks")
    suspend fun getTodayTasks(): List<TodayTaskDto>
}