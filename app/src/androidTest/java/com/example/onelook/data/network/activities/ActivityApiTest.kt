package com.example.onelook.data.network.activities

import androidx.test.filters.SmallTest
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
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
class ActivityApiTest {

    private val activities = listOf(
        NetworkActivity(
            id = UUID.fromString("f8759a57-1919-4891-8ad9-11d376d89e1b"),
            type = "breathing",
            timeOfDay = "afternoon",
            duration = "00:15",
            reminder = "before",
            userId = 1,
            createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("y-MM-dd HH:mm:ss")),
            updatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("y-MM-dd HH:mm:ss")),
        ),
        NetworkActivity(
            id = UUID.fromString("d0f698fa-096d-46c8-8489-8f781e25fc83"),
            type = "running",
            timeOfDay = "night",
            duration = "01:00",
            reminder = "before",
            userId = 1,
            createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("y-MM-dd HH:mm:ss")),
            updatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("y-MM-dd HH:mm:ss")),
        ),
    )

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    @Named("test")
    lateinit var activityApi: ActivityApi

    @Before
    fun setupHilt() {
        hiltRule.inject()
    }

    private fun instantiateActivities() = runBlocking {
        activityApi.createActivities(activities)
    }

    private fun cleanupActivities() = runBlocking {
        activityApi.deleteActivity(activities[0].id)
        activityApi.deleteActivity(activities[1].id)
    }

    @Test
    fun getActivities_returnsListOfActivity() = runBlocking {
        // WHEN call getActivities()
        val result = activityApi.getActivities()

        // THEN response contains a list of NetworkActivity
        assertThat(result, hasSize(5))
    }

    @Test
    fun getActivityById_returnsActivity() = runBlocking {
        // Instantiates activities
        instantiateActivities()

        // GIVEN an id of a NetworkActivity
        val id = activities[0].id

        // WHEN call getActivityById()
        val result = activityApi.getActivityById(id)

        // THEN response contains a list of NetworkActivity
        assertThat(result, notNullValue())

        // Cleans up
        cleanupActivities()
    }

    @Test
    fun createActivity_supplement_createsActivity() = runBlocking {
        // GIVEN a NetworkActivity
        val supplement = activities[0]

        // WHEN call createActivity()
        activityApi.createActivity(supplement)

        // THEN the supplement is created
        val result = activityApi.getActivityById(supplement.id)
        assertThat(result, equalTo(supplement))

        // Cleans up
        activityApi.deleteActivity(supplement.id)
    }

    @Test
    fun createActivities_listOfActivities_createsActivities() = runBlocking {
        // WHEN call createActivities()
        activityApi.createActivities(activities)

        // THEN the supplement is created
        val result = activityApi.getActivities()
        assertThat(activities, everyItem(isIn(result)))

        // Cleans up
        cleanupActivities()
    }

    @Test
    fun updateActivity_supplement_updatesActivity() = runBlocking {
        // Instantiates activities
        instantiateActivities()

        // GIVEN a NetworkActivity
        val supplement = activities[0].copy(type = "walking")

        // WHEN call updateActivity()
        activityApi.updateActivity(supplement)

        // THEN the supplement is updated
        val result = activityApi.getActivityById(supplement.id)
        assertThat(result.type, not(equalTo(activities[0].type)))

        // Cleans up
        cleanupActivities()
    }

    @Test
    fun deleteActivity_supplement_deletesActivity() = runBlocking {
        // Instantiates activities
        instantiateActivities()

        // GIVEN an id of a NetworkActivity
        val id = activities[0].id

        // WHEN call updateActivity()
        activityApi.deleteActivity(id)

        // THEN the supplement is deleted
        val result = activityApi.getActivities()
        assertThat(result, not(contains(activities[0])))

        // Cleans up
        activityApi.deleteActivity(activities[1].id)
    }
}