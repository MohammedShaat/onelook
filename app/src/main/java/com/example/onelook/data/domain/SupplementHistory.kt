package com.example.onelook.data.domain

import com.google.gson.annotations.SerializedName
import java.util.*

data class SupplementHistory(
    override val id: UUID,
    val supplementId: UUID,
    val progress: Int,
    val completed: Boolean,
    override val createdAt: String,
    val updatedAt: String,
    val name: String,
    val form: String,
    val dosage: Int,
    val takingWithMeals: String,
) : TodayTask {

    val formattedForm: Form
        get() = Form.valueOf(form.uppercase())
    val formattedTakingWithMeals: TakingWithMeals
        get() = TakingWithMeals.valueOf(takingWithMeals.uppercase())

    enum class TakingWithMeals {
        BEFORE, AFTER, WITH
    }

    enum class Form {
        PILL, TABLET, SACHET, DROPS, SPOON
    }
}
