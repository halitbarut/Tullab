package com.barutdev.tullab.util

import com.barutdev.tullab.domain.model.CurrencyOption
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

fun formatCurrency(amount: Double, currencyCode: String): String {
    val currency = runCatching { Currency.getInstance(currencyCode) }.getOrNull()
        ?: return amount.toString()
        
    val locale = Locale.getDefault()
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

fun getCurrencySymbol(currencyCode: String, locale: Locale = Locale.getDefault()): String {
    if (currencyCode == "TRY") return "₺"
    return runCatching { Currency.getInstance(currencyCode).getSymbol(locale) }.getOrDefault(currencyCode)
}

// Comprehensive list of active, circulating ISO 4217 currency codes
private val activeIso4217Currencies = setOf(
    "AED", "AFN", "ALL", "AMD", "ANG", "AOA", "ARS", "AUD", "AWG", "AZN", 
    "BAM", "BBD", "BDT", "BGN", "BHD", "BIF", "BMD", "BND", "BOB", "BRL", 
    "BSD", "BTN", "BWP", "BYN", "BZD", "CAD", "CDF", "CHF", "CLP", "CNY", 
    "COP", "CRC", "CUP", "CVE", "CZK", "DJF", "DKK", "DOP", "DZD", "EGP", 
    "ERN", "ETB", "EUR", "FJD", "FKP", "GBP", "GEL", "GHS", "GIP", "GMD", 
    "GNF", "GTQ", "GYD", "HKD", "HNL", "HTG", "HUF", "IDR", "ILS", "INR", 
    "IQD", "IRR", "ISK", "JMD", "JOD", "JPY", "KES", "KGS", "KHR", "KMF", 
    "KPW", "KRW", "KWD", "KYD", "KZT", "LAK", "LBP", "LKR", "LRD", "LSL", 
    "LYD", "MAD", "MDL", "MGA", "MKD", "MMK", "MNT", "MOP", "MRU", "MUR", 
    "MVR", "MWK", "MXN", "MYR", "MZN", "NAD", "NGN", "NIO", "NOK", "NPR", 
    "NZD", "OMR", "PAB", "PEN", "PGK", "PHP", "PKR", "PLN", "PYG", "QAR", 
    "RON", "RSD", "RUB", "RWF", "SAR", "SBD", "SCR", "SDG", "SEK", "SGD", 
    "SHP", "SLL", "SOS", "SRD", "SSP", "STN", "SYP", "SZL", "THB", "TJS", 
    "TMT", "TND", "TOP", "TRY", "TTD", "TWD", "TZS", "UAH", "UGX", "USD", 
    "UYU", "UZS", "VES", "VND", "VUV", "WST", "XAF", "XCD", "XOF", "XPF", 
    "YER", "ZAR", "ZMW", "ZWL"
)

fun getSanitizedCurrencyOptions(locale: Locale = Locale.getDefault()): List<CurrencyOption> {
    val pinnedCodes = listOf("TRY", "USD", "EUR", "GBP", "CHF")
    
    val allCurrencies = Currency.getAvailableCurrencies()
        .filter { it.currencyCode in activeIso4217Currencies }
        .map { currency ->
            val symbol = if (currency.currencyCode == "TRY") "₺" else currency.symbol
            val localizedName = currency.getDisplayName(locale)
            val displayName = "$localizedName (${currency.currencyCode})"
            CurrencyOption(currency.currencyCode, displayName, symbol)
        }
        
    val pinnedOptions = pinnedCodes.mapNotNull { code ->
        allCurrencies.find { it.code == code }
    }
    
    val remainingOptions = allCurrencies
        .filter { it.code !in pinnedCodes }
        .sortedBy { it.displayName }
        
    return pinnedOptions + remainingOptions
}
