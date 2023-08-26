package com.example.onelook.profile_and_settings.doamin.repository

import com.example.onelook.common.util.Resource

interface ProfileRepository {
    fun getName(): String?

    fun getEmail(): String?

    suspend fun changeName(name: String): Resource<Unit>

    suspend fun changePassword(): Resource<Unit>
}