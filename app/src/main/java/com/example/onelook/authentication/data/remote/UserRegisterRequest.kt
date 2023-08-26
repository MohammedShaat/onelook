package com.example.onelook.authentication.data.remote

import com.google.gson.annotations.SerializedName

data class UserRegisterRequest(
    @SerializedName("firebase_token")
    val firebaseToken: String,
    val name: String
)