package com.example.onelook.tasks.data.remote

import retrofit2.http.*
import java.util.*

interface ActivityApi {

    @GET("activities")
    suspend fun getActivities(): List<ActivityDto>

    @GET("activities/{id}")
    suspend fun getActivityById(@Path("id") id: UUID): ActivityDto

    @POST("activities/create")
    suspend fun createActivity(@Body activity: ActivityDto)

    @POST("activities/bulkcreate")
    suspend fun createActivities(@Body activities: List<ActivityDto>)

    @PUT("activities/update")
    suspend fun updateActivity(@Body activity: ActivityDto)

    @DELETE("activities/delete/{id}")
    suspend fun deleteActivity(@Path("id") id: UUID)
}