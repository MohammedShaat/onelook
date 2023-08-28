package com.example.onelook.tasks.data.remote

import retrofit2.http.*
import java.util.*

interface SupplementHistoryApi {

    @GET("supplements-history")
    suspend fun getSupplementsHistory(): List<SupplementHistoryDto>

    @GET("supplements-history/{id}")
    suspend fun getSupplementHistoryById(@Path("id") id: UUID): SupplementHistoryDto

    @POST("supplements-history/create")
    suspend fun createSupplementHistory(@Body supplementHistory: SupplementHistoryDto)

    @POST("supplements-history/bulkcreate")
    suspend fun createSupplementsHistory(@Body supplementsHistory: List<SupplementHistoryDto>)

    @PUT("supplements-history/update")
    suspend fun updateSupplementHistory(@Body supplementHistory: SupplementHistoryDto)

    @DELETE("supplements-history/delete/{id}")
    suspend fun deleteSupplementHistory(@Path("id") id: UUID)
}