package com.example.onelook.data.network.users

import androidx.test.filters.SmallTest
import com.example.onelook.authentication.data.remote.UserApi
import com.example.onelook.authentication.data.remote.UserLoginRequest
import com.example.onelook.authentication.data.remote.UserRegisterRequest
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.isEmptyString
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@SmallTest
class UserApiTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var userApi: UserApi

    @Inject
    lateinit var auth: FirebaseAuth
    private var firebaseIdToken: String? = null
    private val name = "test user"

    @Before
    fun setupHilt() {
        hiltRule.inject()
    }

    @Before
    fun setupFirebaseIdToken() = runBlocking {
        auth.signInWithEmailAndPassword("test@test.com", "123123").await()
        firebaseIdToken = auth.currentUser!!.getIdToken(false).await().token
    }

    @Test
    fun register_idTokenAndName_createsUserAndReturnUserIdAndAccessToken() = runBlocking {
        // Given a firebase id token
        val idToken = firebaseIdToken!!

        // WHEN call register
        val response = userApi.register(UserRegisterRequest(idToken, name))

        // THEN response contains an access token
        assertThat(response.accessToken, not(isEmptyString()))
    }

    @Test
    fun login_idTokenAndName_returnsUserIdAndAccessToken() = runBlocking {
        // Given a firebase id token
        val idToken = firebaseIdToken!!

        // WHEN call login
        val response = userApi.login(UserLoginRequest(idToken))

        // THEN response contains an access token
        assertThat(response.accessToken, not(isEmptyString()))
    }

    @Test
    fun deleteUser_idTokenAndName_deletesUserFromApi() = runBlocking {
        // WHEN call deleteUser
        userApi.deleteUser(UserLoginRequest(firebaseIdToken!!))

        // THEN the user is deleted, and no errors happen
    }
}