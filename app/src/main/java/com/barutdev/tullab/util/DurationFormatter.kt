package com.barutdev.tullab.util

import android.content.Context
import androidx.compose.runtime.Composable
import com.barutdev.tullab.R
import java.text.DecimalFormat

@Composable
fun formatDurationHours(durationInHours: Double): String {
    val quantity = if (durationInHours == 1.0) 1 else 2
    val decimalFormat = DecimalFormat("0.##")
    val formattedNumber = decimalFormat.format(durationInHours)
    return tullabPluralResource(
        id = R.plurals.duration_hours,
        quantity = quantity,
        formattedNumber
    )
}

fun formatDurationHours(context: Context, durationInHours: Double): String {
    val quantity = if (durationInHours == 1.0) 1 else 2
    val decimalFormat = DecimalFormat("0.##")
    val formattedNumber = decimalFormat.format(durationInHours)
    return context.resources.getQuantityString(R.plurals.duration_hours, quantity, formattedNumber)
}
