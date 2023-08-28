package com.example.onelook.tasks.doamin.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
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
    val timeOfDay: String?,
    val takingWithMeals: String,
) : TodayTask, Parcelable {

    val parsedForm: Supplement.Form
        get() = Supplement.Form.valueOf(form.uppercase())
}
