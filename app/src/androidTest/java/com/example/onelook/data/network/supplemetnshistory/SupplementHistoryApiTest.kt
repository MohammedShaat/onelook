package com.example.onelook.data.network.supplemetnshistory

import androidx.test.filters.SmallTest
import com.example.onelook.tasks.data.remote.SupplementHistoryDto
import com.example.onelook.tasks.data.remote.SupplementHistoryApi
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
class SupplementHistoryApiTest {

    private val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("y-MM-dd HH:mm:ss"))

    private val supplementsHistory = listOf(
        SupplementHistoryDto(
            id = UUID.fromString("f8759a57-1919-4891-8ad9-11d376d89e1b"),
            supplementId = UUID.fromString("1e9602ae-fe11-11ed-be56-0242ac120002"),
            progress = 1,
            completed = false,
            createdAt = date,
            updatedAt = date,
        ),
        SupplementHistoryDto(
            id = UUID.fromString("d0f698fa-096d-46c8-8489-8f781e25fc83"),
            supplementId = UUID.fromString("2f366824-fe11-11ed-be56-0242ac120002"),
            progress = 5,
            completed = true,
            createdAt = date,
            updatedAt = date,
        ),
    )

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    @Named("test")
    lateinit var supplementHistoryApi: SupplementHistoryApi

    @Before
    fun setupHiltAndSupplements() = runBlocking {
        hiltRule.inject()
    }

    private fun instantiateSupplementsHistory() = runBlocking {
        supplementHistoryApi.createSupplementsHistory(supplementsHistory)
    }

    private fun cleanupSupplementsHistory() = runBlocking {
        supplementHistoryApi.deleteSupplementHistory(supplementsHistory[0].id)
        supplementHistoryApi.deleteSupplementHistory(supplementsHistory[1].id)
    }

    @Test
    fun getSupplementsHistory_returnsListOfSupplementsHistory() = runBlocking {
        // WHEN call getSupplementsHistory()
        val result = supplementHistoryApi.getSupplementsHistory()

        // THEN response contains a list of NetworkSupplementsHistory
        assertThat(result, hasSize(4))
    }

    @Test
    fun getSupplementHistoryById_returnsSupplementHistory() = runBlocking {
        // Instantiates activitiesHistory
        instantiateSupplementsHistory()

        // GIVEN an id of a NetworkSupplementsHistory
        val id = supplementsHistory[0].id

        // WHEN call getSupplementHistoryById()
        val result = supplementHistoryApi.getSupplementHistoryById(id)

        // THEN response contains a list of NetworkSupplementsHistory
        assertThat(result, notNullValue())

        // Cleans up
        cleanupSupplementsHistory()
    }

    @Test
    fun createSupplementHistory_supplementHistory_createsSupplementHistory() = runBlocking {
        // GIVEN a NetworkSupplementsHistory
        val supplementHistory = supplementsHistory[0]

        // WHEN call createSupplementHistory()
        supplementHistoryApi.createSupplementHistory(supplementHistory)

        // THEN the supplementHistory is created
        val result = supplementHistoryApi.getSupplementHistoryById(supplementHistory.id)
        assertThat(result, equalTo(supplementHistory))

        // Cleans up
        supplementHistoryApi.deleteSupplementHistory(supplementHistory.id)
    }

    @Test
    fun createSupplementsHistory_listOfSupplementsHistory_createsSupplementsHistory() =
        runBlocking {
            // WHEN call createSupplementsHistory()
            supplementHistoryApi.createSupplementsHistory(supplementsHistory)

            // THEN the supplementHistory is created
            val result = supplementHistoryApi.getSupplementsHistory()
            assertThat(supplementsHistory, everyItem(isIn(result)))

            // Cleans up
            cleanupSupplementsHistory()
        }

    @Test
    fun updateSupplementHistory_supplementHistory_updatesSupplementHistory() = runBlocking {
        // Instantiates activitiesHistory
        instantiateSupplementsHistory()

        // GIVEN a NetworkSupplementsHistory
        val supplement = supplementsHistory[0].copy(completed = true)

        // WHEN call updateSupplementHistory()
        supplementHistoryApi.updateSupplementHistory(supplement)

        // THEN the supplementHistory is updated
        val result = supplementHistoryApi.getSupplementHistoryById(supplement.id)
        assertThat(result.completed, not(equalTo(supplementsHistory[0].completed)))

        // Cleans up
        cleanupSupplementsHistory()
    }

    @Test
    fun deleteSupplementHistory_supplementHistory_deletesSupplementHistory() = runBlocking {
        // Instantiates activitiesHistory
        instantiateSupplementsHistory()

        // GIVEN an id of a NetworkSupplementsHistory
        val id = supplementsHistory[0].id

        // WHEN call updateSupplement()
        supplementHistoryApi.deleteSupplementHistory(id)

        // THEN the supplementHistory is deleted
        val result = supplementHistoryApi.getSupplementsHistory()
        assertThat(result, not(contains(supplementsHistory[0])))

        // Cleans up
        supplementHistoryApi.deleteSupplementHistory(supplementsHistory[1].id)
    }
}