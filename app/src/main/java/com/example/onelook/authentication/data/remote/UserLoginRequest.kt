package com.example.onelook.authentication.data.remote

import com.google.gson.annotations.SerializedName

data class UserLoginRequest(
    @SerializedName("firebase_token")
    val firebaseToken: String,
)