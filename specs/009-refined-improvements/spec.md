# Feature Specification: Refined Improvements & Bug Fixes

**Feature Branch**: `009-refined-improvements`

**Created**: 2026-09-07

**Status**: Draft

**Input**: User description: "Refined Improvements & Bug Fixes: Calendar (Bug): Fix duration text field being disabled when clicking "Mark as Paid". Users should be able to edit the lesson duration before confirming payment. Calendar (UI): Fix button alignment and order in the "Log Details" dialog for past lessons to match future lesson dialog styling. Localization: Implement Android <plurals> string resources across all screens (Dashboard, Calendar, etc.) to resolve pluralization errors like "1 hours" across all supported languages. Currencies: Expand supported currency options by implementing a standard ISO 4217 currency list. Reports (UI): Remove the duplicate "Reports" title from the screen body; keep only the top app bar title. Reports (Formatting): Ensure Turkish Lira displays with its currency symbol (₺) in the Total Earnings section, matching the behavior of $ and €."

## Clarifications

### Session 2026-09-07
- Q: How should the expanded ISO 4217 currency selection be presented in the Settings dialog? → A: Full dynamic set from `Currency.getAvailableCurrencies()` with a search filter field in the picker dialog.
- Q: How should the action buttons in the "Log Details" dialog be aligned and ordered to match future lesson dialog styling? → A: Same row order with Cancel first, then Mark as Not Done, then Complete (or Cancel then Save Changes).
- Q: How should fractional hours (such as 1.5 or 0.5) be handled with Android plurals resources? → A: Quantize to ceiling integer (or integer parts) for plurals, with 1.0 mapping to singular and non-integer values using the plural quantity string with the formatted decimal number.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Editable Duration on Marking Lesson as Paid (Priority: P1)

As a tutor marking a completed lesson as paid, I want the duration field to be editable in the "Mark as Paid" dialog so that I can adjust the actual lesson duration prior to payment confirmation without having to cancel and separately edit the lesson.

**Why this priority**: Directly resolves a functional blocker/bug where tutors cannot log accurate lesson duration at the exact moment of payment confirmation.

**Independent Test**:
- Open the Calendar screen, select an unpaid scheduled or completed lesson, and tap "Mark as Paid".
- Verify that the duration text field is enabled and allows editing numeric duration (e.g., changing 1.0 to 1.5).
- Confirm payment and verify that the updated duration and recalculation are saved.

**Acceptance Scenarios**:
1. **Given** an unpaid lesson in Calendar or Dashboard, **When** the user taps "Mark as Paid", **Then** the duration text field is enabled, editable, and does not show locked helper text.
2. **Given** the user changes the lesson duration in the "Mark as Paid" dialog, **When** the user taps "Mark as Paid" confirmation, **Then** the updated duration is applied to the lesson along with the payment status.

---

### User Story 2 - Consistent Log Details & Future Lesson Dialog Action Layout (Priority: P2)

As a tutor logging or editing lesson details, I want dialog action buttons (e.g., Save/Complete, Mark as Not Done, Cancel) in the past lesson "Log Details" dialog to follow the identical alignment, order, and styling conventions as the future lesson dialog.

**Why this priority**: Inconsistent button ordering and visual hierarchy across dialog variants causes cognitive friction and mis-taps.

**Independent Test**:
- Open a future scheduled lesson and note the button layout (action button hierarchy and secondary dismiss/cancel options).
- Open a past scheduled lesson ("Log Details") and verify that button ordering, alignment, and spacing match the future lesson dialog (Cancel first, then Mark as Not Done, then Complete).

**Acceptance Scenarios**:
1. **Given** a past scheduled lesson opened via "Log Details", **When** viewing the dialog action area, **Then** the action buttons follow the same row order and styling conventions as future lesson dialogs: Cancel on the left, Mark as Not Done in the middle, and Complete on the right.

---

### User Story 3 - Grammatically Correct Pluralization Across Languages (Priority: P2)

