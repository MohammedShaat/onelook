package com.example.onelook.data.network.activitieshistory

import retrofit2.http.*
import java.util.*

interface ActivityHistoryApi {

    @GET("activities-history")
    suspend fun getActivitiesHistory(): List<NetworkActivityHistory>

    @GET("activities-history/{id}")
    suspend fun getActivityHistoryById(@Path("id") id: UUID): NetworkActivityHistory

    @POST("activities-history/create")
    suspend fun createActivityHistory(@Body activityHistory: NetworkActivityHistory)

    @POST("activities-history/bulkcreate")
    suspend fun createActivitiesHistory(@Body activitiesHistory: List<NetworkActivityHistory>)

    @PUT("activities-history/update")
    suspend fun updateActivityHistory(@Body activityHistory: NetworkActivityHistory)

    @DELETE("activities-history/delete/{id}")
    suspend fun deleteActivityHistory(@Path("id") id: UUID)
}