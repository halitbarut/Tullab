# Implementation Plan: Refined Improvements & Bug Fixes

**Branch**: `009-refined-improvements` | **Date**: 2026-09-07 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/009-refined-improvements/spec.md`

## Summary

Delivered PR objectives across Calendar, Dashboard, Reports, Settings, Navigation, and Localization subsystems:
1. **Editable Lesson Duration in "Mark as Paid" Flow**: Enabled duration editing in `LogLessonDialog` before payment confirmation for non-paid lessons with reactive fee recalculation.
2. **Unified Dialog Action Button Hierarchy**: Standardized button ordering to `Cancel` (left), `Mark as Not Done` (center, supporting cancellation of past, present, and scheduled future unpaid lessons), and `Save` / `Complete` / `Mark as Paid` (right).
3. **Ergonomic Modal Bottom Sheet & Segmented Buttons**: Modernized `LogLessonDialog` into a thumb-friendly `ModalBottomSheet` with Material 3 `SingleChoiceSegmentedButtonRow`, side-by-side duration and rate fields, and soft `secondaryContainer` styling.
4. **Independent Lesson Completion Switch**: Introduced dedicated "Mark as completed" Material 3 `Switch` defaulting strictly to `lesson.isCompleted`, plus confirmation warning when modifying past uncompleted lessons.
5. **Notification Re-arming on Uncomplete**: Automatically re-arm lesson reminder notifications via `ScheduleNotificationAlarmsUseCase` whenever an uncompleted lesson transitions back to `SCHEDULED` status in Calendar and Dashboard.
6. **Conditional Duration Persistence**: Ensured `durationInHours` is persisted only for `PER_HOUR` pricing mode and explicitly cleared (`null`) for `FLAT_FEE` pricing mode across ViewModels.
7. **Comprehensive Locale-Aware Duration Pluralization**: Replaced hardcoded string concatenations with Android `<plurals>` and locale-aware `DecimalFormat` across English, German, and Turkish, with explicit `Context`-derived plural resolution.
8. **Dynamic ISO 4217 Currency Support & Localized Sorting**: Dynamically filtered active circulating currencies via `Currency.getAvailableCurrencies()`, resolved display names and symbols using runtime `Locale.getDefault()`, and sorted results using `java.text.Collator`.
9. **Remove Redundant Reports Body Title**: Cleaned up `ReportsScreen` vertical layout by eliminating duplicate in-body title in favor of standard Scaffold top app bar title.
10. **Consistent Turkish Lira (₺) Symbol Guarantee**: Ensured TRY formatting consistently renders `"₺"` regardless of device display locale.
11. **Snackbar Centering & FAB Coordination**: Horizontally centered snackbars across all screens and coordinated FAB clearance with bottom navigation inner padding.

## Technical Context

**Language/Version**: Kotlin 2.0.21, JVM target 17  
**Primary Dependencies**: Jetpack Compose (Compose BOM 2024.09.00), Material 3, Hilt 2.51.1, Jetpack DataStore, Room 2.6.1, Java Currency/NumberFormat  
**Storage**: Room (SQLite), DataStore Preferences (`UserPreferences`)  
**Testing**: JUnit 4, Kotlinx Coroutines Test, Turbine, Compose UI Test JUnit4  
**Target Platform**: Android (Min SDK 26, Target/Compile SDK 36)  
**Project Type**: Native Android Mobile Application  
**Performance Goals**: Instant search filtering in currency dialog (<50ms), smooth 60fps scrolling  
**Constraints**: Offline-first (no internet permissions), 100% Jetpack Compose UI, WCAG AA compliance, full parity across English, Turkish, and German  
**Scale/Scope**: ~6 UI and utility files touched across `ui/screens/` and `util/`  

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **I. Offline-First & Privacy-First Architecture**: PASS. No network requests, all currency data and formatting are evaluated locally on-device.
- **II. Clean Architecture & Meaningful Modularity**: PASS. UI consumes ViewModels and domain models; formatters and helpers isolated in `util/`.
- **III. Dependency Injection via Hilt**: PASS. ViewModels continue to use `@HiltViewModel` and `@Inject constructor`.
- **IV. Jetpack Compose-Only UI, Accessibility & Modern UX**: PASS. Material 3 dialogs, minimum 48x48dp touch targets, semantic descriptions maintained.
- **V. MVVM, Unidirectional Data Flow & UI Event Channels**: PASS. Immutable StateFlows in ViewModels, hoisted state in dialogs.
- **VI. Predictable, Layered Error Handling**: PASS. Duration and currency code validations guard against crashes and format exceptions.
- **VII. Internationalization (i18n) & Extensible Formatting**: PASS. Replaces hardcoded strings with `<plurals>`, maintains parity across EN/TR/DE, and expands currencies dynamically from ISO 4217.
- **VIII. Secrets Management & Android Platform Security**: PASS. No secrets or personal data logged.

## Project Structure

### Documentation (this feature)

```text
specs/009-refined-improvements/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── ui-formatter-contracts.md
└── checklists/
    └── requirements.md
```

### Source Code (repository root)

```text
app/src/main/
├── java/com/barutdev/tullab/
│   ├── domain/model/
│   │   └── CurrencyOption.kt                              # [NEW] Currency model with code, symbol, name
│   ├── ui/screens/
│   │   ├── calendar/
│   │   │   └── CalendarScreen.kt                          # [MODIFY] Connect dialogs & duration formatters
│   │   ├── dashboard/
│   │   │   ├── DashboardScreen.kt                         # [MODIFY] Duration pluralization in completed lessons
│   │   │   └── components/
│   │   │       └── LogLessonDialog.kt                     # [MODIFY] Editable duration in Mark as Paid & button row order
│   │   ├── reports/
│   │   │   └── ReportsScreen.kt                           # [MODIFY] Remove duplicate title & format TRY with ₺
│   │   └── settings/
│   │       ├── SettingsScreen.kt                          # [MODIFY] Searchable currency selection dialog
│   │       └── SettingsViewModel.kt                       # [MODIFY] Provide ISO 4217 currency options with search filter
│   └── util/
│       ├── CurrencyFormatter.kt                           # [MODIFY] Dynamic ISO 4217 format & TRY ₺ symbol guarantee
│       └── DurationFormatter.kt                           # [NEW] Reusable pluralized duration formatter
└── res/
    ├── values/strings.xml                                 # [MODIFY] Add plurals & string resources (EN)
    ├── values-tr/strings.xml                              # [MODIFY] Add plurals & string resources (TR)
    └── values-de/strings.xml                              # [MODIFY] Add plurals & string resources (DE)
```

**Structure Decision**: Standard Android single-app architecture adhering to Clean Architecture packages (`domain/model/`, `ui/screens/`, `util/`, and `res/values*/`).

## Complexity Tracking

*No constitutional violations; no additional complexity added.*
