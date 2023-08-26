package com.example.onelook.profile_and_settings.data.repository

import com.example.onelook.authentication.domain.repository.AuthenticationRepository
import com.example.onelook.profile_and_settings.doamin.repository.ProfileRepository
import com.example.onelook.common.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val authenticationRepository: AuthenticationRepository,
) : ProfileRepository {
    override fun getName(): String? {
        return auth.currentUser?.displayName
    }

    override fun getEmail(): String? {
        return auth.currentUser?.email
    }

    override suspend fun changeName(name: String): Resource<Unit> {
        return try {
            val request = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            auth.currentUser!!.updateProfile(request).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e)
            Resource.Failure(e)
        }
    }

    override suspend fun changePassword(): Resource<Unit> {
        return authenticationRepository.changePassword()
    }
}