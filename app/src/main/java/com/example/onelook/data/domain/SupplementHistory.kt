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

    val formattedForm: Supplement.Form
        get() = Supplement.Form.valueOf(form.uppercase())
    val formattedTakingWithMeals: Supplement.TakingWithMeals
        get() = Supplement.TakingWithMeals.valueOf(takingWithMeals.uppercase())
}
