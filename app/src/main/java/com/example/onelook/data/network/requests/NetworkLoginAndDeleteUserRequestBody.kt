package com.example.onelook.data.network.requests

import com.google.gson.annotations.SerializedName

data class NetworkLoginAndDeleteUserRequestBody(
    @SerializedName("firebase_token")
    val firebaseToken: String,
)