package com.example.onelook.data.network

import androidx.test.filters.SmallTest
import com.example.onelook.data.network.requests.NetworkLoginAndDeleteUserRequestBody
import com.example.onelook.data.network.requests.NetworkRegisterRequestBody
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.isEmptyString
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidTest
@SmallTest
class OneLookApiTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var oneLookApi: OneLookApi

    @Inject
    lateinit var auth: FirebaseAuth
    private var firebaseIdToken: String? = null
    private val name = "test user"
    private var accessToken =
        "eyJhbGciOiJSUzI1NiIsImtpZCI6IjU0NWUyNDZjNTEwNmExMGQ2MzFiMTA0M2E3MWJiNTllNWJhMGM5NGQiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vb25lbG9vay04MWJjZCIsImF1ZCI6Im9uZWxvb2stODFiY2QiLCJhdXRoX3RpbWUiOjE2ODU4MDE3NzcsInVzZXJfaWQiOiJOTUEydGJ6UWZOYk5Hc1ZGQlg0UXR2M2J6V3AxIiwic3ViIjoiTk1BMnRielFmTmJOR3NWRkJYNFF0djNieldwMSIsImlhdCI6MTY4NTgwMTc3NywiZXhwIjoxNjg1ODA1Mzc3LCJlbWFpbCI6InRlc3RAdGVzdC5jb20iLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImZpcmViYXNlIjp7ImlkZW50aXRpZXMiOnsiZW1haWwiOlsidGVzdEB0ZXN0LmNvbSJdfSwic2lnbl9pbl9wcm92aWRlciI6InBhc3N3b3JkIn19.fZdCPxfkHNjk0LCtHDXGAMNtVw0zEMkN3gDMOM1xqQTkHtp3KGUMtLO3uXas0sdfmNGtPiIZGdNbJNHVXN_XhkAvsy2RupZm8tElrF0nM4PrZ7CTCRAPSXuv2kxP1rf_4PFEAT9UnnxPpjsc3hgTwZHzfi-xFvS_venzuPNcR2HpM2s3-bqjQUuENCA7HI9mgdv16qckr7BsBYPpPLL94I6hmVxenaCgz8h2H-VaHAZNoqyOSdUF40RgfWntkW3Q2w8QJi5AdTGHvNEYW1RyMzM5Ib9PitcjLcxMIBB48SEA2j8uVbld2VIbP9EpCuzh_QgnGgF0GDy_4-UmeHi09w"

    @Before
    fun setupHilt() {
        hiltRule.inject()
    }

    @Before
    fun setupFirebaseIdToken() = runBlocking {
        auth.signInWithEmailAndPassword("mohammedshaat.it@gmail.com", "123123").await()
        firebaseIdToken = auth.currentUser!!.getIdToken(false).await().token
        Timber.i("Id token: $firebaseIdToken")
    }

    @Test
    fun register_idTokenAndName_createsUserAndReturnUserIdAndAccessToken() = runBlocking {
        // WHEN call register
        val response = oneLookApi.register(NetworkRegisterRequestBody(firebaseIdToken!!, name))

        // THEN response contains an access token
        assertThat(response.accessToken, not(isEmptyString()))
    }

    @Test
    fun login_idTokenAndName_returnsUserIdAndAccessToken() = runBlocking {
        // WHEN call login
        val response = oneLookApi.login(NetworkLoginAndDeleteUserRequestBody(firebaseIdToken!!))

        // THEN response contains an access token
        assertThat(response.accessToken, not(isEmptyString()))
    }

    @Test
    fun deleteUser_idTokenAndName_deletesUserFromApi() = runBlocking {
        // WHEN call deleteUser
        val response = oneLookApi.deleteUser(NetworkLoginAndDeleteUserRequestBody(firebaseIdToken!!))

        // THEN response contains an access token
        assertThat(response.message, equalTo("User deleted successfully"))
    }

    @Test
    fun getTodayTasks_returnsListOfNetworkSupplementAndActivityHistory() = runBlocking {
        // WHEN call getTodayTasks()
        val response = oneLookApi.getTodayTasks()

        // THEN response contains a list of SupplementHistory and ActivityHistory
        assertThat(response.size, equalTo(4))
    }

    @Test
    fun getSupplements_returnsListOfNetworkSupplement() = runBlocking {
        // WHEN call getSupplements()
        val response = oneLookApi.getSupplements()

        // THEN response contains a list of NetworkSupplement
        assertThat(response.size, equalTo(4))
    }
}