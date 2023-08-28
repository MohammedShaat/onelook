package com.example.onelook.common.util.adapters

import androidx.annotation.DrawableRes

interface SelectableItem

data class SelectableRectWithText(
    val text: String,
    @DrawableRes val iconSelected: Int,
) : SelectableItem

data class SelectableOvalWithText(
    val text: String,
    @DrawableRes val iconSelected: Int,
) : SelectableItem

data class SelectableOvalNumber(
    val number: String,
) : SelectableItem