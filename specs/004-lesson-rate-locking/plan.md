# Implementation Plan: Lesson Rate Locking and Historical Payment Calculation

**Branch**: `004-lesson-rate-locking` | **Date**: 2026-09-04 | **Spec**: [spec.md](file:///home/halit/AndroidStudioProjects/Kora/specs/004-lesson-rate-locking/spec.md)  
**Input**: Feature specification from `/specs/004-lesson-rate-locking/spec.md`

## Summary

Prevent retroactive recalculation of completed unpaid lessons when a student's profile hourly rate changes by capturing an explicit, immutable pricing snapshot (`pricingMode`: `PER_HOUR` vs. `FLAT_FEE`, and `rateOrFee`: `Double`) on each lesson. Total lesson monetary value is computed deterministically on demand (`val calculatedValue: Double`) without storing redundant database columns. All outstanding debt and payment calculations across the Student List, Dashboard, Calendar, and Payment Settlement are refactored to sum `Σ lesson.calculatedValue`. Legacy lessons are migrated to schema v10 with their student's rate backfilled.

## Technical Context

**Language/Version**: Kotlin 2.1.10 (JVM Target 17)  
**Primary Dependencies**: Jetpack Compose Material 3, AndroidX Room 2.7.0, Dagger Hilt 2.55, Kotlinx Coroutines 1.10.1  
**Storage**: Room SQLite database upgraded from schema version 9 to 10 (`kora.db`)  
**Testing**: JUnit 4, Cash App Turbine, Kotlinx Coroutines Test, AndroidX Room Testing (`room-testing`)  
**Target Platform**: Android (Min SDK: 26, Target/Compile SDK: 36)  
**Project Type**: Native Android Application (Clean Architecture: `ui → domain ← data`)  
**Performance Goals**: Instant offline calculation (<16ms, maintaining 60fps UI recomposition)  
**Constraints**: 100% offline-first, no network permissions, zero redundant stored financial columns, full linguistic parity (EN, TR, DE)  
**Scale/Scope**: Local database with tens of students and hundreds of lessons per tutor  

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

- [x] **I. Offline-First & Privacy-First Architecture**: All pricing snapshots and calculations are stored exclusively on-device in Room; no network calls or tracking.
- [x] **II. Clean Architecture & Modularity**: Domain models (`Lesson`, `PricingMode`) contain 0 Android imports; data layer encapsulates Room entities and migrations; UI ViewModels consume domain contracts.
- [x] **III. Dependency Injection via Hilt**: All repositories, DAOs, and ViewModels injected using `@Inject` and `@HiltViewModel`.
- [x] **IV. Jetpack Compose-Only UI & Accessibility**: 100% Compose/Material 3; touch targets ≥48×48 dp; dynamic scaling supported; all labels externalized.
- [x] **V. MVVM & UDF**: Immutable `StateFlow<ScreenUiState>`; events dispatched to ViewModels; one-time events via buffered `Channel`.
- [x] **VI. Predictable Error Handling**: DB exceptions mapped at data layer; safe domain fallbacks.
- [x] **VII. Internationalization & Formatting**: All strings externalized to `strings.xml` for English, Turkish, and German; monetary amounts formatted via `CurrencyFormatter`.
- [x] **VIII. Secrets & Security**: Zero PII in logcat; release shrinking enabled.
- [x] **Database Evolution**: Room schema incremented from 9 to 10 with manual `MIGRATION_9_10`, exported schemas committed, and migration tests implemented.

## Project Structure

### Documentation (this feature)

```text
specs/004-lesson-rate-locking/
├── plan.md              # This implementation plan
├── research.md          # Phase 0 architectural decisions & rationales
├── data-model.md        # Phase 1 domain models, schema v10, and transitions
├── quickstart.md        # Phase 1 verification and manual test walkthrough
├── contracts/           # Phase 1 repository & UI dialog contracts
│   ├── lesson-repository.md
│   ├── payment-repository.md
│   └── ui-dialogs.md
├── checklists/
│   └── requirements.md  # Specification quality checklist (16/16 passed)
└── spec.md              # Feature specification
```

### Source Code Layout

```text
app/src/main/
├── java/com/barutdev/kora/
│   ├── domain/
│   │   ├── model/
│   │   │   ├── PricingMode.kt             # [NEW] Enum: PER_HOUR, FLAT_FEE
│   │   │   └── Lesson.kt                  # [MODIFY] Add pricingMode, rateOrFee, calculatedValue
│   │   └── repository/
│   │       ├── LessonRepository.kt        # [MODIFY] hasScheduledLessons, updateScheduledLessonsRate
│   │       └── PaymentRepository.kt       # Contract documentation
│   ├── data/
│   │   ├── local/
│   │   │   ├── entity/
│   │   │   │   └── LessonEntity.kt        # [MODIFY] Add pricingMode, rateOrFee
│   │   │   ├── migrations/
│   │   │   │   └── StudentMigrations.kt   # [MODIFY] Add MIGRATION_9_10
│   │   │   ├── PricingModeConverter.kt    # [NEW] Room TypeConverter for PricingMode
│   │   │   ├── LessonDao.kt               # [MODIFY] Scheduled lessons count & rate update queries
│   │   │   └── KoraDatabase.kt            # [MODIFY] Increment version to 10
│   │   ├── mapper/
│   │   │   └── LessonMapper.kt            # [MODIFY] Map pricingMode and rateOrFee
│   │   └── repository/
│   │       ├── LessonRepositoryImpl.kt    # [MODIFY] Implement scheduled rate update & checks
│   │       └── PaymentRepositoryImpl.kt   # [MODIFY] Refactor math to sum calculatedValue
│   └── ui/screens/
│       ├── student_list/
│       │   └── StudentListViewModel.kt    # [MODIFY] Sum completedLessons.calculatedValue
│       ├── dashboard/
│       │   ├── DashboardViewModel.kt      # [MODIFY] Sum completedLessons.calculatedValue
│       │   └── components/
│       │       ├── AddLessonDialog.kt     # [MODIFY] Add pricing mode toggle & rate input
│       │       └── LogLessonDialog.kt     # [MODIFY] Add pricing mode toggle & rate input
│       ├── calendar/
│       │   └── CalendarViewModel.kt       # [MODIFY] Pre-fill lesson rate with student's active rate
│       └── student_profile/
│           ├── EditStudentProfileViewModel.kt  # [MODIFY] Prompt if scheduled lessons exist on rate change
│           ├── EditStudentProfileScreen.kt     # [MODIFY] Show confirmation dialog if triggered
│           └── components/
│               └── ScheduledLessonsRatePromptDialog.kt # [NEW] Confirmation dialog
└── res/
    ├── values/strings.xml                 # [MODIFY] English strings for pricing modes & dialogs
    ├── values-tr/strings.xml              # [MODIFY] Turkish strings
    └── values-de/strings.xml              # [MODIFY] German strings

app/src/test/java/com/barutdev/kora/
├── domain/model/
│   └── LessonTest.kt                     # [NEW] Test calculatedValue under PER_HOUR and FLAT_FEE
├── data/repository/
│   ├── LessonRepositoryImplTest.kt       # [MODIFY] Test non-retroactive snapshot persistence
│   └── PaymentRepositoryImplTest.kt      # [MODIFY] Test multi-rate and flat-fee cycle settlements
└── ui/screens/
    ├── student_list/StudentListViewModelTest.kt # [MODIFY] Test non-retroactive debt calculation
    ├── dashboard/DashboardViewModelTest.kt       # [MODIFY] Test non-retroactive due amount
    └── student_profile/EditStudentProfileViewModelTest.kt # [MODIFY] Test scheduled lesson prompt

app/src/androidTest/java/com/barutdev/kora/data/local/
└── Migration9to10Test.kt                  # [NEW] Automated Room migration test from v9 to v10
```

## Complexity Tracking

> **No Constitution Violations**: The architecture strictly complies with all constitutional principles, clean architecture boundaries, and Room database evolution standards.
