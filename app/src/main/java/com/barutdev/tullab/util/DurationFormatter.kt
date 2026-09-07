package com.barutdev.tullab.util

import android.content.Context
import androidx.compose.runtime.Composable
import com.barutdev.tullab.R
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun formatDurationHours(durationInHours: Double): String {
    val locale = Locale.getDefault()
    val quantity = if (durationInHours == 1.0) 1 else 2
    val decimalFormat = DecimalFormat("0.##", DecimalFormatSymbols(locale))
    val formattedNumber = decimalFormat.format(durationInHours)
    return tullabPluralResource(
        id = R.plurals.duration_hours,
        quantity = quantity,
        formattedNumber
    )
}

fun formatDurationHours(context: Context, durationInHours: Double): String {
    val locale = context.resources.configuration.locales[0]
    val quantity = if (durationInHours == 1.0) 1 else 2
    val decimalFormat = DecimalFormat("0.##", DecimalFormatSymbols(locale))
    val formattedNumber = decimalFormat.format(durationInHours)
    return context.resources.getQuantityString(R.plurals.duration_hours, quantity, formattedNumber)
}
