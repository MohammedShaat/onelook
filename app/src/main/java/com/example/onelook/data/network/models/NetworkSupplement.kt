package com.example.onelook.data.network.models

import com.google.gson.annotations.SerializedName

data class NetworkSupplement(
    val id: String,
    val name: String,
    val form: String,
    val dosage: Int,
    val frequency: String,
    val duration: String?,
    @SerializedName("time_of_day") val timeOfDay: String,
    @SerializedName("taking_with_meals") val takingWithMeals: String,
    val reminder: String,
    val completed: Boolean,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)