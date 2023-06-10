package com.example.onelook.data.network.users

import com.google.gson.annotations.SerializedName


data class NetworkUserRegisterRequest(
    @SerializedName("firebase_token")
    val firebaseToken: String,
    val name: String
)

data class NetworkUserLoginRequest(
    @SerializedName("firebase_token")
    val firebaseToken: String,
)

data class NetworkUserLoginResult(
    @SerializedName("user") val user: NetworkUser,
    @SerializedName("access_token") val accessToken: String
) {
    data class NetworkUser(
        val id: Int,
        val name: String,
        val firebase_uid: String,
        @SerializedName("updated_at") val updatedAt: String,
        @SerializedName("created_at") val createdAt: String,
    )
}