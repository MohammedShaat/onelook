package com.example.onelook.data.network.responses

import com.example.onelook.data.network.models.NetworkUser
import com.google.gson.annotations.SerializedName

data class NetworkRegisterAndLoginResponse(
    @SerializedName("user") val user: NetworkUser,
    @SerializedName("access_token") val accessToken: String
)
