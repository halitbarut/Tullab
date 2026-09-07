package com.barutdev.tullab.util

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import com.barutdev.tullab.R
import com.barutdev.tullab.ui.theme.LocalLocale
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun formatDurationHours(durationInHours: Double): String {
    val locale = LocalLocale.current
    val quantity = if (durationInHours == 1.0) 1 else 2
    val decimalFormat = DecimalFormat("0.##", DecimalFormatSymbols(locale))
    val formattedNumber = decimalFormat.format(durationInHours)
    return tullabPluralResource(
        id = R.plurals.duration_hours,
        quantity = quantity,
        formattedNumber
    )
}

fun formatDurationHours(context: Context, durationInHours: Double, locale: Locale? = null): String {
    val resolvedLocale = locale ?: context.resources.configuration.locales[0]
    val quantity = if (durationInHours == 1.0) 1 else 2
    val decimalFormat = DecimalFormat("0.##", DecimalFormatSymbols(resolvedLocale))
    val formattedNumber = decimalFormat.format(durationInHours)
    val config = Configuration(context.resources.configuration).apply {
        setLocale(resolvedLocale)
    }
    val localizedContext = context.createConfigurationContext(config)
    return localizedContext.resources.getQuantityString(R.plurals.duration_hours, quantity, formattedNumber)
}
