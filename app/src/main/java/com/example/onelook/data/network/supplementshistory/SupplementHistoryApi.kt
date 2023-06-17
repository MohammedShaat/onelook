package com.example.onelook.data.network.supplementshistory

import retrofit2.http.*
import java.util.*

interface SupplementHistoryApi {

    @GET("supplements-history")
    suspend fun getSupplementsHistory(): List<NetworkSupplementHistory>

    @GET("supplements-history/{id}")
    suspend fun getSupplementHistoryById(@Path("id") id: UUID): NetworkSupplementHistory

    @POST("supplements-history/create")
    suspend fun createSupplementHistory(@Body supplementHistory: NetworkSupplementHistory)

    @POST("supplements-history/bulkcreate")
    suspend fun createSupplementsHistory(@Body supplementsHistory: List<NetworkSupplementHistory>)

    @PUT("supplements-history/update")
    suspend fun updateSupplementHistory(@Body supplementHistory: NetworkSupplementHistory)

    @DELETE("supplements-history/delete/{id}")
    suspend fun deleteSupplementHistory(@Path("id") id: UUID)
}