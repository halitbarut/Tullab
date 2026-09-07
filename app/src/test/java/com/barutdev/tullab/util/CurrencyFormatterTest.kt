package com.barutdev.tullab.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.NumberFormat
import java.util.Locale

class CurrencyFormatterTest {

    @Test
    fun `test TRY currency symbol is enforced`() {
        // Enforce a specific locale to ensure consistent testing environments
        Locale.setDefault(Locale.US)
        
        val formatted = formatCurrency(1250.0, "TRY")
        assertTrue("Formatted TRY should contain ₺ symbol", formatted.contains("₺"))
    }

    @Test
    fun `test USD currency symbol is correct`() {
        Locale.setDefault(Locale.US)
        val formatted = formatCurrency(150.0, "USD")
        assertTrue("Formatted USD should contain $ symbol", formatted.contains("$"))
    }

    @Test
    fun `test TRY formatting with tr_TR locale`() {
        Locale.setDefault(Locale("tr", "TR"))
        val formatted = formatCurrency(1250.50, "TRY")
        // In tr_TR, decimal separator is comma, and group separator is dot
        // We just ensure the symbol is ₺ and it contains comma
        assertTrue("Formatted TRY should contain ₺ symbol", formatted.contains("₺"))
        assertTrue("Formatted TRY in tr_TR should contain comma for decimal", formatted.contains(","))
    }

    @Test
    fun `test sanitized currency options do not contain test codes`() {
        val options = getSanitizedCurrencyOptions()
        
        val containsInvalid = options.any { 
            it.code == "XXX" || it.code == "XTS" || (it.code.startsWith("X") && it.code !in setOf("XAF", "XCD", "XOF", "XPF"))
        }
        
        assertFalse("Sanitized currencies should not contain XXX, XTS, or commodity codes starting with X", containsInvalid)
    }
    
    @Test
    fun `test pinned currencies are at the top`() {
        val options = getSanitizedCurrencyOptions()
        val pinnedCodes = listOf("TRY", "USD", "EUR", "GBP", "CHF")
        
        // At least the first few should be the pinned ones in the same order
        // (assuming they exist in the available currencies)
        var matchCount = 0
        for (i in pinnedCodes.indices) {
            if (options[i].code == pinnedCodes[i]) {
                matchCount++
            }
        }
        
        assertTrue("Pinned currencies should appear at the top", matchCount == pinnedCodes.size)
    }
}
