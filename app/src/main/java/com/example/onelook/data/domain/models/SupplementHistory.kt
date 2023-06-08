package com.example.onelook.data.domain.models

import com.google.gson.annotations.SerializedName

data class SupplementHistory(
    override val id: String,
    @SerializedName("supplement_id") val supplementId: String,
    val progress: Int,
    val completed: Boolean,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    val name: String,
    val form: String,
    val dosage: Int,
    @SerializedName("taking_with_meals") val takingWithMeals: String,
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
