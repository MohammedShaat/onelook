package com.example.onelook.tasks.data.remote

import retrofit2.http.*
import java.util.*

interface ActivityHistoryApi {

    @GET("activities-history")
    suspend fun getActivitiesHistory(): List<ActivityHistoryDto>

    @GET("activities-history/{id}")
    suspend fun getActivityHistoryById(@Path("id") id: UUID): ActivityHistoryDto

    @POST("activities-history/create")
    suspend fun createActivityHistory(@Body activityHistory: ActivityHistoryDto)

    @POST("activities-history/bulkcreate")
    suspend fun createActivitiesHistory(@Body activitiesHistory: List<ActivityHistoryDto>)

    @PUT("activities-history/update")
    suspend fun updateActivityHistory(@Body activityHistory: ActivityHistoryDto)

    @DELETE("activities-history/delete/{id}")
    suspend fun deleteActivityHistory(@Path("id") id: UUID)
}