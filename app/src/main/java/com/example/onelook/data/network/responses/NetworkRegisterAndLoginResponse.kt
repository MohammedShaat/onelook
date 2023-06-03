package com.example.onelook.data.network.responses

import com.google.gson.annotations.SerializedName

data class NetworkRegisterAndLoginResponse(
    @SerializedName("user") val user: User,
    @SerializedName("access_token") val accessToken: String
) {
    data class User(
        val name: String,
        val firebase_uid: String,
        @SerializedName("updated_at")
        val updatedAt: String,
        @SerializedName("created_at")
        val createdAt: String,
        val id: String,
    )
}
