package com.barutdev.tullab.util

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.text.NumberFormat
import java.util.Locale

class CurrencyFormatterTest {

    private lateinit var originalLocale: Locale

    @Before
    fun saveLocale() {
        originalLocale = Locale.getDefault()
    }

    @After
    fun restoreLocale() {
        Locale.setDefault(originalLocale)
    }

    @Test
    fun `test TRY currency symbol is enforced`() {
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
        
        // The first N entries must exactly match the pinned codes in order
        val actualPinnedCodes = options.take(pinnedCodes.size).map { it.code }
        assertEquals("Pinned currencies should appear at the top in order", pinnedCodes, actualPinnedCodes)
    }

    @Test
    fun `test compact currency formatting`() {
        val usLocale = Locale.US
        assertEquals("$0", formatCompactCurrency(0.0, "USD", usLocale))
        assertEquals("$800", formatCompactCurrency(800.0, "USD", usLocale))
        assertEquals("$12.5K", formatCompactCurrency(12500.0, "USD", usLocale))
        assertEquals("$1M", formatCompactCurrency(1000000.0, "USD", usLocale))

        val trLocale = Locale("tr", "TR")
        val tryCompact = formatCompactCurrency(12500.0, "TRY", trLocale)
        assertTrue(tryCompact.startsWith("₺"))
        assertTrue(tryCompact.endsWith("K"))
    }
}
