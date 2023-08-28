package com.example.onelook.authentication.data.mapper

import com.example.onelook.authentication.data.local.UserEntity
import com.example.onelook.authentication.data.remote.UserDto
import com.example.onelook.authentication.domain.model.User

fun UserDto.toUserEntity(): UserEntity {
    return UserEntity(
        id = id,
        name = name,
        firebaseUid = firebaseUid,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
}

fun UserDto.toUser(accessToken: String): User {
    return User(
        id = id,
        name = name,
        firebaseUid = firebaseUid,
        updatedAt = updatedAt,
        createdAt = createdAt,
        accessToken = accessToken
    )
}