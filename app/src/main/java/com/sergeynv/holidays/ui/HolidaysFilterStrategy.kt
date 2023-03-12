package com.sergeynv.holidays.ui

import android.content.Context
import androidx.annotation.StringRes
import com.sergeynv.holidays.R

enum class HolidaysFilterStrategy(@StringRes val descriptionStringId: Int) {
    IN_EITHER (R.string.holidays_filter_strategy_either),
    IN_BOTH (R.string.holidays_filter_strategy_both),
    IN_A_NOT_IN_B (R.string.holidays_filter_strategy_in_a_not_in_b),
    IN_B_NOT_IN_A (R.string.holidays_filter_strategy_in_b_not_in_a);

    fun getDescription(context: Context) = context.resources.getString(descriptionStringId)

    companion object {
        fun getDescriptions(context: Context) = values().map { it.getDescription(context) }
    }
}