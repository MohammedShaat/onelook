package com.example.onelook.data.local.users

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
@SmallTest
class UserDaoTest {

    @Inject
    @Named("test")
    lateinit var userDao: UserDao

    private val user = LocalUser(
        1, "Android Test",
        "firebaseUid", "accessToken",
        "2023-06-10 11:45:30", "2023-06-10 11:45:30"
    )

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupHilt() {
        hiltRule.inject()
    }

    @Test
    fun insertUser_user_createsUser() = runBlocking {
        // WHEN call insertUser()
        userDao.insertUser(user)

        // THEN a user is created
        val userResult = userDao.getUser(1)
        assertThat(userResult, equalTo(user))
    }

    @Test
    fun getUser_id_returnsUser() = runBlocking {
        userDao.insertUser(user)

        // WHEN call getUser()
        userDao.getUser(user.id)

        // THEN the user is received
        val userResult = userDao.getUser(1)
        assertThat(userResult, equalTo(user))
    }

    @Test
    fun deleteUser_user_deletesUser() = runBlocking {
        userDao.insertUser(user)

        // WHEN call deleteUser()
        userDao.deleteUser(user)

        // THEN the user is deleted
        val userResult = userDao.getUser(1)
        assertThat(userResult, not(equalTo(user)))
    }
}