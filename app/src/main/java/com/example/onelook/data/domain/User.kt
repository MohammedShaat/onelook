package com.example.onelook.data.domain

data class User(
    val id: Int,
    val name: String,
    val firebaseUid: String,
    val accessToken: String,
    val updatedAt: String,
    val createdAt: String,
)
