package com.barutdev.tullab.util

import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
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

    @Test
    fun testEnglishPluralization() {
        val context = getLocalizedContext("en")
        assertEquals("1 hour", formatDurationHours(context, 1.0))
        assertEquals("1.5 hours", formatDurationHours(context, 1.5))
        assertEquals("2 hours", formatDurationHours(context, 2.0))
    }

    @Test
    fun testTurkishPluralization() {
        val context = getLocalizedContext("tr")
        assertEquals("1 saat", formatDurationHours(context, 1.0))
        assertEquals("1,5 saat", formatDurationHours(context, 1.5))
        assertEquals("2 saat", formatDurationHours(context, 2.0))
    }

    @Test
    fun testGermanPluralization() {
        val context = getLocalizedContext("de")
        assertEquals("1 Stunde", formatDurationHours(context, 1.0))
        assertEquals("1,5 Stunden", formatDurationHours(context, 1.5))
        assertEquals("2 Stunden", formatDurationHours(context, 2.0))
    }
}
