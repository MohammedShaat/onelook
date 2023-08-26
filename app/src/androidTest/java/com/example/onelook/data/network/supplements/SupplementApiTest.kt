package com.example.onelook.data.network.supplements

import androidx.test.filters.SmallTest
import com.example.onelook.tasks.data.remote.SupplementApi
import com.example.onelook.tasks.data.remote.SupplementDto
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
class SupplementApiTest {

    private val supplements = listOf(
        SupplementDto(
            id = UUID.fromString("f8759a57-1919-4891-8ad9-11d376d89e1b"),
            name = "Supplement Test 1",
            form = "pill",
            dosage = 2,
            frequency = "everyday",
            duration = null,
            timeOfDay = "afternoon",
            takingWithMeals = "before",
            reminder = "before",
            completed = false,
            userId = 1,
            createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("y-MM-dd HH:mm:ss")),
            updatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("y-MM-dd HH:mm:ss")),
        ),
        SupplementDto(
            id = UUID.fromString("d0f698fa-096d-46c8-8489-8f781e25fc83"),
            name = "Supplement Test 2",
            form = "tablet",
            dosage = 1,
            frequency = "everyday",
            duration = "4 days",
            timeOfDay = "evening",
            takingWithMeals = "with",
            reminder = "both",
            completed = false,
            userId = 1,
            createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("y-MM-dd HH:mm:ss")),
            updatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("y-MM-dd HH:mm:ss")),
        ),
    )

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    @Named("test")
    lateinit var supplementApi: SupplementApi

    @Before
    fun setupHilt() {
        hiltRule.inject()
    }

    private fun instantiateSupplements() = runBlocking {
        supplementApi.createSupplements(supplements)
    }

    private fun cleanupSupplements() = runBlocking {
        supplementApi.deleteSupplement(supplements[0].id)
        supplementApi.deleteSupplement(supplements[1].id)
    }

    @Test
    fun getSupplements_returnsListOfSupplement() = runBlocking {
        // WHEN call getSupplements()
        val result = supplementApi.getSupplements()

        // THEN response contains a list of SupplementDto
        assertThat(result, hasSize(4))
    }

    @Test
    fun getSupplementById_returnsSupplement() = runBlocking {
        // Instantiates supplements
        instantiateSupplements()

        // GIVEN a id of a SupplementDto
        val id = supplements[0].id

        // WHEN call getSupplementById()
        val result = supplementApi.getSupplementById(id)

        // THEN response contains a list of SupplementDto
        assertThat(result, notNullValue())

        // Cleans up
        cleanupSupplements()
    }

    @Test
    fun createSupplement_supplement_createsSupplement() = runBlocking {
        // GIVEN a SupplementDto
        val supplement = supplements[0]

        // WHEN call createSupplement()
        supplementApi.createSupplement(supplement)

        // THEN the supplement is created
        val result = supplementApi.getSupplementById(supplement.id)
        assertThat(result, equalTo(supplement))

        // Cleans up
        supplementApi.deleteSupplement(supplement.id)
    }

    @Test
    fun createSupplements_listOfSupplements_createsSupplements() = runBlocking {
        // WHEN call createSupplements()
        supplementApi.createSupplements(supplements)

        // THEN the supplement is created
        val result = supplementApi.getSupplements()
        assertThat(supplements, everyItem(isIn(result)))

        // Cleans up
        cleanupSupplements()
    }

    @Test
    fun updateSupplement_supplement_updatesSupplement() = runBlocking {
        // Instantiates supplements
        instantiateSupplements()

        // GIVEN a SupplementDto
        val supplement = supplements[0].copy(form = "sachet")

        // WHEN call updateSupplement()
        supplementApi.updateSupplement(supplement)

        // THEN the supplement is updated
        val result = supplementApi.getSupplementById(supplement.id)
        assertThat(result.form, not(equalTo(supplements[0].form)))

        // Cleans up
        cleanupSupplements()
    }

    @Test
    fun deleteSupplement_supplement_deletesSupplement() = runBlocking {
        // Instantiates supplements
        instantiateSupplements()

        // GIVEN an id of a SupplementDto
        val id = supplements[0].id

        // WHEN call updateSupplement()
        supplementApi.deleteSupplement(id)

        // THEN the supplement is deleted
        val result = supplementApi.getSupplements()
        assertThat(result, not(contains(supplements[0])))

        // Cleans up
        supplementApi.deleteSupplement(supplements[1].id)
    }
}