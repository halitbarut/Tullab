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

import android.content.Context
import androidx.compose.runtime.Composable

/**
 * Resolves a pluralized localized duration string for a given hour amount in Compose UI.
 * Uses locale-aware DecimalFormat with the runtime Locale.getDefault().
 *
 * @param durationInHours Lesson duration in decimal hours (e.g. 1.0, 1.5, 2.0)
 * @return Localized duration text with proper singular/plural inflection
 */
@Composable
fun formatDurationHours(durationInHours: Double): String

/**
 * Resolves a pluralized localized duration string for a given hour amount using an explicit Context.
 * Ensures formatting and plural selection are strictly synchronized with the context's configuration locale.
 *
 * @param context Android Context with active configuration locale
 * @param durationInHours Lesson duration in decimal hours (e.g. 1.0, 1.5, 2.0)
 * @return Localized duration text with proper singular/plural inflection
 */
fun formatDurationHours(context: Context, durationInHours: Double): String
```

---

## 3. LogLessonDialog UI Contract

```kotlin
@Composable
fun LogLessonDialog(
    showDialog: Boolean,
    lesson: Lesson?,
    onDismiss: () -> Unit,
    onSave: ((duration: String, notes: String, pricingMode: PricingMode, rateOrFee: String, isCompleted: Boolean) -> Unit)? = null,
    onComplete: ((duration: String, notes: String, pricingMode: PricingMode, rateOrFee: String) -> Unit)? = null,
    onMarkNotDone: ((notes: String) -> Unit)? = null,
    isMarkAsPaidMode: Boolean = false,
    requiresFeePrompt: Boolean = false,
    onMarkAsPaid: ((duration: String, customFee: String) -> Unit)? = null,
    onSaveScheduled: ((duration: String, notes: String, pricingMode: PricingMode, rateOrFee: String) -> Unit)? = null,
    isPastScheduled: Boolean = false,
    currencyCode: String = "USD"
)
```

### Dialog Contract Invariants
- When `isMarkAsPaidMode == true`, the duration field MUST be enabled and allow user input unless `lesson.status == LessonStatus.PAID`.
- When confirming "Mark as Paid", the button is enabled ONLY if duration is a valid positive number (and custom fee is valid if `requiresFeePrompt == true`).
- Button row ordering MUST be:
  - Left: `Cancel` (subtle text button)
  - Center: `Mark as Not Done` (unobtrusive red text button, visible for all unpaid lessons including past, present, and scheduled/future lessons; legitimately cancels scheduled lessons, transitions status to `CANCELLED`, cancels alarms, safely routes null callbacks to dismissal, and resets dialog inputs)
  - Right: `Save` / `Complete` / `Mark as Paid` (solid primary action button)
- Dedicated "Mark as completed" `Switch` defaults strictly to `lesson.isCompleted` (not inferred based on date).
- Editing a past uncompleted lesson presents a warning confirmation dialog before persisting changes.