As an international tutor using Tullab in English, Turkish, or German, I want all counts and durations to display with correct grammatical plural forms (e.g., "1 hour" vs. "2 hours") across all screens (Dashboard, Calendar, Reports).

**Why this priority**: Eliminates awkward strings like "1 hours" that undermine professionalism, adhering strictly to Tullab Constitution Principle VII.

**Independent Test**:
- Set language to English and create a lesson with a 1-hour duration; verify the UI displays "1 hour" instead of "1 hours".
- Test with 0, 1, 1.5, and multiple hours/items across Dashboard, Calendar, and Reports in English, Turkish, and German.

**Acceptance Scenarios**:
1. **Given** a lesson with duration of 1 hour, **When** viewing lesson duration chips or labels in Dashboard and Calendar, **Then** the text displays singular "1 hour" (or language-appropriate singular form).
2. **Given** a lesson or report with duration greater than 1, zero, or fractional (e.g. 1.5), **When** viewing duration text, **Then** the appropriate plural form is displayed using Android `<plurals>` resources with formatted decimal numbers.

---

### User Story 4 - Expanded ISO 4217 Currency Options (Priority: P3)

As a private tutor operating outside the US, Eurozone, or Turkey, I want to select my local currency from a comprehensive list of standard ISO 4217 currencies in Settings, so that all rates, earnings, and logs reflect my currency.

**Why this priority**: Expands Tullab's international usability beyond the initial 3 hardcoded currencies (USD, EUR, TRY) as mandated by Constitution Principle VII.

**Independent Test**:
- Open Settings and tap Currency.
- Verify that a searchable dialog of all valid ISO 4217 currencies is presented.
- Type in the search box to find a currency (e.g., "GBP", "JPY", "SAR").
- Select a currency and verify that the selected currency is persisted and formatted appropriately across the app.

**Acceptance Scenarios**:
1. **Given** the Settings currency picker, **When** opening the dialog, **Then** users can search and select from all available ISO 4217 currencies displaying their currency code, symbol, and display name.
2. **Given** a selected currency code, **When** viewing prices or rates on Student List, Calendar, and Reports, **Then** the formatting system formats amounts in that currency.

---

### User Story 5 - Clean Reports Screen Header (Priority: P3)

As a tutor viewing Reports, I want the duplicate "Reports" title inside the scrollable content area removed so that only the top app bar displays the screen title and screen real estate is optimized.

**Why this priority**: Polish and visual clarity, avoiding redundant headers and wasted vertical space.

**Independent Test**:
- Navigate to the Reports screen.
- Verify that the top app bar displays "Reports" (or localized equivalent).
- Verify that the scrollable body content begins directly with range filters and summary cards without a second "Reports" title.

**Acceptance Scenarios**:
1. **Given** the Reports screen, **When** the screen is rendered, **Then** the top app bar shows the screen title and no duplicate headline appears above the summary cards.

---

### User Story 6 - Consistent Turkish Lira Symbol (₺) in Total Earnings (Priority: P3)

As a tutor in Turkey using TRY currency, I want the Total Earnings section in Reports to display the Turkish Lira currency symbol (₺) rather than the fallback text or inconsistent symbol, matching the visual presentation of $ and €.

**Why this priority**: Turkish Lira users should receive the same high-fidelity formatting with symbol placement as USD ($) and EUR (€) users.

**Independent Test**:
- Set currency preference to "TRY".
- Navigate to Reports.
- Verify that the Total Earnings card displays the amount formatted with "₺" (e.g., "₺1.250,00" or "1.250,00 ₺" per standard Turkish locale rules).

**Acceptance Scenarios**:
1. **Given** user currency set to TRY, **When** viewing the Total Earnings summary card and reports charts, **Then** the Turkish Lira symbol (₺) is displayed in the formatted currency string.

---

### Edge Cases

