# Research: Refined Improvements & Bug Fixes

## Decision 1: Editable Lesson Duration in "Mark as Paid" Flow

- **Decision**: Update `LogLessonDialog` so that when `isMarkAsPaidMode == true`, the duration `TextField` remains `enabled = true` (only disabled if the underlying lesson is already `LessonStatus.PAID`), and `supportingText` explaining that duration is locked is suppressed when in Mark as Paid mode. Validate that the input duration is a valid positive number before enabling the confirm button.
- **Rationale**: Currently, line 174 of `LogLessonDialog.kt` sets `enabled = lesson.status != LessonStatus.PAID && !isMarkAsPaidMode`. This explicitly disables duration editing in Mark as Paid mode even though `onMarkAsPaid` passes `duration` to `viewModel.onConfirmMarkLessonAsPaid(duration, fee)`. Removing `&& !isMarkAsPaidMode` immediately unlocks the field for editing during payment confirmation, exactly matching user expectation and existing ViewModel logic.
- **Alternatives Considered**: Creating a completely separate dialog for Mark as Paid was rejected because `LogLessonDialog` is already reused and well-tested for this purpose across Calendar and Dashboard.

---

## Decision 2: Standardizing Action Button Ordering & Hierarchy Across Dialogs

- **Decision**: Align action buttons in `LogLessonDialog` (both for past lessons "Log Details" and future lessons "Save Changes") into a unified, consistent row hierarchy:
  - Material 3 `AlertDialog` footer layout:
    - **Dismiss / Secondary actions**: On the left/dismiss slot: `Cancel` (as `TextButton`) followed by `Mark as Not Done` (as `TextButton` or outlined if applicable).
    - **Confirm / Primary action**: On the right/confirm slot: Primary `Button` (`Complete` / `Save Changes` / `Mark as Paid`).
  - Ensure button styling (colors, padding, spacing of 8.dp) is consistent across states.
- **Rationale**: In the previous implementation, the "Log Details" dialog placed "Mark as Not Done" and "Cancel" in the dismiss button area, while "Complete" was in confirmButton. By maintaining a uniform left-to-right flow (`[Cancel] [Mark as Not Done]` -> `[Complete]`) and matching future lesson layout (`[Cancel]` -> `[Save Changes]`), cognitive friction and mis-taps are eliminated.
- **Alternatives Considered**: Stacking buttons vertically was rejected because standard Android Material 3 dialogs use horizontal button rows for compact, thumb-friendly interaction.

---

## Decision 3: Comprehensive Pluralization with Android `<plurals>`

- **Decision**: Replace hardcoded count strings and unit concatenations with Android `<plurals>` across all screens for English (`values`), Turkish (`values-tr`), and German (`values-de`):
  1. `dashboard_payment_rate_info_plurals`:
     - English: one: `%1$s hour x %2$s/hour`, other: `%1$s hours x %2$s/hour`
     - Turkish: other: `%1$s saat x %2$s/saat`
     - German: one: `%1$s Stunde x %2$s/Stunde`, other: `%1$s Stunden x %2$s/Stunde`
  2. `dashboard_completed_lessons_duration_plurals`:
     - English: one: `Duration: %1$s hour`, other: `Duration: %1$s hours`
     - Turkish: other: `Süre: %1$s saat`
     - German: one: `Dauer: %1$s Stunde`, other: `Dauer: %1$s Stunden`
  3. `calendar_lesson_details_duration_plurals`:
     - English: one: `Duration: %1$s hour`, other: `Duration: %1$s hours`
     - Turkish: other: `Süre: %1$s saat`
     - German: one: `Dauer: %1$s Stunde`, other: `Dauer: %1$s Stunden`
  4. `reports_summary_hours_unit_plurals`:
     - English: one: `%1$s hour`, other: `%1$s hours`
     - Turkish: other: `%1$s saat`
     - German: one: `%1$s Stunde`, other: `%1$s Stunden`
  - For plural quantity resolution with decimals (e.g. `1.0` vs `1.5`): If the rounded duration equals `1.0` and fraction is `0`, pass `quantity = 1`; otherwise pass `quantity = 2` (or integer ceil) so that `other` is correctly selected for fractions like `1.5 hours` while injecting the localized formatted decimal string `%1$s`.
- **Rationale**: Adheres to Tullab Constitution Principle VII (Internationalization & Pluralization). German and English require distinct singular (`Stunde`/`hour`) vs plural (`Stunden`/`hours`).
- **Alternatives Considered**: Using custom string concatenation was rejected as it directly violates Constitution Principle VII.

---

## Decision 4: Dynamic ISO 4217 Currency List with Search Filtering

- **Decision**:
  1. Expand currency support from hardcoded `["USD", "EUR", "TRY"]` to all standard active currencies obtained from `Currency.getAvailableCurrencies()`.
  2. Filter out pseudo/funds codes without valid 3-letter uppercase codes, sort by currency code, and extract localized display names and symbols.
  3. Update `CurrencySelectionDialog` in `SettingsScreen.kt` to include a search text field at the top and a scrollable `LazyColumn` of selectable currency options.
  4. In `CurrencyFormatter.kt`, dynamically look up or construct formatters for any ISO 4217 code using `NumberFormat.getCurrencyInstance` with currency-specific symbol and fraction digit support.
- **Rationale**: Fulfills Constitution Principle VII ("Dynamic Preference: Formatting systems MUST be extensible and dynamically respect user preferences and device locale. Architectural rules MUST NOT hardcode supported currencies").
- **Alternatives Considered**: A fixed static list of 25 currencies was rejected because tutors operate in diverse countries globally and Java's `Currency.getAvailableCurrencies()` provides the authoritative standard ISO list out of the box.

---

## Decision 5: Reports Screen Body Title Removal

- **Decision**: Remove the redundant in-body `Text(text = tullabStringResource(id = R.string.reports_title), ...)` on line 270 of `ReportsScreen.kt`.
- **Rationale**: `ReportsScreen` already configures `ScreenScaffoldConfig(topBarConfig = TopBarConfig(title = topBarTitle))` on line 92-94, which renders the title in the top app bar. The second title in the scrollable column wastes vertical space and is visually duplicate.
- **Alternatives Considered**: Keeping it and hiding the top bar title was rejected because all other screens in Tullab (Dashboard, Calendar, Student List) consistently use the top app bar title.

---

## Decision 6: Turkish Lira Currency Symbol (₺) in Reports

- **Decision**: In `CurrencyFormatter.kt` and `rememberCurrencyFormatter` in `ReportsScreen.kt`:
  - When `currencyCode == "TRY"`, ensure the `DecimalFormatSymbols` has `currencySymbol = "₺"` (and international currency symbol `"TRY"`), regardless of whether the active app locale is English, German, or Turkish.
- **Rationale**: On some Android platform versions or non-Turkish system locales, formatting `TRY` with `Locale.US` or `Locale.GERMANY` produces `"TRY 1,234.50"` or `"1.234,50 TRY"` instead of using the Turkish Lira symbol `"₺"`. Explicitly binding `"₺"` to `currencySymbol` for TRY ensures consistent symbol rendering alongside `$` and `€`.
- **Alternatives Considered**: Hardcoding the symbol in UI text widgets was rejected because currency formatting must remain centralized in `CurrencyFormatter` and respect formatting rules.
