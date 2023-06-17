package com.example.onelook.data.network.activitieshistory

import androidx.test.filters.SmallTest
import com.example.onelook.data.network.activities.ActivityApi
import com.example.onelook.data.network.activities.NetworkActivity
import com.example.onelook.data.network.users.NetworkUserRegisterRequest
import com.example.onelook.data.network.users.UserApi
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.After
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
class ActivityHistoryApiTest {

    private val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("y-MM-dd HH:mm:ss"))

    private val activitiesHistory = listOf(
        NetworkActivityHistory(
            id = UUID.fromString("f8759a57-1919-4891-8ad9-11d376d89e1b"),
            activityId = UUID.fromString("1558412a-fe11-11ed-be56-0242ac120002"),
            progress = "00:03",
            completed = false,
            createdAt = date,
            updatedAt = date,
        ),
        NetworkActivityHistory(
            id = UUID.fromString("d0f698fa-096d-46c8-8489-8f781e25fc83"),
            activityId = UUID.fromString("2f366824-fe11-11ed-be56-0242ac120002"),
            progress = "00:15",
            completed = true,
            createdAt = date,
            updatedAt = date,
        ),
    )

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    @Named("test")
    lateinit var activityHistoryApi: ActivityHistoryApi

    @Before
    fun setupHiltAndActivities() = runBlocking {
        hiltRule.inject()
    }

    private fun instantiateActivitiesHistory() = runBlocking {
        activityHistoryApi.createActivitiesHistory(activitiesHistory)
    }

    private fun cleanupActivitiesHistory() = runBlocking {
        activityHistoryApi.deleteActivityHistory(activitiesHistory[0].id)
        activityHistoryApi.deleteActivityHistory(activitiesHistory[1].id)
    }

    @Test
    fun getActivitiesHistory_returnsListOfActivitiesHistory() = runBlocking {
        // WHEN call getActivitiesHistory()
        val result = activityHistoryApi.getActivitiesHistory()

        // THEN response contains a list of NetworkActivitiesHistory
        assertThat(result, hasSize(2))
    }

    @Test
    fun getActivityHistoryById_returnsActivityHistory() = runBlocking {
        // Instantiates activitiesHistory
        instantiateActivitiesHistory()

        // GIVEN an id of a NetworkActivitiesHistory
        val id = activitiesHistory[0].id

        // WHEN call getActivityHistoryById()
        val result = activityHistoryApi.getActivityHistoryById(id)

        // THEN response contains a list of NetworkActivitiesHistory
        assertThat(result, notNullValue())

        // Cleans up
        cleanupActivitiesHistory()
    }

    @Test
    fun createActivityHistory_activityHistory_createsActivityHistory() = runBlocking {
        // GIVEN a NetworkActivitiesHistory
        val activityHistory = activitiesHistory[0]

        // WHEN call createActivityHistory()
        activityHistoryApi.createActivityHistory(activityHistory)

        // THEN the activityHistory is created
        val result = activityHistoryApi.getActivityHistoryById(activityHistory.id)
        assertThat(result, equalTo(activityHistory))

        // Cleans up
        activityHistoryApi.deleteActivityHistory(activityHistory.id)
    }

    @Test
    fun createActivitiesHistory_listOfActivitiesHistory_createsActivitiesHistory() = runBlocking {
        // WHEN call createActivitiesHistory()
        activityHistoryApi.createActivitiesHistory(activitiesHistory)

        // THEN the activityHistory is created
        val result = activityHistoryApi.getActivitiesHistory()
        assertThat(activitiesHistory, everyItem(isIn(result)))

        // Cleans up
        cleanupActivitiesHistory()
    }

    @Test
    fun updateActivityHistory_activityHistory_updatesActivityHistory() = runBlocking {
        // Instantiates activitiesHistory
        instantiateActivitiesHistory()

        // GIVEN a NetworkActivitiesHistory
        val supplement = activitiesHistory[0].copy(completed = true)

        // WHEN call updateActivityHistory()
        activityHistoryApi.updateActivityHistory(supplement)

        // THEN the activityHistory is updated
        val result = activityHistoryApi.getActivityHistoryById(supplement.id)
        assertThat(result.completed, not(equalTo(activitiesHistory[0].completed)))

        // Cleans up
        cleanupActivitiesHistory()
    }

    @Test
    fun deleteActivityHistory_activityHistory_deletesActivityHistory() = runBlocking {
        // Instantiates activitiesHistory
        instantiateActivitiesHistory()

        // GIVEN an id of a NetworkActivitiesHistory
        val id = activitiesHistory[0].id

        // WHEN call updateActivity()
        activityHistoryApi.deleteActivityHistory(id)

        // THEN the activityHistory is deleted
        val result = activityHistoryApi.getActivitiesHistory()
        assertThat(result, not(contains(activitiesHistory[0])))

        // Cleans up
        activityHistoryApi.deleteActivityHistory(activitiesHistory[1].id)
    }
}