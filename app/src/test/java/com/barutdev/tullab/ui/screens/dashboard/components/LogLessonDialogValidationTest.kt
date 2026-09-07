package com.barutdev.tullab.ui.screens.dashboard.components

import com.barutdev.tullab.domain.model.PricingMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LogLessonDialogValidationTest {

    @Test
    fun testDurationDecimalParsing() {
        assertEquals(1.5, parseDurationDecimal("1.5"))
        assertEquals(1.5, parseDurationDecimal("1,5"))
        assertEquals(2.0, parseDurationDecimal("2"))
        assertEquals(null, parseDurationDecimal("invalid"))
    }
    
    @Test
    fun testReactiveFeeCalculation() {
        // Hourly rate logic
        assertEquals(75.0, calculateDialogTotalFee("1.5", 50.0, PricingMode.PER_HOUR, false, ""), 0.0)
        assertEquals(75.0, calculateDialogTotalFee("1,5", 50.0, PricingMode.PER_HOUR, false, ""), 0.0)
        
        // Flat fee logic
        assertEquals(50.0, calculateDialogTotalFee("1.5", 50.0, PricingMode.FLAT_FEE, false, ""), 0.0)
        
        // Custom fee prompt
        assertEquals(100.0, calculateDialogTotalFee("1.5", 0.0, PricingMode.PER_HOUR, true, "100"), 0.0)
    }

    @Test
    fun testPositiveValidationLogic() {
        // Valid mark as paid
        assertTrue(isDialogSaveEnabled(durationStr = "1.5", customFeeStr = "", rateOrFeeInput = "50.0", pricingMode = PricingMode.PER_HOUR, isMarkAsPaidMode = true, requiresFeePrompt = false))
        
        // Invalid duration
        assertFalse(isDialogSaveEnabled(durationStr = "-1.0", customFeeStr = "", rateOrFeeInput = "50.0", pricingMode = PricingMode.PER_HOUR, isMarkAsPaidMode = true, requiresFeePrompt = false))
        assertFalse(isDialogSaveEnabled(durationStr = "0", customFeeStr = "", rateOrFeeInput = "50.0", pricingMode = PricingMode.PER_HOUR, isMarkAsPaidMode = true, requiresFeePrompt = false))
        assertFalse(isDialogSaveEnabled(durationStr = "", customFeeStr = "", rateOrFeeInput = "50.0", pricingMode = PricingMode.PER_HOUR, isMarkAsPaidMode = true, requiresFeePrompt = false))
        
        // Custom fee invalid vs valid
        assertFalse(isDialogSaveEnabled(durationStr = "1.5", customFeeStr = "-10", rateOrFeeInput = "0", pricingMode = PricingMode.PER_HOUR, isMarkAsPaidMode = true, requiresFeePrompt = true))
        assertTrue(isDialogSaveEnabled(durationStr = "1.5", customFeeStr = "100", rateOrFeeInput = "0", pricingMode = PricingMode.PER_HOUR, isMarkAsPaidMode = true, requiresFeePrompt = true))

        // Standard save mode with data changed
        assertTrue(isDialogSaveEnabled(durationStr = "2.0", rateOrFeeInput = "60.0", pricingMode = PricingMode.PER_HOUR, isDataChanged = true))
        assertFalse(isDialogSaveEnabled(durationStr = "2.0", rateOrFeeInput = "60.0", pricingMode = PricingMode.PER_HOUR, isDataChanged = false))

        // Mark as paid mode ignores isDataChanged guard
        assertTrue(isDialogSaveEnabled(durationStr = "1.5", rateOrFeeInput = "50.0", pricingMode = PricingMode.PER_HOUR, isMarkAsPaidMode = true, isDataChanged = false))
        assertTrue(isDialogSaveEnabled(durationStr = "1.5", rateOrFeeInput = "50.0", pricingMode = PricingMode.PER_HOUR, isMarkAsPaidMode = true, isDataChanged = true))

        // Cancelled status choice allows saving even with empty duration
        assertTrue(isDialogSaveEnabled(durationStr = "", rateOrFeeInput = "", pricingMode = PricingMode.PER_HOUR, isDataChanged = true, statusChoice = LogLessonStatusChoice.CANCELLED))
        assertFalse(isDialogSaveEnabled(durationStr = "", rateOrFeeInput = "", pricingMode = PricingMode.PER_HOUR, isDataChanged = false, statusChoice = LogLessonStatusChoice.CANCELLED))
    }
}
