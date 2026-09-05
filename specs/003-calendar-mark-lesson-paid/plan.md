# Implementation Plan: Mark Lessons as Paid in Calendar

**Branch**: `003-calendar-mark-lesson-paid` | **Date**: 2026-09-04 | **Spec**: [spec.md](file:///home/halit/AndroidStudioProjects/Kora/specs/003-calendar-mark-lesson-paid/spec.md)
**Input**: Feature specification from `/specs/003-calendar-mark-lesson-paid/spec.md`

## Summary

Enable tutors to mark individual tutoring lessons as paid directly from the lesson card in the Calendar screen. This updates the lesson status to `PAID`, sets `paymentTimestamp`, deducts the lesson's monetary value (`duration × effective hourly rate`) from the student's active unpaid payment cycle, and records a corresponding transaction in `PaymentRecord`. For scheduled lessons without duration or cases with unset/zero hourly rates, the system prompts for required duration and rate values. When a lesson is paid, its duration is locked to maintain financial integrity, and an outlined "Revert Payment" button replaces the green action, protected by a confirmation pop-up that reverses the status and removes the payment record.

## Technical Context

**Language/Version**: Kotlin (pinned in `gradle/libs.versions.toml`), JVM target 17  
**Primary Dependencies**: Jetpack Compose + Material 3, Hilt (Dagger), Room Database, Kotlin Coroutines & Flow  
**Storage**: Room (local SQLite database `kora.db`, schema version 9, offline-only per Constitution Principle I)  
**Testing**: JUnit 4 + `kotlinx-coroutines-test` + Cash App Turbine (unit), `compose-ui-test-junit4` (instrumented UI)  
**Target Platform**: Android API 26+ (Min SDK), API 36 (Target/Compile)  
**Project Type**: Mobile Application (Android, single-module Clean Architecture `ui → domain ← data`)  
**Performance Goals**: < 100ms local database transaction latency; instant UI recomposition on payment toggle  
**Constraints**: 100% offline-first; no network permission; full linguistic parity across English (`values/`), Turkish (`values-tr/`), and German (`values-de/`)  
**Scale/Scope**: Single tutor, on-device SQLite database, per-lesson and payment history operations  

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|---|---|---|
| **I. Offline-First & Privacy-First** | ✅ PASS | All mutations persist exclusively to local Room database. Zero network requests, no telemetry, no remote storage. |
| **II. Clean Architecture & Modularity** | ✅ PASS | Inward dependency flow: `ui → domain ← data`. Payment interfaces in `domain/repository/`, Room implementations in `data/`, Composables and ViewModel in `ui/`. Zero framework imports in domain layer. |
| **III. DI via Hilt** | ✅ PASS | Repository injected via `@Binds` in `RepositoryModule`. `CalendarViewModel` uses `@HiltViewModel` and `@Inject constructor`. |
| **IV. Compose UI, a11y & Modern UX** | ✅ PASS | 100% Jetpack Compose Material 3. Min 48×48 dp touch targets. WCAG AA compliant colors (`StatusGreen` `#2E7D32` with white text). |
| **V. MVVM, UDF & UI Event Channels** | ✅ PASS | `CalendarViewModel` exposes immutable `StateFlow`. Composables observe via `collectAsStateWithLifecycle()`. Reversal confirmation handled via explicit dialog state. |
| **VI. Predictable Error Handling** | ✅ PASS | Database exceptions caught in data layer and returned as typed domain results; UI informs tutor gracefully. |
| **VII. i18n & Extensible Formatting** | ✅ PASS | All labels, buttons, dialog titles, and messages externalized to `strings.xml` in EN, TR, DE. Currency formatted via centralized currency utility. |
| **VIII. Platform Security** | ✅ PASS | Zero PII or financial data logged to Logcat. |

**Gate Result**: ✅ All 8 constitutional gates pass.

## Project Structure

### Documentation (this feature)

```text
specs/003-calendar-mark-lesson-paid/
├── plan.md              # This file (/speckit-plan output)
├── spec.md              # Feature specification with clarifications
├── research.md          # Phase 0 output — architectural & design decisions
├── data-model.md        # Phase 1 output — entities, invariants & state transitions
├── contracts/           # Phase 1 output — repository, DAO & UI contracts
│   └── calendar_payment_contract.md
├── checklists/          # Validation checklists
│   └── requirements.md
└── quickstart.md        # Phase 1 output — automated & manual validation scenarios
```

### Source Code Impact

```text
app/src/main/java/com/barutdev/kora/
├── domain/repository/
│   └── PaymentRepository.kt                 # [MODIFY] Add markLessonAsPaid & revertLessonPayment
├── data/local/
│   └── PaymentRecordDao.kt                  # [MODIFY] Add deleteByStudentAndTimestamp & getLatestPaymentRecord
├── data/repository/
│   └── PaymentRepositoryImpl.kt             # [MODIFY] Implement transactional single-lesson payment & revert
├── ui/screens/calendar/
│   ├── CalendarScreen.kt                    # [MODIFY] Add Mark as Paid & Revert buttons, revert pop-up dialog
│   └── CalendarViewModel.kt                 # [MODIFY] Inject PaymentRepository, add payment & revert handlers
└── ui/screens/dashboard/components/
    └── LogLessonDialog.kt                   # [MODIFY] Lock duration field when lesson is in PAID status

app/src/main/res/
├── values/strings.xml                       # [MODIFY] Add EN action & dialog strings
├── values-tr/strings.xml                    # [MODIFY] Add TR action & dialog strings
└── values-de/strings.xml                    # [MODIFY] Add DE action & dialog strings

app/src/test/java/com/barutdev/kora/
├── data/repository/
│   └── PaymentRepositoryImplTest.kt         # [NEW/MODIFY] Unit tests for markLessonAsPaid and revertLessonPayment
└── ui/screens/calendar/
    └── CalendarViewModelTest.kt             # [MODIFY] Test payment state handling & dialog triggers
```

## Complexity Tracking

> **No violations**: Feature strictly conforms to existing architecture, Room schemas, and UI design patterns. No schema migrations required.

## Verification Plan

### Automated Tests
- `com.barutdev.kora.data.repository.PaymentRepositoryImplTest`:
  - Verify `markLessonAsPaid` transitions lesson to `PAID`, sets `paymentTimestamp`, inserts `PaymentRecordEntity`, and updates `StudentEntity.lastPaymentDate`.
  - Verify `revertLessonPayment` transitions lesson back to `COMPLETED`, clears `paymentTimestamp`, deletes matching `PaymentRecordEntity`, and restores `lastPaymentDate`.
  - Verify transaction rollback on failure.
- `com.barutdev.kora.ui.screens.calendar.CalendarViewModelTest`:
  - Verify mark as paid triggers repository call.
  - Verify revert pop-up state is managed correctly.
  - Verify duration lock on paid lesson edit.
- Run tests: `./gradlew testDebugUnitTest`

### Manual Verification
- Execute Scenarios 1–4 from [quickstart.md](file:///home/halit/AndroidStudioProjects/Kora/specs/003-calendar-mark-lesson-paid/quickstart.md) on device/emulator.
