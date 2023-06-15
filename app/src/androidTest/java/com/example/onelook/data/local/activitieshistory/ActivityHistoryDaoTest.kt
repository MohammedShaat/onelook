package com.example.onelook.data.local.activitieshistory

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.example.onelook.data.local.activities.ActivityDao
import com.example.onelook.data.local.activities.LocalActivity
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
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
@SmallTest
class ActivityHistoryDaoTest {

    @Inject
    @Named("test")
    lateinit var userDao: UserDao

    @Inject
    @Named("test")
    lateinit var activityDao: ActivityDao

    @Inject
    @Named("test")
    lateinit var activityHistoryDao: ActivityHistoryDao

    private val user = LocalUser(
        1, "Android Test", "firebaseUid",
        "2023-06-10 11:45:30", "2023-06-10 11:45:30"
    )
    private val activities = listOf(
        LocalActivity(
            UUID.randomUUID(), "breathing", "evening", "00:10", "before",
            user.id, "2023-06-10 13:00:50", "2023-06-10 13:00:50"
        ),
        LocalActivity(
            UUID.randomUUID(), "waking", "morning", "01:30", "before",
            user.id, "2023-06-10 13:00:50", "2023-06-10 13:00:50"
        ),
        LocalActivity(
            UUID.randomUUID(), "yoga", "morning", "00:25", "before",
            user.id, "2023-06-10 13:00:50", "2023-06-10 13:00:50"
        )
    )
    private val activitiesHistory = listOf(
        LocalActivityHistory(
            UUID.randomUUID(), activities[0].id, "00:05", false,
            "2023-06-10 13:00:50", "2023-06-10 13:00:50"
        ),
        LocalActivityHistory(
            UUID.randomUUID(), activities[0].id, "00:010", true,
            "2023-06-10 13:00:50", "2023-06-10 13:00:50"
        ),
        LocalActivityHistory(
            UUID.randomUUID(), activities[1].id, "01:05", false,
            "2023-06-10 13:00:50", "2023-06-10 13:00:50"
        ),
    )

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setupHiltAndDBUser() = runBlocking {
        hiltRule.inject()
        userDao.insertUser(user)
        activityDao.insertActivities(activities)
    }

    @Test
    fun getActivitiesHistory_userId_returnsListOfActivitiesHistory() = runBlocking {
        activityHistoryDao.insertActivitiesHistory(activitiesHistory)
        // WHEN call getActivitiesHistory()
        val activitiesHistoryResult = activityHistoryDao.getActivitiesHistory(activities[0].id).first()

        // THEN there is a list of ActivitiesHistory
        assertThat(activitiesHistoryResult, hasSize(2))
    }

    @Test
    fun getActivityHistoryById_id_returnsActivityHistory() = runBlocking {
        activityHistoryDao.insertActivitiesHistory(activitiesHistory)
        // GIVEN a activityHistory id
        val id = activitiesHistory[0].id

        // WHEN call getActivityHistoryById()
        val supplementResult = activityHistoryDao.getActivityHistoryById(id).first()

        // THEN the activityHistory is retrieved
        assertThat(supplementResult, equalTo(activitiesHistory[0]))
    }

    @Test
    fun insertActivityHistory_activityHistory_insertsActivityHistory() = runBlocking {
        // GIVEN an ActivityHistory
        val activityHistory = activitiesHistory[0]

        // WHEN call insertActivityHistory()
        activityHistoryDao.insertActivityHistory(activityHistory)

        // THEN the activityHistory is inserted
        val activityHistoryResult =
            activityHistoryDao.getActivityHistoryById(activityHistory.id).first()
        assertThat(activityHistoryResult, equalTo(activityHistory))
    }

    @Test
    fun insertActivitiesHistory_listOfActivitiesHistory_insertsActivitiesHistory() =
        runBlocking {
            // WHEN call insertActivitiesHistory()
            activityHistoryDao.insertActivitiesHistory(activitiesHistory)

            // THEN the ActivitiesHistory objects are inserted
            val activitiesHistoryResult =
                activityHistoryDao.getActivitiesHistory(activities[1].id).first()
            assertThat(activitiesHistoryResult, hasSize(1))
        }

    @Test
    fun updateActivityHistory_activitiesHistory_updatesActivityHistory() = runBlocking {
        activityHistoryDao.insertActivitiesHistory(activitiesHistory)
        // GIVEN an ActivityHistory
        val activityHistory = activitiesHistory[0].copy(completed = true)

        // WHEN call updateActivityHistory()
        activityHistoryDao.updateActivityHistory(activityHistory)

        // THEN the activityHistory is updated
        val activitiesHistoryResult =
            activityHistoryDao.getActivityHistoryById(activityHistory.id).first()
        assertThat(activitiesHistoryResult.completed, not(equalTo(activitiesHistory[0].completed)))
    }

    @Test
    fun deleteActivityHistory_activitiesHistory_deletesActivityHistory() = runBlocking {
        activityHistoryDao.insertActivitiesHistory(activitiesHistory)
        // GIVEN an ActivityHistory
        val activityHistory = activitiesHistory[0]

        // WHEN call deleteActivityHistory()
        activityHistoryDao.deleteActivityHistory(activityHistory)

        // THEN the activityHistory is deleted
        val activitiesHistoryResult =
            activityHistoryDao.getActivityHistoryById(activityHistory.id).first()
        assertThat(activitiesHistoryResult, nullValue())
    }
}