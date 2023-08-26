package com.example.onelook.authentication.domain.repository

import com.example.onelook.common.util.Resource
import com.google.firebase.auth.AuthCredential

interface AuthenticationRepository {

    suspend fun signInWithEmailAndPassword(email: String, password: String): Resource<Unit>

    suspend fun signUpWithEmailAndPassword(email: String, password: String, name: String): Resource<Unit>

    suspend fun signInWithCredential(credential: AuthCredential): Resource<Unit>

    suspend fun signUpWithCredential(
        credential: AuthCredential,
        name: String? = null,
        email: String? = null
    ): Resource<Unit>

    suspend fun changePassword(): Resource<Unit>

    suspend fun signOut()
}