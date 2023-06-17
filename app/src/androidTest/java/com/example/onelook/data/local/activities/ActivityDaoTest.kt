package com.example.onelook.data.local.activities

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.example.onelook.data.local.users.LocalUser
import com.example.onelook.data.local.users.UserDao
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
@SmallTest
class ActivityDaoTest {

    @Inject
    @Named("test")
    lateinit var userDao: UserDao

    @Inject
    @Named("test")
    lateinit var activityDao: ActivityDao
    
    private val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("y-MM-dd HH:mm:ss"))

    private val user = LocalUser(
        1, "Android Test", "firebaseUid",
        date, date
    )
    private val activities = listOf(
        LocalActivity(
            UUID.randomUUID(), "breathing", "evening", "00:10", "before",
            date, date
        ),
        LocalActivity(
            UUID.randomUUID(), "waking", "morning", "01:30", "before",
            "date", "date"
        ),
        LocalActivity(
            UUID.randomUUID(), "yoga", "morning", "00:25", "before",
            "date", "date"
        )
    )

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setupHiltAndDBUser() = runBlocking {
        hiltRule.inject()
        userDao.insertUser(user)
    }

    @Test
    fun getActivities_userId_returnsListOfActivities() = runBlocking {
        activityDao.insertActivities(activities)
        // GIVEN a user id
        val userId = user.id

        // WHEN call getActivities()
        val activitiesResult = activityDao.getActivities().first()

        // THEN there is a list of activities
        assertThat(activitiesResult, hasSize(3))
    }

    @Test
    fun getActivityById_id_returnsActivity() = runBlocking {
        activityDao.insertActivities(activities)
        // GIVEN a activity id
        val id = activities[0].id

        // WHEN call getActivityById()
        val supplementResult = activityDao.getActivityById(id).first()

        // THEN the activity is retrieved
        assertThat(supplementResult, equalTo(activities[0]))
    }

    @Test
    fun insertActivity_supplement_insertsActivity() = runBlocking {
        // GIVEN an Activity
        val activity = activities[0]

        // WHEN call insertActivity()
        activityDao.insertActivity(activity)

        // THEN the activity is inserted
        val supplementResult = activityDao.getActivityById(activity.id).first()
        assertThat(supplementResult, equalTo(activity))
    }

    @Test
    fun insertActivities_listOfActivities_insertsActivities() = runBlocking {
        // GIVEN a list of activities
        val activity = activities

        // WHEN call insertActivities()
        activityDao.insertActivities(activities)

        // THEN the activities are inserted
        val activitiesResult = activityDao.getActivities().first()
        assertThat(activitiesResult, hasSize(3))
    }

    @Test
    fun updateActivity_supplement_updatesActivity() = runBlocking {
        activityDao.insertActivities(activities)
        // GIVEN an Activity
        val activity = activities[0].copy(type = "Updated Activity")

        // WHEN call updateActivity()
        activityDao.updateActivity(activity)

        // THEN the activity is updated
        val activitiesResult = activityDao.getActivityById(activity.id).first()
        assertThat(activitiesResult.type, not(equalTo(activities[0].type)))
    }

    @Test
    fun deleteActivity_supplement_deletesActivity() = runBlocking {
        activityDao.insertActivities(activities)
        // GIVEN an Activity
        val activity = activities[0]

        // WHEN call deleteActivity()
        activityDao.deleteActivity(activity)

        // THEN the activity is deleted
        val activitiesResult = activityDao.getActivityById(activity.id).first()
        assertThat(activitiesResult, nullValue())
    }
}