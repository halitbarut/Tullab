# UI & Formatter Contracts: Refined Improvements & Bug Fixes

## 1. Currency Formatting Contract (`CurrencyFormatter.kt`)

```kotlin
package com.barutdev.tullab.util

import java.util.Locale

/**
 * Formats a monetary amount using the specified ISO 4217 currency code.
 * Ensures proper symbol placement (e.g. ₺ for TRY, $ for USD, € for EUR)
 * and locale-aware number formatting.
 *
 * @param amount Numeric value to format
 * @param currencyCode Valid ISO 4217 currency code
 * @param locale Optional display locale (defaults to current app locale)
 * @return Formatted localized currency string
 */
fun formatCurrency(amount: Double, currencyCode: String, locale: Locale = Locale.getDefault()): String
```

### Contract Guarantees
- For `currencyCode == "TRY"`, the formatted output MUST contain the Turkish Lira symbol `"₺"`.
- For any valid ISO 4217 currency, standard `NumberFormat.getCurrencyInstance` formatting MUST succeed without throwing runtime exceptions.
- If `currencyCode` is invalid or unsupported by the runtime, the function MUST fallback gracefully to `"$amount $currencyCode"`.

---

## 2. Pluralized Duration Formatter Contract (`DurationFormatter.kt`)

```kotlin
package com.barutdev.tullab.util

import androidx.compose.runtime.Composable

/**
 * Resolves a pluralized localized duration string for a given hour amount.
 * Example outputs:
 * - English: "1 hour", "1.5 hours", "2 hours"
 * - German: "1 Stunde", "1,5 Stunden", "2 Stunden"
 * - Turkish: "1 saat", "1,5 saat", "2 saat"
 *
 * @param hours Lesson duration in decimal hours (e.g. 1.0, 1.5, 2.0)
 * @return Localized duration text with proper singular/plural inflection
 */
@Composable
fun formatDurationHours(hours: Double): String
```

---

## 3. LogLessonDialog UI Contract

```kotlin
@Composable
fun LogLessonDialog(
    showDialog: Boolean,
    lesson: Lesson?,
    onDismiss: () -> Unit,
    onComplete: (duration: String, notes: String, pricingMode: PricingMode, rateOrFeeInput: String) -> Unit,
    onMarkNotDone: (notes: String) -> Unit,
    onSaveScheduled: ((duration: String, notes: String, pricingMode: PricingMode, rateOrFeeInput: String) -> Unit)? = null,
    isMarkAsPaidMode: Boolean = false,
    requiresFeePrompt: Boolean = false,
    onMarkAsPaid: ((duration: String, fee: String) -> Unit)? = null
)
```

### Dialog Contract Invariants
- When `isMarkAsPaidMode == true`, the duration field MUST be enabled and allow user input unless `lesson.status == LessonStatus.PAID`.
- When confirming "Mark as Paid", the button is enabled ONLY if duration is a valid positive number (and custom fee is valid if `requiresFeePrompt == true`).
- Button row ordering MUST be:
  - Left: `Cancel`
  - Center: `Mark as Not Done` (only in past lesson log mode)
  - Right: `Complete` / `Save Changes` / `Mark as Paid` (primary action)
