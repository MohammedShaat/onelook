package com.example.onelook.authentication.data.repository

import com.example.onelook.authentication.data.local.UserDao
import com.example.onelook.authentication.data.mapper.toUserEntity
import com.example.onelook.authentication.data.remote.UserApi
import com.example.onelook.authentication.data.remote.UserLoginRequest
import com.example.onelook.authentication.data.remote.UserRegisterRequest
import com.example.onelook.authentication.domain.repository.AuthenticationRepository
import com.example.onelook.common.data.repository.AppState
import com.example.onelook.common.data.repository.AppStateRepositoryImpl
import com.example.onelook.tasks.data.local.ActivityDao
import com.example.onelook.tasks.data.local.ActivityHistoryDao
import com.example.onelook.tasks.data.local.SupplementDao
import com.example.onelook.tasks.data.local.SupplementHistoryDao
import com.example.onelook.common.di.ApplicationCoroutine
import com.example.onelook.common.util.Resource
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.annotation.Signed
import javax.inject.Inject

@Signed
class AuthenticationRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val userDao: UserDao,
    private val activityDao: ActivityDao,
    private val supplementDao: SupplementDao,
    private val activityHistoryDao: ActivityHistoryDao,
    private val supplementHistoryDao: SupplementHistoryDao,
    val auth: FirebaseAuth,
    private val appStateRepositoryImpl: AppStateRepositoryImpl,
    @ApplicationCoroutine private val applicationCoroutine: CoroutineScope
) : AuthenticationRepository {

    override suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Resource<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            getAccessTokenAndSavedIt(SignType.SIGN_IN)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }

    override suspend fun signUpWithEmailAndPassword(
        email: String,
        password: String,
        name: String
    ): Resource<Unit> {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            getAccessTokenAndSavedIt(SignType.SIGN_UP, name)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }

    override suspend fun signInWithCredential(credential: AuthCredential): Resource<Unit> {
        return try {
            auth.signInWithCredential(credential).await()
            getAccessTokenAndSavedIt(SignType.SIGN_IN)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }

    override suspend fun signUpWithCredential(
        credential: AuthCredential,
        name: String?,
        email: String?
    ): Resource<Unit> {
        return try {
            auth.signInWithCredential(credential).await()
            addNameAndEmail(name, email)
            getAccessTokenAndSavedIt(SignType.SIGN_UP, name.orEmpty())
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }

    override suspend fun signOut() {
        applicationCoroutine.cancel(CancellationException("Logging Out"))
        auth.signOut()
        userDao.deleteAllUsers()
        activityDao.deleteAllActivities()
        supplementDao.deleteAllSupplements()
        activityHistoryDao.deleteAllActivitiesHistory()
        supplementHistoryDao.deleteAllSupplementsHistory()
        appStateRepositoryImpl.clear()
        appStateRepositoryImpl.updateAppState(AppState.LOGGED_OUT)
    }

    override suspend fun changePassword(): Resource<Unit> {
        return try {
            val email = auth.currentUser!!.email!!
            auth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e)
            Resource.Failure(e)
        }
    }

    private suspend fun getAccessTokenAndSavedIt(signType: SignType, name: String = "") {
        val user = auth.currentUser!!
        val firebaseToken = user.getIdToken(false).await().token
        requireNotNull(firebaseToken) { "Firebase token is null" }
        Timber.i("firebaseToken: $firebaseToken")

        val result =
            if (signType == SignType.SIGN_IN) userApi.login(UserLoginRequest(firebaseToken))
            else userApi.register(UserRegisterRequest(firebaseToken, name))
        Timber.i("accessToken: ${result.accessToken}")

        userDao.insertUser(result.user.toUserEntity())
        appStateRepositoryImpl.apply {
            updateAppState(AppState.LOGGED_IN)
            setAccessToken(result.accessToken)
        }
    }

    private suspend fun addNameAndEmail(name: String? = null, email: String? = null) {
        val user = auth.currentUser!!
        val request = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        user.updateProfile(request).await()
        if (email != null)
            user.updateEmail(email).await()
    }

    enum class SignType {
        SIGN_UP, SIGN_IN
    }
}