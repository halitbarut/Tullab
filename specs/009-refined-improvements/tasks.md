# Tasks: Refined Improvements & Bug Fixes

**Feature**: Refined Improvements & Bug Fixes  
**Branch**: `009-refined-improvements`  
**Specification**: [spec.md](file:///home/halit/AndroidStudioProjects/Tullab/specs/009-refined-improvements/spec.md)  
**Implementation Plan**: [plan.md](file:///home/halit/AndroidStudioProjects/Tullab/specs/009-refined-improvements/plan.md)  

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Shared models and string resources setup across all locales

- [ ] T001 [P] Create `CurrencyOption` model in `app/src/main/java/com/barutdev/tullab/domain/model/CurrencyOption.kt`
- [ ] T002 [P] Define `<plurals>` resources for duration in `app/src/main/res/values/strings.xml`
- [ ] T003 [P] Define `<plurals>` resources for duration in `app/src/main/res/values-tr/strings.xml`
- [ ] T004 [P] Define `<plurals>` resources for duration in `app/src/main/res/values-de/strings.xml`

---

## Phase 2: Foundational (Formatting & Currency Filtering Utilities)

**Purpose**: Centralized utilities for currency filtering, symbol resolution, and plural duration formatting required by screens

**⚠️ CRITICAL**: Must be completed before UI story updates

- [ ] T005 [P] Update `CurrencyFormatter.kt` to dynamically support standard ISO 4217 currencies and enforce Turkish Lira symbol `₺` for `TRY` in `app/src/main/java/com/barutdev/tullab/util/CurrencyFormatter.kt`
- [ ] T006 [P] Create `DurationFormatter.kt` composable helper with fractional and plural duration support using `tullabPluralResource` in `app/src/main/java/com/barutdev/tullab/util/DurationFormatter.kt`
- [ ] T007 [P] Implement currency sanitizer & repository logic in `CurrencyOption` / `CurrencyFormatter.kt` that filters out non-circulating and test/commodity codes (`XXX`, `XTS`, or codes starting with `X`) and pins popular currencies (**TRY**, **USD**, **EUR**, **GBP**, **CHF**) to the top of the available currency list
- [ ] T008 Unit test `CurrencyFormatter`, sanitized currency ordering/filtering, and `DurationFormatter` in `app/src/test/java/com/barutdev/tullab/util/CurrencyFormatterTest.kt`

**Checkpoint**: Core models, plural resources, sanitized currencies with pinned order, and formatting utilities ready.

---

## Phase 3: User Story 1 - Editable Duration on Marking Lesson as Paid (Priority: P1) 🎯 MVP

**Goal**: Allow tutors to edit lesson duration in the "Mark as Paid" dialog before payment confirmation with real-time fee calculation and localized decimal parsing.

**Independent Test**:
- Open Calendar, click "Mark as Paid" on an unpaid lesson.
- Confirm duration text field is enabled with `KeyboardType.Decimal`, edit value (e.g. from 1,0 or 1.0 to 1,5), and verify that total fee updates reactively in real time.
- Confirm payment and verify lesson is marked as paid with updated duration and recalculated fee.

### Implementation for User Story 1
- [ ] T009 [US1] Unlock duration `TextField` and remove locked helper text in `isMarkAsPaidMode` in `app/src/main/java/com/barutdev/tullab/ui/screens/dashboard/components/LogLessonDialog.kt`
- [ ] T010 [US1] Ensure duration text input robustly handles localized decimal formats using `KeyboardType.Decimal` and parses commas and dots via `replace(',', '.').toDoubleOrNull()` in `app/src/main/java/com/barutdev/tullab/ui/screens/dashboard/components/LogLessonDialog.kt`
- [ ] T011 [US1] Implement real-time reactive recalculation and display of total fee whenever duration input changes in the "Mark as Paid" dialog in `app/src/main/java/com/barutdev/tullab/ui/screens/dashboard/components/LogLessonDialog.kt`
- [ ] T012 [US1] Add positive numeric validation to enable `Mark as Paid` confirmation button in `app/src/main/java/com/barutdev/tullab/ui/screens/dashboard/components/LogLessonDialog.kt`
- [ ] T013 [US1] Unit test duration decimal parsing (commas and dots), reactive fee calculation, and positive validation logic in `app/src/test/java/com/barutdev/tullab/ui/screens/dashboard/components/LogLessonDialogValidationTest.kt`

**Checkpoint**: User Story 1 is functional and testable independently as the MVP increment.

---

## Phase 4: User Story 2 - Consistent Log Details & Future Lesson Dialog Action Layout (Priority: P2)

**Goal**: Unify dialog action button row ordering (`Cancel` -> `Mark as Not Done` -> `Complete / Save Changes`) across past and future lessons.

**Independent Test**:
- Open a future scheduled lesson and verify Cancel on left, Save Changes on right.
- Open a past scheduled lesson ("Log Details") and verify Cancel on left, Mark as Not Done in center, Complete on right.

### Implementation for User Story 2
- [ ] T014 [US2] Restructure dialog action buttons in `LogLessonDialog.kt` to standardized row order (`Cancel` first, `Mark as Not Done` middle, `Complete/Save Changes` right) in `app/src/main/java/com/barutdev/tullab/ui/screens/dashboard/components/LogLessonDialog.kt`
- [ ] T015 [US2] Align button paddings, colors, and minimum 48dp touch targets in `app/src/main/java/com/barutdev/tullab/ui/screens/dashboard/components/LogLessonDialog.kt`

**Checkpoint**: Dialog button layouts are consistent across past and future lesson flows.

---

## Phase 5: User Story 3 - Grammatically Correct Pluralization Across Languages (Priority: P2)

**Goal**: Integrate Android `<plurals>` across Dashboard, Calendar, and Reports screens to prevent errors like "1 hours".

**Independent Test**:
- Set language to English and check a 1-hour lesson: displays "1 hour".
- Check a 1.5 or 2-hour lesson: displays "1.5 hours" / "2 hours".
- Test Turkish ("1 saat", "2 saat") and German ("1 Stunde", "2 Stunden").

### Implementation for User Story 3
- [ ] T016 [P] [US3] Update `DashboardScreen.kt` payment cycle and completed lessons duration labels to use `formatDurationHours` / plural resources in `app/src/main/java/com/barutdev/tullab/ui/screens/dashboard/DashboardScreen.kt`
- [ ] T017 [P] [US3] Update `CalendarScreen.kt` lesson detail card duration label to use `formatDurationHours` / plural resources in `app/src/main/java/com/barutdev/tullab/ui/screens/calendar/CalendarScreen.kt`
- [ ] T018 [P] [US3] Update `ReportsScreen.kt` total hours summary card to use pluralized hours resource in `app/src/main/java/com/barutdev/tullab/ui/screens/reports/ReportsScreen.kt`
- [ ] T019 [US3] Add unit tests verifying plural string resolution for singular, plural, and fractional durations across EN, TR, and DE in `app/src/test/java/com/barutdev/tullab/util/DurationPluralizationTest.kt`

**Checkpoint**: All duration displays show grammatically correct forms across all supported languages.

---

## Phase 6: User Story 4 - Expanded ISO 4217 Currency Options (Priority: P3)

**Goal**: Provide sanitized active ISO 4217 currencies with pinned popular currencies (**TRY**, **USD**, **EUR**, **GBP**, **CHF**) and interactive search filter in Settings.

**Independent Test**:
- Open Settings > Currency.
- Verify pinned popular currencies (**TRY**, **USD**, **EUR**, **GBP**, **CHF**) are shown at top.
- Verify test/non-circulating codes (`XXX`, `XTS`, commodities) are excluded.
- Verify search bar is present and filtering by "JPY" or "SAR" displays matching currencies.
- Select a new currency and verify all amounts across the app format with the selected currency.

### Implementation for User Story 4
- [ ] T020 [P] [US4] Update `SettingsViewModel.kt` to expose sanitized ISO 4217 `CurrencyOption` list with pinned popular currencies and search query filtering in `app/src/main/java/com/barutdev/tullab/ui/screens/settings/SettingsViewModel.kt`
- [ ] T021 [US4] Update `CurrencySelectionDialog` in `SettingsScreen.kt` to include search text field, pinned section or top items, and scrollable currency list with radio selection in `app/src/main/java/com/barutdev/tullab/ui/screens/settings/SettingsScreen.kt`
- [ ] T022 [US4] Unit test currency loading, sanitized filtering (no X codes), pinned item ordering, and selection in `app/src/test/java/com/barutdev/tullab/ui/screens/settings/SettingsViewModelTest.kt`

**Checkpoint**: Tutors worldwide can search and select their native ISO 4217 currency with pinned quick access to popular currencies.

---

## Phase 7: User Story 5 - Clean Reports Screen Header (Priority: P3)

**Goal**: Remove duplicate in-body "Reports" title from Reports screen.

**Independent Test**:
- Open Reports screen.
- Verify top app bar displays screen title and scrollable content begins directly with filters and cards without an extra "Reports" heading.

### Implementation for User Story 5
- [ ] T023 [US5] Remove redundant headline `Text(text = tullabStringResource(id = R.string.reports_title))` from `SummaryCards` in `app/src/main/java/com/barutdev/tullab/ui/screens/reports/ReportsScreen.kt`

**Checkpoint**: Reports screen displays a single, clean title in the top app bar.

---

## Phase 8: User Story 6 - Consistent Turkish Lira Symbol (₺) in Total Earnings (Priority: P3)

**Goal**: Ensure the Turkish Lira currency symbol `₺` renders consistently in Reports Total Earnings and charts across all locales.

**Independent Test**:
- Select currency "TRY" in Settings.
- Open Reports in English, German, and Turkish.
- Verify the Total Earnings card displays `₺` (e.g. `₺1.250,00` or `1.250,00 ₺`).

### Implementation for User Story 6
- [ ] T024 [US6] Ensure `rememberCurrencyFormatter` in `ReportsScreen.kt` and `formatCurrency` in `CurrencyFormatter.kt` set `currencySymbol = "₺"` when currency is `TRY` in `app/src/main/java/com/barutdev/tullab/ui/screens/reports/ReportsScreen.kt` and `app/src/main/java/com/barutdev/tullab/util/CurrencyFormatter.kt`
- [ ] T025 [US6] Unit test TRY currency formatting across English, German, and Turkish locales in `app/src/test/java/com/barutdev/tullab/ui/screens/reports/ReportsCurrencyFormattingTest.kt`

**Checkpoint**: Turkish Lira symbol `₺` renders reliably across all app locales.

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: End-to-end regression validation, builds, and automated verification

- [ ] T026 [P] Update string resources and translations across `values/`, `values-tr/`, and `values-de/` for any missing dialog labels or pinned currency titles
- [ ] T027 Execute quickstart test scenarios from `specs/009-refined-improvements/quickstart.md`
- [ ] T028 Run `./gradlew testDebugUnitTest` and verify all tests pass without errors

---

## Dependencies & Execution Order

### Phase Dependencies
- **Phase 1 (Setup)**: Can start immediately.
- **Phase 2 (Foundational)**: Depends on Phase 1 completion; blocks all UI stories.
- **Phase 3 (User Story 1 - MVP)**: Can start after Phase 2.
- **Phase 4 (User Story 2)**: Can start after Phase 2.
- **Phase 5 (User Story 3)**: Can start after Phase 2 (uses `DurationFormatter`).
- **Phase 6 (User Story 4)**: Can start after Phase 2 (uses `CurrencyOption` and sanitized currencies).
- **Phase 7 (User Story 5)**: Can start after Phase 2.
- **Phase 8 (User Story 6)**: Can start after Phase 2.
- **Phase 9 (Polish)**: Runs after all user stories are complete.

### Parallel Opportunities
- T001, T002, T003, T004 in Phase 1 can execute in parallel.
- T005, T006, T007 in Phase 2 can execute in parallel.
- User Stories 1, 2, 3, 4, 5, 6 can be developed independently once Phase 2 is complete.
- T016, T017, T018 in User Story 3 can execute in parallel.

---

## Implementation Strategy

### MVP First (User Story 1 Only)
1. Complete Phase 1 (Setup) and Phase 2 (Foundational).
2. Complete Phase 3 (User Story 1 - Editable Duration with Decimal Support & Reactive Fee Calculation).
3. Verify Mark as Paid flow in Calendar.

### Incremental Delivery
- Add User Story 2 (Unified Dialog Buttons) -> test dialog actions.
- Add User Story 3 (Pluralization) -> test EN/TR/DE duration texts.
- Add User Story 4 (ISO 4217 Currency Selection with Sanitization & Pinned Currencies) -> test search & persistence.
- Add User Story 5 (Clean Reports Header) & User Story 6 (TRY ₺ Symbol) -> test Reports screen.
- Run Phase 9 for final regression testing and `./gradlew testDebugUnitTest`.
