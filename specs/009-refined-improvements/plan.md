# Implementation Plan: Refined Improvements & Bug Fixes

**Branch**: `009-refined-improvements` | **Date**: 2026-09-07 | **Spec**: [spec.md](file:///home/halit/AndroidStudioProjects/Tullab/specs/009-refined-improvements/spec.md)

**Input**: Feature specification from `/specs/009-refined-improvements/spec.md`

## Summary

Deliver 6 focused refinements and bug fixes across the Calendar, Dashboard, Reports, Settings, and Localization subsystems:
1. Enable lesson duration editing in the "Mark as Paid" dialog flow before payment confirmation.
2. Standardize button alignment, ordering, and styling between past lesson "Log Details" and future lesson dialogs.
3. Replace hardcoded duration string concatenations with Android `<plurals>` resources across English, German, and Turkish.
4. Expand currency options in Settings from 3 hardcoded values to standard active ISO 4217 currencies with interactive search filtering.
5. Remove the duplicate in-body "Reports" title from the Reports screen content.
6. Ensure Turkish Lira currency formatting renders the standard symbol (`₺`) consistently across all app languages.

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
