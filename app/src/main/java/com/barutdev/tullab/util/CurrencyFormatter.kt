package com.barutdev.tullab.util

import com.barutdev.tullab.domain.model.CurrencyOption
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

fun formatCurrency(amount: Double, currencyCode: String, locale: Locale = Locale.getDefault()): String {
    val currency = runCatching { Currency.getInstance(currencyCode) }.getOrNull()
        ?: return amount.toString()
        
    val formatter = NumberFormat.getCurrencyInstance(locale)
    formatter.currency = currency
    
    // Enforce ₺ for TRY
    if (currencyCode == "TRY" && formatter is DecimalFormat) {
        val symbols = formatter.decimalFormatSymbols
        symbols.currencySymbol = "₺"
        formatter.decimalFormatSymbols = symbols
    }
    
    return formatter.format(amount)
}

fun formatCompactCurrency(
    amount: Double,
    currencyCode: String,
    locale: Locale = Locale.getDefault()
): String {
    val symbol = getCurrencySymbol(currencyCode, locale)
    if (amount <= 0.0) return "${symbol}0"

    val symbols = DecimalFormatSymbols(locale)
    return when {
        amount >= 1_000_000 -> {
            val millions = amount / 1_000_000.0
            val pattern = if (millions % 1.0 == 0.0 || millions >= 100) "#,##0" else "#,##0.#"
            val df = DecimalFormat(pattern, symbols)
            "$symbol${df.format(millions)}M"
        }
        amount >= 1_000 -> {
            val thousands = amount / 1_000.0
            val pattern = if (thousands % 1.0 == 0.0 || thousands >= 100) "#,##0" else "#,##0.#"
            val df = DecimalFormat(pattern, symbols)
            "$symbol${df.format(thousands)}K"
        }
        else -> {
            val pattern = if (amount % 1.0 == 0.0) "#,##0" else "#,##0.#"
            val df = DecimalFormat(pattern, symbols)
            "$symbol${df.format(amount)}"
        }
    }
}

fun getCurrencySymbol(currencyCode: String, locale: Locale = Locale.getDefault()): String {
    if (currencyCode == "TRY") return "₺"
    return runCatching { Currency.getInstance(currencyCode).getSymbol(locale) }.getOrDefault(currencyCode)
}

fun getSanitizedCurrencyOptions(locale: Locale = Locale.getDefault()): List<CurrencyOption> {
    val pinnedCodes = listOf("TRY", "USD", "EUR", "GBP", "CHF")
    
    val activeCountryCurrencies = Locale.getISOCountries().mapNotNull { country ->
        runCatching { Currency.getInstance(Locale.Builder().setRegion(country).build()) }.getOrNull()
    }.toSet()

    val allCurrencies = Currency.getAvailableCurrencies()
        .filter { currency ->
            val code = currency.currencyCode
            val isStandardCirculating = currency in activeCountryCurrencies || code == "BGN"
            val isPseudoOrTestCode = code.startsWith("X") && code !in setOf("XAF", "XCD", "XOF", "XPF")
            isStandardCirculating && !isPseudoOrTestCode && currency.numericCode > 0 && currency.numericCode != 999
        }
        .map { currency ->
            val symbol = if (currency.currencyCode == "TRY") "₺" else currency.getSymbol(locale)
            val localizedName = currency.getDisplayName(locale)
            val displayName = "$localizedName (${currency.currencyCode})"
            CurrencyOption(currency.currencyCode, displayName, symbol)
        }
        
    val pinnedOptions = pinnedCodes.mapNotNull { code ->
        allCurrencies.find { it.code == code }
    }
    
    val collator = java.text.Collator.getInstance(locale)
    val remainingOptions = allCurrencies
        .filter { it.code !in pinnedCodes }
        .sortedWith { a, b -> collator.compare(a.displayName, b.displayName) }
        
    return pinnedOptions + remainingOptions
}
