# Implementation Plan: Material 3 Standards & Cross-Screen UX Refinement

**Branch**: `010-m3-ux-refinement` | **Date**: 2026-09-07 | **Spec**: [specs/010-m3-ux-refinement/spec.md](spec.md)

**Input**: Feature specification from `specs/010-m3-ux-refinement/spec.md`

## Summary

Unify and elevate the application's UI to strict Material 3 standards:
1. Standardize all text inputs into M3 `OutlinedTextField`.
2. Convert Homework creation modal into an M3 `ModalBottomSheet` and add status filter chips.
3. Clean up top app bars by removing redundant settings gear icons on all sub-screens (keeping it scoped to Student List root).
4. Modernize Student List with a pill-shaped M3 `SearchBar` and full-card tap navigation (removing dual pencil/chevron clutter).
5. Clean Student Detail header (drop "Öğrenci:" prefix) and boost "Mark as Paid" button contrast.
6. Enhance Calendar with a color dot legend, an expandable Speed Dial FAB uniting single and bulk lesson scheduling (removing the competing in-feed bulk button), and an M3 SegmentedButton for lesson status in `LogLessonDialog`.
7. Upgrade Reports chart to display compact values above bars with interactive tap tooltips for full unrounded values.
8. Complete localization in TR, EN, and DE with plurals.

## Technical Context

**Language/Version**: Kotlin 2.0+ (JVM target 17)  
**Primary Dependencies**: Jetpack Compose BOM (pinned in `libs.versions.toml`), Material 3 (`androidx.compose.material3`), Navigation Compose, Hilt  
**Storage**: Room (SQLite) - no schema changes required for this feature  
**Testing**: JUnit4, `kotlinx-coroutines-test`, Cash App Turbine, Compose UI Testing (`compose-ui-test-junit4`)  
**Target Platform**: Android (minSdk 26, targetSdk 36)  
**Project Type**: Android Jetpack Compose Mobile App  
**Performance Goals**: 60 fps smooth Compose animations and transitions; zero jank on list/chart interactions  
**Constraints**: 100% offline-first; WCAG AA compliance (4.5:1 contrast, 48x48 dp touch targets); dynamic font scaling up to 200% without layout breakage  
**Scale/Scope**: 6 primary screens refactored (`StudentListScreen`, `DashboardScreen`, `CalendarScreen`, `HomeworkScreen`, `ReportsScreen`, `SettingsScreen`)  

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **I. Offline-First & Privacy-First**: All UI state operates locally; no network permissions requested.
- [x] **II. Clean Architecture & Modularity**: Changes are strictly confined to `ui/` and resource files (`res/values*`); domain entities and repository contracts remain untouched.
- [x] **III. Dependency Injection via Hilt**: ViewModels continue to use `@HiltViewModel` and constructor injection.
- [x] **IV. Jetpack Compose-Only & Material 3**: 100% Compose using `androidx.compose.material3` tokens and theme styles.
- [x] **V. MVVM & UDF**: Screens adhere to immutable `ScreenUiState` flows and state hoisting.
- [x] **VI. Predictable Error Handling**: No exceptions leaked; user feedback provided via dialogs/snackbars.
- [x] **VII. Localization & Extensible Formatting**: Strings externalized into `strings.xml` for `values`, `values-tr`, and `values-de`; pluralization applied for durations.

## Project Structure

### Documentation (this feature)

```text
specs/010-m3-ux-refinement/
├── spec.md              # Feature specification
├── plan.md              # Implementation plan
├── research.md          # Architecture decisions and research
├── data-model.md        # State models and UI representations
├── quickstart.md        # Validation scenarios
├── contracts/
│   └── ui-contracts.md  # UI component signatures and contracts
└── checklists/
    └── requirements.md  # Quality checklist
```

### Source Code Impact

```text
app/src/main/
├── java/com/barutdev/tullab/
│   ├── navigation/
│   │   └── TullabNavGraph.kt                       # TopBar actions and settings icon scoping
│   ├── ui/
│   │   ├── components/
│   │   │   └── CalendarSpeedDialFab.kt             # [NEW] Expandable Speed Dial FAB
│   │   ├── screens/
│   │   │   ├── student_list/
│   │   │   │   └── StudentListScreen.kt            # Pill SearchBar, full-card click, remove chevron/pencil
│   │   │   ├── dashboard/
│   │   │   │   ├── DashboardScreen.kt              # Clean header, high-contrast button, remove settings icon
│   │   │   │   └── components/
│   │   │   │       └── LogLessonDialog.kt          # M3 SegmentedButton for status, single Save button
│   │   │   ├── calendar/
│   │   │   │   ├── CalendarScreen.kt               # Integrate SpeedDial FAB, remove bulk button, add legend
│   │   │   │   └── components/
│   │   │   │       └── CalendarLegend.kt           # [NEW] Dot color legend
│   │   │   ├── homework/
│   │   │   │   ├── HomeworkScreen.kt               # Filter chips (All/Pending/Completed), ModalBottomSheet trigger
│   │   │   │   └── components/
│   │   │   │       ├── HomeworkFilterChips.kt      # [NEW] Horizontal filter chips row
│   │   │   │       └── HomeworkBottomSheet.kt      # [NEW/REFACTOR] Bottom sheet with OutlinedTextFields
│   │   │   └── reports/
│   │   │       └── ReportsScreen.kt                # Compact values above bars, tap tooltip
│   │   └── theme/
│   └── util/
│       └── CurrencyFormatter.kt                    # Compact currency helper
└── res/
    ├── values/strings.xml                          # English string additions & plural updates
    ├── values-tr/strings.xml                       # Turkish string additions & plural updates
    └── values-de/strings.xml                       # German string additions & plural updates
```

**Structure Decision**: Standard Android Jetpack Compose feature directory layout cleanly separating UI components, screens, and localized resources under `:app`.

## Complexity Tracking

*No constitutional violations. Zero architectural exceptions required.*
