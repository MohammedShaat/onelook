package com.example.onelook.data.network.supplements

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.UUID

interface SupplementApi {

    @GET("supplements")
    suspend fun getSupplements(): List<NetworkSupplement>

    @GET("supplements/{id}")
    suspend fun getSupplementById(@Path("id") id: UUID): NetworkSupplement

    @POST("supplements/create")
    suspend fun createSupplement(@Body supplement: NetworkSupplement)

    @POST("supplements/bulkcreate")
    suspend fun createSupplements(@Body supplements: List<NetworkSupplement>)

    @PUT("supplements/update")
    suspend fun updateSupplement(@Body supplement: NetworkSupplement): NetworkSupplement

    @DELETE("supplements/delete/{id}")
    suspend fun deleteSupplement(@Path("id") id: UUID)
}