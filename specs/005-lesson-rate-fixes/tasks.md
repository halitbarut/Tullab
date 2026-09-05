# Tasks: Lesson Rate Management, Multi-Rate Payment Breakdown, and Calendar Rate Editing

**Input**: Design documents from `specs/005-lesson-rate-fixes/`  
**Prerequisites**: [plan.md](file:///home/halit/AndroidStudioProjects/Kora/specs/005-lesson-rate-fixes/plan.md), [spec.md](file:///home/halit/AndroidStudioProjects/Kora/specs/005-lesson-rate-fixes/spec.md), [research.md](file:///home/halit/AndroidStudioProjects/Kora/specs/005-lesson-rate-fixes/research.md), [data-model.md](file:///home/halit/AndroidStudioProjects/Kora/specs/005-lesson-rate-fixes/data-model.md), [contracts/](file:///home/halit/AndroidStudioProjects/Kora/specs/005-lesson-rate-fixes/contracts/)

**Tests**: Unit tests are included for ViewModels and domain computation models per Kora testing standards (testing state transitions, Turbine flow verification, and calculation accuracy).

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `- [ ] [TaskID] [P?] [Story?] Description with file path`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (`[US1]`, `[US2]`, `[US3]`)
- Every task includes an exact file path.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Externalize and localize all required strings across English, Turkish, and German resource tables.

- [ ] T001 [P] Add rate change dialog, payment breakdown tier, and calendar save action strings to app/src/main/res/values/strings.xml
- [ ] T002 [P] Add Turkish translations for rate change dialog, payment breakdown tier, and calendar save action strings to app/src/main/res/values-tr/strings.xml
- [ ] T003 [P] Add German translations for rate change dialog, payment breakdown tier, and calendar save action strings to app/src/main/res/values-de/strings.xml

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Define common data structures, read models, and shared dialog contract additions required by all user stories.

**⚠️ CRITICAL**: Must be completed before user story implementation begins.

- [ ] T004 [P] Define `ScheduledLessonsScope` enum (`PAST_ONLY`, `FUTURE_ONLY`, `MIXED`) in app/src/main/java/com/barutdev/kora/ui/screens/student_profile/ScheduledLessonsScope.kt
- [ ] T005 [P] Define `PaymentBreakdownTier` read model and `computePaymentBreakdownTiers` aggregation function in app/src/main/java/com/barutdev/kora/ui/screens/dashboard/PaymentBreakdownTier.kt
- [ ] T006 Extend `LogLessonDialog` parameters to accept `onSaveScheduled` callback and future lesson mode in app/src/main/java/com/barutdev/kora/ui/screens/dashboard/components/LogLessonDialog.kt

**Checkpoint**: Shared models and dialog contracts defined; user stories can now proceed.

---

## Phase 3: User Story 1 - Context-Aware and Fully Localized Rate Change Confirmation Dialog (Priority: P1) 🎯 MVP

**Goal**: Accurately categorize uncompleted scheduled lessons into past, future, or mixed relative to current date, adapt dialog messaging accordingly, and switch string resolution to `koraStringResource` to honor the active in-app language (`values`, `values-tr`, `values-de`).

**Independent Test**: Schedule an uncompleted lesson dated before today, set app language to English, update the student profile rate, and verify that the confirmation prompt appears in English and explicitly refers to past uncompleted lessons rather than future scheduled lessons.

### Tests for User Story 1 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T007 [P] [US1] Add unit tests for `ScheduledLessonsScope` date evaluation and rate cascade decisions in app/src/test/java/com/barutdev/kora/ui/screens/student_profile/EditStudentProfileViewModelTest.kt

### Implementation for User Story 1

- [ ] T008 [US1] Update `StudentProfileUiState` to include `scheduledLessonsScope`, `scheduledLessonsCount`, and `isScheduledLessonsPromptVisible` in app/src/main/java/com/barutdev/kora/ui/screens/student_profile/StudentProfileUiState.kt
- [ ] T009 [US1] Implement date-based scope determination (`PAST_ONLY`, `FUTURE_ONLY`, `MIXED`) and prompt triggering in app/src/main/java/com/barutdev/kora/ui/screens/student_profile/EditStudentProfileViewModel.kt
- [ ] T010 [US1] Refactor `ScheduledLessonsRatePromptDialog` to use `koraStringResource` with `LocalLocale.current` and dynamically render texts based on `ScheduledLessonsScope` in app/src/main/java/com/barutdev/kora/ui/screens/student_profile/components/ScheduledLessonsRatePromptDialog.kt
- [ ] T011 [US1] Connect `ScheduledLessonsRatePromptDialog` with `EditStudentProfileViewModel` actions in app/src/main/java/com/barutdev/kora/ui/screens/student_profile/EditStudentProfileScreen.kt

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently.

---

## Phase 4: User Story 2 - Accurate Multi-Rate Payment Cycle Breakdown on Dashboard (Priority: P1)

**Goal**: Group completed unpaid lessons by pricing mode and applicable rate, and render a responsive two-column breakdown on the Dashboard payment tracking card where tier subtotals sum exactly to the total amount due.

**Independent Test**: Complete 5 hours of lessons at 200 TL and 2 hours of lessons at 400 TL in the same payment cycle; verify Dashboard displays 1,800 TL total with separate rows for `5 hours × 200 TL` (1,000 TL) and `2 hours × 400 TL` (800 TL).

### Tests for User Story 2 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T012 [P] [US2] Add unit tests for `computePaymentBreakdownTiers` multi-rate grouping, single-rate aggregation, and subtotal consistency in app/src/test/java/com/barutdev/kora/ui/screens/dashboard/DashboardViewModelTest.kt

### Implementation for User Story 2

- [ ] T013 [US2] Add `rateBreakdownTiers: List<PaymentBreakdownTier>` to `DashboardUiState` in app/src/main/java/com/barutdev/kora/ui/screens/dashboard/DashboardUiState.kt
- [ ] T014 [US2] Update `DashboardViewModel` to compute and expose `rateBreakdownTiers` from unpaid completed lessons in app/src/main/java/com/barutdev/kora/ui/screens/dashboard/DashboardViewModel.kt
- [X] T015 [US2] Update `PaymentTrackingCard` composable to render multi-tier breakdown rows with responsive wrapping and formula/subtotal columns in app/src/main/java/com/barutdev/kora/ui/screens/dashboard/DashboardScreen.kt

**Checkpoint**: At this point, User Stories 1 and 2 should both work independently.

---

## Phase 5: User Story 3 - Editing and Saving Future Planned Lesson Rates from Calendar (Priority: P2)

**Goal**: Allow tutors to edit and persist rate, pricing mode, duration, and notes of future scheduled lessons directly from the Calendar screen while keeping `SCHEDULED` status intact.

**Independent Test**: Open an upcoming scheduled lesson in the Calendar, change its rate, tap "Save Changes", and verify the card displays the updated rate while the status remains `SCHEDULED`.

### Tests for User Story 3 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [X] T016 [P] [US3] Add unit tests for `onSaveScheduledLesson` updating pricing attributes while preserving `SCHEDULED` status in app/src/test/java/com/barutdev/kora/ui/screens/calendar/CalendarViewModelTest.kt

### Implementation for User Story 3

- [X] T017 [US3] Implement `onSaveScheduledLesson` handler in app/src/main/java/com/barutdev/kora/ui/screens/calendar/CalendarViewModel.kt
- [X] T018 [US3] Update `LogLessonDialog` contextual actions: replace disabled "Complete Lesson" button with "Save Changes" for future scheduled lessons in app/src/main/java/com/barutdev/kora/ui/screens/dashboard/components/LogLessonDialog.kt
- [X] T019 [US3] Wire `onSaveScheduled` lambda in `CalendarScreen` to invoke `CalendarViewModel.onSaveScheduledLesson` in app/src/main/java/com/barutdev/kora/ui/screens/calendar/CalendarScreen.kt

**Checkpoint**: All user stories (US1, US2, US3) are independently functional.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Verification, string parity checks, and test suite execution across all modules.

- [X] T020 [P] Verify 100% localization key parity across app/src/main/res/values/strings.xml, app/src/main/res/values-tr/strings.xml, and app/src/main/res/values-de/strings.xml
- [X] T021 Execute full unit test suite across affected screens via `./gradlew testDebugUnitTest` targeting app/src/test/java/com/barutdev/kora/ui/screens/
- [X] T022 Validate manual end-to-end verification flows documented in specs/005-lesson-rate-fixes/quickstart.md

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately.
- **Foundational (Phase 2)**: Depends on Phase 1 strings — BLOCKS all user stories.
- **User Stories (Phases 3, 4, 5)**:
  - US1 (Phase 3) depends on Foundational (Phase 2).
  - US2 (Phase 4) depends on Foundational (Phase 2).
  - US3 (Phase 5) depends on Foundational (Phase 2).
  - US1, US2, and US3 are orthogonal and can proceed sequentially (US1 → US2 → US3) or in parallel.
- **Polish (Phase 6)**: Depends on completion of all user story implementations.

### User Story Dependencies

- **User Story 1 (P1)**: Modifies `EditStudentProfile` flow and `ScheduledLessonsRatePromptDialog`. No dependencies on US2 or US3.
- **User Story 2 (P1)**: Modifies `Dashboard` payment tracking card and tier calculations. No dependencies on US1 or US3.
- **User Story 3 (P2)**: Modifies `Calendar` and `LogLessonDialog`. No dependencies on US1 or US2.

### Within Each User Story

- Tests MUST be written and fail before implementation.
- Models before services and ViewModels.
- ViewModel business logic before UI components.
- Dialog and screen UI integration after ViewModel logic.
- Story complete and validated before moving to next priority.

### Parallel Opportunities

- Within Phase 1: `T001`, `T002`, and `T003` can run in parallel (different XML resource files).
- Within Phase 2: `T004` and `T005` can run in parallel (different new model files).
- Across User Stories: Once Phase 2 completes, Phase 3 (US1), Phase 4 (US2), and Phase 5 (US3) can proceed independently in parallel.
- Test tasks `T007`, `T012`, and `T016` can each be created prior to their respective implementation tasks.

---

## Parallel Execution Examples

### Parallel Example: Phase 1 Setup
```bash
# Launch string externalization across all target locales simultaneously:
Task: "Add rate change dialog, payment breakdown tier, and calendar save action strings to app/src/main/res/values/strings.xml"
Task: "Add Turkish translations for rate change dialog, payment breakdown tier, and calendar save action strings to app/src/main/res/values-tr/strings.xml"
Task: "Add German translations for rate change dialog, payment breakdown tier, and calendar save action strings to app/src/main/res/values-de/strings.xml"
```

### Parallel Example: Phase 2 Foundational
```bash
# Launch shared model definitions together:
Task: "Define ScheduledLessonsScope enum (PAST_ONLY, FUTURE_ONLY, MIXED) in app/src/main/java/com/barutdev/kora/ui/screens/student_profile/ScheduledLessonsScope.kt"
Task: "Define PaymentBreakdownTier read model and computePaymentBreakdownTiers aggregation function in app/src/main/java/com/barutdev/kora/ui/screens/dashboard/PaymentBreakdownTier.kt"
```

### Parallel Example: User Story 1
```bash
# Launch unit tests and UI state updates in parallel:
Task: "Add unit tests for ScheduledLessonsScope date evaluation and rate cascade decisions in app/src/test/java/com/barutdev/kora/ui/screens/student_profile/EditStudentProfileViewModelTest.kt"
Task: "Update StudentProfileUiState to include scheduledLessonsScope, scheduledLessonsCount, and isScheduledLessonsPromptVisible in app/src/main/java/com/barutdev/kora/ui/screens/student_profile/StudentProfileUiState.kt"
```

### Parallel Example: User Story 2
```bash
# Launch unit tests and UI state updates in parallel:
Task: "Add unit tests for computePaymentBreakdownTiers multi-rate grouping, single-rate aggregation, and subtotal consistency in app/src/test/java/com/barutdev/kora/ui/screens/dashboard/DashboardViewModelTest.kt"
Task: "Add rateBreakdownTiers: List<PaymentBreakdownTier> to DashboardUiState in app/src/main/java/com/barutdev/kora/ui/screens/dashboard/DashboardUiState.kt"
```

### Parallel Example: User Story 3
```bash
# Launch unit tests and ViewModel handler in parallel:
Task: "Add unit tests for onSaveScheduledLesson updating pricing attributes while preserving SCHEDULED status in app/src/test/java/com/barutdev/kora/ui/screens/calendar/CalendarViewModelTest.kt"
Task: "Implement onSaveScheduledLesson handler in app/src/main/java/com/barutdev/kora/ui/screens/calendar/CalendarViewModel.kt"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (`T001`, `T002`, `T003`)
2. Complete Phase 2: Foundational (`T004`, `T005`, `T006`)
3. Complete Phase 3: User Story 1 (`T007` through `T011`)
4. **STOP and VALIDATE**: Verify prompt date scope and in-app localization in profile editor per Quickstart Scenario 1.
5. Ready for immediate deployment/release as an MVP increment.

### Incremental Delivery

1. **Increment 1 (MVP)**: Setup + Foundational + User Story 1 → Resolves misleading dialog text and Turkish fallback in English mode.
2. **Increment 2**: User Story 2 → Resolves Dashboard payment calculation math bug and introduces multi-rate tier breakdown.
3. **Increment 3**: User Story 3 → Adds "Save Changes" capability to upcoming planned lessons in Calendar.
4. **Increment 4**: Phase 6 Polish → Full regression testing, localization parity check, and E2E validation.

### Parallel Team Strategy

With multiple developers:
1. Team completes Phase 1 (Setup) and Phase 2 (Foundational) together.
2. Once Phase 2 completes:
   - Developer A: User Story 1 (`T007` - `T011`)
   - Developer B: User Story 2 (`T012` - `T015`)
   - Developer C: User Story 3 (`T016` - `T019`)
3. Stories integrate cleanly without file conflicts.
4. Team runs Phase 6 (Polish & verification) together.

---

## Notes

- `[P]` tasks = different files, no dependencies.
- `[Story]` label maps task to specific user story for traceability.
- Each user story is independently completable and testable.
- Verify unit tests fail before implementing each feature.
- Commit after each task or logical group using Conventional Commits.
- Stop at any checkpoint to validate story independently.
