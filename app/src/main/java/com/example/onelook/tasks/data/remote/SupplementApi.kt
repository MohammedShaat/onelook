package com.example.onelook.tasks.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.UUID

interface SupplementApi {

    @GET("supplements")
    suspend fun getSupplements(): List<SupplementDto>

    @GET("supplements/{id}")
    suspend fun getSupplementById(@Path("id") id: UUID): SupplementDto

    @POST("supplements/create")
    suspend fun createSupplement(@Body supplement: SupplementDto)

    @POST("supplements/bulkcreate")
    suspend fun createSupplements(@Body supplements: List<SupplementDto>)

    @PUT("supplements/update")
    suspend fun updateSupplement(@Body supplement: SupplementDto)

    @DELETE("supplements/delete/{id}")
    suspend fun deleteSupplement(@Path("id") id: UUID)
}