- **Fractional Durations with Plurals**: When duration is fractional (e.g., 0.5 hours or 1.5 hours), plural rules handle fractions by formatting the decimal number into the plural `<plurals>` item.
- **Uncommon ISO Currencies**: Currencies without distinctive symbols should gracefully fall back to standard ISO 4217 formatting (e.g., "CHF 100") without crashing or throwing formatting errors.
- **Empty / Invalid Duration in Mark as Paid**: If the user clears the duration field or enters an invalid number in the "Mark as Paid" dialog, the confirmation button must be disabled until a valid positive duration is entered.
- **Zero Hours / Negative Numbers**: Pluralization and duration inputs must strictly guard against negative numbers.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST allow tutors to edit the duration field in the "Mark as Paid" dialog when confirming payment for a lesson, keeping the field enabled and free of locked helper text.
- **FR-002**: The system MUST validate that the entered duration in the "Mark as Paid" dialog is a valid positive number before enabling the confirmation action.
- **FR-003**: The "Log Details" dialog action buttons MUST match the ordering, alignment, and spacing hierarchy of the future scheduled lesson dialog: Cancel on the left, Mark as Not Done in the middle, and Complete/Save Changes on the right.
- **FR-004**: The system MUST use Android `<plurals>` string resources for count- and duration-dependent messages across Dashboard, Calendar, and Reports.
- **FR-005**: All plural string resources MUST be defined with linguistic completeness in English (`values`), Turkish (`values-tr`), and German (`values-de`).
- **FR-006**: The system MUST provide an expanded selection of standard ISO 4217 currencies derived from `Currency.getAvailableCurrencies()` with a search filter field in the Settings currency picker dialog.
- **FR-007**: The currency formatting utility MUST format any valid ISO 4217 currency code dynamically without throwing exceptions.
- **FR-008**: The Reports screen body MUST NOT contain a duplicate headline title, relying exclusively on the Scaffold TopAppBar for the "Reports" title.
- **FR-009**: The Reports currency formatting for Turkish Lira (TRY) MUST display the standard Turkish Lira symbol (₺) consistently alongside the formatted amount.

### Key Entities

- **Lesson**: Represents a tutoring session, containing duration (in hours), status (SCHEDULED, COMPLETED, PAID), pricing mode (PER_HOUR, FLAT_FEE), rate or fee, and payment timestamp.
- **UserPreferences**: Persisted user configuration including selected language (`languageCode`), preferred ISO 4217 currency (`currencyCode`), default hourly rate, and notification settings.
- **CurrencyOption**: Metadata representation of an ISO 4217 currency including code (e.g., `USD`, `TRY`, `EUR`, `GBP`), symbol, and localized display name.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of "Mark as Paid" flows allow users to modify lesson duration before confirming payment.
- **SC-002**: Zero hardcoded count/hour concatenations remain in user-visible UI strings across Dashboard, Calendar, and Reports screens.
- **SC-003**: 100% of supported language locales (en, tr, de) render grammatically correct singular and plural forms for duration displays (0, 1, 1.5, and 2+ hours).
- **SC-004**: Users can search and choose from all standard active ISO 4217 currencies in Settings, and changes persist across app restarts.
- **SC-005**: The Reports screen displays zero duplicate titles in the main content area.
- **SC-006**: Reports Total Earnings formatted in TRY displays the currency symbol `₺` across all supported app languages.

## Assumptions

- ISO 4217 currency list is derived from `java.util.Currency.getAvailableCurrencies()` filtered to non-pseudo valid currencies, sorted by currency code / name, and includes a search query filter.
- Standard Android `QuantityString` / `tullabPluralResource` resolution handles integer quantities; for decimal/fractional hours (e.g. 1.5), standard conventions format the numeric value with the plural quantity string.
- Turkish Lira symbol `₺` is standard in modern Android ICU / Java Locale data for `tr_TR`, and the custom currency formatter in `CurrencyFormatter.kt` and `rememberCurrencyFormatter` ensures the symbol displays even when the app language is set to English or German.
