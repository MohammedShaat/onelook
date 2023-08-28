package com.example.onelook.authentication.data.remote

import com.google.gson.annotations.SerializedName

data class UserLoginResultDto(
    @SerializedName("user") val user: UserDto,
    @SerializedName("access_token") val accessToken: String
)