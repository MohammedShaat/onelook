package com.example.onelook.data.local.users

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.example.onelook.authentication.data.local.UserDao
import com.example.onelook.authentication.data.local.UserEntity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
@SmallTest
class UserDaoTest {

    @Inject
    @Named("test")
    lateinit var userDao: UserDao

    private val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("y-MM-dd HH:mm:ss"))

    private val user = UserEntity(
        1, "Android Test", "firebaseUid",
        date, date
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
        val userResult = userDao.getUserById(1)
        assertThat(userResult, equalTo(user))
    }

    @Test
    fun getUserById_id_returnsUser() = runBlocking {
        userDao.insertUser(user)

        // WHEN call getUser()
        val userResult = userDao.getUserById(user.id)

        // THEN the user is received
        assertThat(userResult, equalTo(user))
    }

    @Test
    fun getUserByFirebaseUid_firebaseUid_returnsUser() = runBlocking {
        userDao.insertUser(user)

        // WHEN call getUser()
        val userResult = userDao.getUserByFirebaseUid(user.firebaseUid)

        // THEN the user is retrieved
        assertThat(userResult, equalTo(user))
    }

    @Test
    fun getAllUsers_returnsAllUsers() = runBlocking {
        userDao.insertUser(user)

        // WHEN call getAllUsers()
        val usersResult = userDao.getAllUsers()

        // THEN all users are retrieved
        assertThat(usersResult, hasSize(1))
    }

    @Test
    fun deleteUser_user_deletesUser() = runBlocking {
        userDao.insertUser(user)

        // WHEN call deleteUser()
        userDao.deleteUser(user)

        // THEN the user is deleted
        val userResult = userDao.getUserById(1)
        assertThat(userResult, not(equalTo(user)))
    }

    @Test
    fun deleteAllUsers_deletesAllUsers() = runBlocking {
        userDao.insertUser(user)

        // WHEN call deleteAllUsers()
        userDao.deleteAllUsers()

        // THEN all users are deleted
        val userResult = userDao.getAllUsers()
        assertThat(userResult, hasSize(0))
    }
}