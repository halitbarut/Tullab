package com.barutdev.tullab.util

import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import com.barutdev.tullab.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.text.DecimalFormat
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DurationPluralizationTest {

    private fun getLocalizedContext(languageCode: String): Context {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    private fun formatDuration(context: Context, durationInHours: Double): String {
        val quantity = if (durationInHours == 1.0) 1 else 2
        val decimalFormat = DecimalFormat("0.##")
        val formattedNumber = decimalFormat.format(durationInHours)
        return context.resources.getQuantityString(R.plurals.duration_hours, quantity, formattedNumber)
    }

    @Test
    fun testEnglishPluralization() {
        val context = getLocalizedContext("en")
        assertEquals("1 hour", formatDuration(context, 1.0))
        assertEquals("1.5 hours", formatDuration(context, 1.5))
        assertEquals("2 hours", formatDuration(context, 2.0))
    }

    @Test
    fun testTurkishPluralization() {
        val context = getLocalizedContext("tr")
        assertEquals("1 saat", formatDuration(context, 1.0))
        assertEquals("1,5 saat", formatDuration(context, 1.5))
        assertEquals("2 saat", formatDuration(context, 2.0))
    }

    @Test
    fun testGermanPluralization() {
        val context = getLocalizedContext("de")
        assertEquals("1 Stunde", formatDuration(context, 1.0))
        assertEquals("1,5 Stunden", formatDuration(context, 1.5))
        assertEquals("2 Stunden", formatDuration(context, 2.0))
    }
}
