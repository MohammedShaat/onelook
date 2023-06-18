package com.example.onelook.data.domain

import com.google.gson.annotations.SerializedName
import java.util.*

data class Supplement(
    val id: UUID,
    val name: String,
    val form: String,
    val dosage: Int,
    val frequency: String,
    val duration: String?,
    val timeOfDay: String?,
    val takingWithMeals: String,
    val reminder: String?,
    val completed: Boolean,
    val userId: Int,
    val createdAt: String,
    val updatedAt: String
) {

    val formattedForm: Supplement.Form
        get() = Supplement.Form.valueOf(form.uppercase())
    val formattedTakingWithMeals: Supplement.TakingWithMeals
        get() = Supplement.TakingWithMeals.valueOf(takingWithMeals.uppercase())

    enum class Form {
        PILL, TABLET, SACHET, DROPS, SPOON
    }

    enum class TakingWithMeals {
        BEFORE, AFTER, WITH
    }
}
