package com.example.onelook.tasks.doamin.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
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
) : Parcelable {

    val parsedForm: Form
        get() = Form.valueOf(form.uppercase())

    enum class Form {
        PILL, TABLET, SACHET, DROPS, SPOON
    }
}
