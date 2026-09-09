package com.barutdev.tullab.ui.components

import android.content.Context
import android.os.Vibrator
import android.view.View
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class TullabHapticsTest {

    private class CountingView(context: Context) : View(context) {
        var hapticCalls = 0

        override fun performHapticFeedback(feedbackConstant: Int, flags: Int): Boolean {
            hapticCalls++
            // Report unhandled so the Vibrator fallback would run when enabled.
            return false
        }
    }

    @Test
    fun `disabled feedback suppresses view haptics and vibrator calls for all types`() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val view = CountingView(appContext)
        val composeHaptics = mockk<androidx.compose.ui.hapticfeedback.HapticFeedback>(relaxed = true)
        val context = mockk<Context>(relaxed = true)
        val vibrator = mockk<Vibrator>(relaxed = true)
        every { context.getSystemService(Context.VIBRATOR_SERVICE) } returns vibrator
        every { vibrator.hasVibrator() } returns true

        val haptics = TullabHapticFeedback(view, composeHaptics, context, enabled = false)

        TullabHapticFeedbackType.entries.forEach { haptics.perform(it) }

        assertEquals(0, view.hapticCalls)
        verify(exactly = 0) { vibrator.hasVibrator() }
        verify(exactly = 0) { vibrator.vibrate(any<Long>()) }
    }

    @Test
    fun `haptic feedback is enabled by default`() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val view = CountingView(appContext)
        val composeHaptics = mockk<androidx.compose.ui.hapticfeedback.HapticFeedback>(relaxed = true)
        val context = mockk<Context>(relaxed = true)

        val haptics = TullabHapticFeedback(view, composeHaptics, context)

        assertTrue(haptics.enabled)
    }
}
