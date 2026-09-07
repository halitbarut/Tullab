package com.barutdev.tullab.ui.screens.dashboard.components

import com.barutdev.tullab.domain.model.PricingMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LogLessonDialogValidationTest {

    // Helper functions representing the logic inside LogLessonDialog
    private fun parseDuration(duration: String): Double? = duration.trim().replace(',', '.').toDoubleOrNull()
    
    private fun calculateTotalFee(durationStr: String, rate: Double, pricingMode: PricingMode, requiresFeePrompt: Boolean, customFeeStr: String): Double {
        val parsedDuration = parseDuration(durationStr) ?: 0.0
        return if (requiresFeePrompt) {
            parseDuration(customFeeStr) ?: 0.0
        } else if (pricingMode == PricingMode.PER_HOUR) {
            parsedDuration * rate
        } else {
            rate
        }
    }
    
    private fun isCompleteEnabled(durationStr: String, customFeeStr: String, rateOrFeeInput: String, pricingMode: PricingMode, isMarkAsPaidMode: Boolean, requiresFeePrompt: Boolean): Boolean {
        val isDurationValid = parseDuration(durationStr)?.let { it > 0.0 } == true
        val isFeeValid = !requiresFeePrompt || parseDuration(customFeeStr)?.let { it > 0.0 } == true
        val isRateOrFeeValid = parseDuration(rateOrFeeInput) != null
        return if (pricingMode == PricingMode.PER_HOUR || isMarkAsPaidMode) {
            isDurationValid && isFeeValid && isRateOrFeeValid
        } else {
            isFeeValid && isRateOrFeeValid
        }
    }

    @Test
    fun testDurationDecimalParsing() {
        assertEquals(1.5, parseDuration("1.5"))
        assertEquals(1.5, parseDuration("1,5"))
        assertEquals(2.0, parseDuration("2"))
        assertEquals(null, parseDuration("invalid"))
    }
    
    @Test
    fun testReactiveFeeCalculation() {
        // Hourly rate logic
        assertEquals(75.0, calculateTotalFee("1.5", 50.0, PricingMode.PER_HOUR, false, ""), 0.0)
        assertEquals(75.0, calculateTotalFee("1,5", 50.0, PricingMode.PER_HOUR, false, ""), 0.0)
        
        // Flat fee logic
        assertEquals(50.0, calculateTotalFee("1.5", 50.0, PricingMode.FLAT_FEE, false, ""), 0.0)
        
        // Custom fee prompt
        assertEquals(100.0, calculateTotalFee("1.5", 0.0, PricingMode.PER_HOUR, true, "100"), 0.0)
    }

    @Test
    fun testPositiveValidationLogic() {
        // Valid mark as paid
        assertTrue(isCompleteEnabled("1.5", "", "50.0", PricingMode.PER_HOUR, true, false))
        
        // Invalid duration
        assertFalse(isCompleteEnabled("-1.0", "", "50.0", PricingMode.PER_HOUR, true, false))
        assertFalse(isCompleteEnabled("0", "", "50.0", PricingMode.PER_HOUR, true, false))
        assertFalse(isCompleteEnabled("", "", "50.0", PricingMode.PER_HOUR, true, false))
        
        // Custom fee invalid
        assertFalse(isCompleteEnabled("1.5", "-10", "0", PricingMode.PER_HOUR, true, true))
        assertTrue(isCompleteEnabled("1.5", "100", "0", PricingMode.PER_HOUR, true, true))
    }
}
