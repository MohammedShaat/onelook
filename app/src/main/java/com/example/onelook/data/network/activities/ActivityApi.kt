package com.example.onelook.data.network.activities

import retrofit2.http.*
import java.util.*

interface ActivityApi {

    @GET("activities")
    suspend fun getActivities(): List<NetworkActivity>

    @GET("activities/{id}")
    suspend fun getActivityById(@Path("id") id: UUID): NetworkActivity

    @POST("activities/create")
    suspend fun createActivity(@Body activity: NetworkActivity)

    @POST("activities/bulkcreate")
    suspend fun createActivities(@Body activities: List<NetworkActivity>)

    @PUT("activities/update")
    suspend fun updateActivity(@Body activity: NetworkActivity)

    @DELETE("activities/delete/{id}")
    suspend fun deleteActivity(@Path("id") id: UUID)
}