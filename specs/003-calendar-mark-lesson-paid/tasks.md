# Tasks: Mark Lessons as Paid in Calendar

**Input**: Design documents from `/specs/003-calendar-mark-lesson-paid/`  
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅, quickstart.md ✅

**Tests**: Unit tests are included per Constitution Principle V (Testing Expectations) covering `PaymentRepositoryImpl` transactions and `CalendarViewModel` state handling.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure & Localized Strings)

**Purpose**: String externalization in all supported languages (EN, TR, DE) that all user stories depend on

- [x] T001 [P] Add localized string resources (`calendar_lesson_action_mark_as_paid`, `calendar_lesson_action_revert_payment`, `calendar_dialog_revert_payment_title`, `calendar_dialog_revert_payment_message`, `calendar_dialog_revert_payment_confirm`, `calendar_dialog_mark_paid_fee_prompt`, `calendar_lesson_duration_locked_helper`) to `app/src/main/res/values/strings.xml`, `app/src/main/res/values-tr/strings.xml`, and `app/src/main/res/values-de/strings.xml`

---

## Phase 2: Foundational (Blocking Prerequisites - Data & Domain Layer)

**Purpose**: Core data access and repository operations that MUST be complete before user story UI implementation

**⚠️ CRITICAL**: No user story UI work can begin until this phase is complete

- [x] T002 [P] Add `deleteByStudentAndTimestamp(studentId: Int, paidAtEpochMs: Long): Int` and `getLatestPaymentRecord(studentId: Int): PaymentRecordEntity?` queries to `PaymentRecordDao` in `app/src/main/java/com/barutdev/kora/data/local/PaymentRecordDao.kt`
- [x] T003 [P] Add `markLessonAsPaid(lessonId: Int, durationInHours: Double? = null, customFee: Double? = null)` and `revertLessonPayment(lessonId: Int)` method signatures to `PaymentRepository` in `app/src/main/java/com/barutdev/kora/domain/repository/PaymentRepository.kt`
- [x] T004 Implement `markLessonAsPaid` in `app/src/main/java/com/barutdev/kora/data/repository/PaymentRepositoryImpl.kt` — execute inside `database.withTransaction`: fetch lesson, resolve duration and rate, insert `PaymentRecordEntity`, update `LessonEntity` status to `PAID` with `paymentTimestamp`, update `StudentEntity.lastPaymentDate`
- [x] T005 Implement `revertLessonPayment` in `app/src/main/java/com/barutdev/kora/data/repository/PaymentRepositoryImpl.kt` — execute inside `database.withTransaction`: delete matching `PaymentRecordEntity`, update `LessonEntity` status to `COMPLETED` with `paymentTimestamp = null`, recalculate `StudentEntity.lastPaymentDate`
- [x] T006 [P] Add unit tests in `app/src/test/java/com/barutdev/kora/data/repository/PaymentRepositoryImplTest.kt` verifying `markLessonAsPaid` (cycle reduction, record creation) and `revertLessonPayment` (cycle restore, record deletion)

**Checkpoint**: Foundation ready — repository and DAO can atomically mark lessons as paid and revert payments with unit test coverage

---

## Phase 3: User Story 1 — Mark Lessons as Paid from Calendar (Priority: P1) 🎯 MVP

**Goal**: Allow tutors to mark completed and scheduled lessons as paid directly from the calendar day details card, deducting the amount from the active payment cycle.

**Independent Test**: Select a date with a completed or scheduled lesson, tap the green "Mark as Paid" button (enter duration if scheduled), and verify the lesson transitions to "Paid", a `PaymentRecord` is created, and the student's unpaid cycle balance decreases by `duration × rate`.

### Implementation for User Story 1

- [x] T007 [US1] Inject `PaymentRepository` into `CalendarViewModel` in `app/src/main/java/com/barutdev/kora/ui/screens/calendar/CalendarViewModel.kt`
- [x] T008 [US1] Add state and handlers (`onMarkLessonAsPaidClicked`, `onConfirmMarkLessonAsPaid`, `pendingLessonForPayment`) in `app/src/main/java/com/barutdev/kora/ui/screens/calendar/CalendarViewModel.kt` to mark completed lessons as paid immediately or prompt for duration/fee if unlogged or rate is 0, canceling notification alarms via `cancelNotificationAlarmsUseCase(lessonId)` when scheduled lessons are marked as paid
- [x] T009 [US1] Update `LogLessonDialog` in `app/src/main/java/com/barutdev/kora/ui/screens/dashboard/components/LogLessonDialog.kt` to support prompting for duration and optional fee when marking a scheduled lesson as paid
- [x] T010 [US1] Update `LessonDetailCard` in `app/src/main/java/com/barutdev/kora/ui/screens/calendar/CalendarScreen.kt` to render a green `Button` (`StatusGreen`, white text, `Icons.Default.Check` icon) positioned below the existing action button when `lesson.status != LessonStatus.PAID`
- [x] T011 [US1] Connect `onMarkAsPaidClick` from `LessonDetailCard` to `CalendarViewModel` and display duration/fee prompt dialog when required in `app/src/main/java/com/barutdev/kora/ui/screens/calendar/CalendarScreen.kt`

**Checkpoint**: User Story 1 fully functional — tutors can mark any lesson as paid from Calendar and observe payment cycle reduction (MVP complete)

---

## Phase 4: User Story 2 — Visual Distinction and Status of Paid Lessons (Priority: P2)

**Goal**: Visually distinguish paid lessons from unpaid lessons in both the monthly calendar grid indicators and the lesson detail card, locking duration from direct edits.

**Independent Test**: View a calendar day with paid lessons → verify green dot indicator. View lesson detail card → verify "Paid" status badge, formatted payment timestamp, and that editing duration is disabled while notes remain editable.

### Implementation for User Story 2

- [x] T012 [US2] Update `CalendarStatusResolver.kt` in `app/src/main/java/com/barutdev/kora/ui/screens/calendar/CalendarStatusResolver.kt` to ensure days with paid lessons resolve green indicators properly when all completed/scheduled lessons on that date are paid
- [x] T013 [US2] Update `LessonDetailCard` in `app/src/main/java/com/barutdev/kora/ui/screens/calendar/CalendarScreen.kt` to display the formatted payment date and time when `lesson.paymentTimestamp != null`
- [x] T014 [US2] Update `LogLessonDialog` in `app/src/main/java/com/barutdev/kora/ui/screens/dashboard/components/LogLessonDialog.kt` to disable the duration input field (`enabled = false`) with an informative helper text when `lesson.status == LessonStatus.PAID`, while keeping notes editable

**Checkpoint**: Paid lessons have distinct visual indicators, display payment timestamp, and protect duration from accidental alteration

---

## Phase 5: User Story 3 — Reversing Accidental Payment Marks via Confirmation Pop-up (Priority: P3)

**Goal**: Provide a protected reversal flow that allows tutors to unmark a paid lesson, restoring its amount to the payment cycle and removing the payment record after confirmation.

**Independent Test**: Tap "Revert Payment" on a paid lesson card → verify confirmation pop-up appears. Dismissing keeps the lesson paid. Confirming reverts the lesson to "Completed", restores the cycle balance, and deletes the payment record from payment history.

### Implementation for User Story 3

- [x] T015 [US3] Add `lessonToRevert` state and handlers (`onRevertLessonPaymentClicked`, `onConfirmRevertPayment`, `onDismissRevertDialog`) in `app/src/main/java/com/barutdev/kora/ui/screens/calendar/CalendarViewModel.kt`
- [x] T016 [US3] Update `LessonDetailCard` in `app/src/main/java/com/barutdev/kora/ui/screens/calendar/CalendarScreen.kt` to display an `OutlinedButton` labelled "Revert Payment" positioned below the "Edit" button when `lesson.status == LessonStatus.PAID`
- [x] T017 [US3] Implement `RevertPaymentConfirmationDialog` in `app/src/main/java/com/barutdev/kora/ui/screens/calendar/CalendarScreen.kt` with title, warning message, confirm ("Revert"), and dismiss ("Cancel") buttons
- [x] T018 [US3] Connect revert dialog confirm and dismiss actions to `CalendarViewModel` in `app/src/main/java/com/barutdev/kora/ui/screens/calendar/CalendarScreen.kt`

**Checkpoint**: All user stories complete — tutors can mark as paid, see clear visual distinction, and safely revert payments with pop-up confirmation

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Automated test suite validation, lint checks, and manual verification

- [x] T019 [P] Create unit tests in `app/src/test/java/com/barutdev/kora/ui/screens/calendar/CalendarViewModelTest.kt` verifying `onMarkLessonAsPaidClicked`, `onConfirmRevertPayment`, and dialog state flows
- [x] T020 [P] Run `./gradlew testDebugUnitTest` to verify all repository, DAO, and ViewModel unit tests pass
- [x] T021 [P] Run `./gradlew lintDebug` to verify no lint regressions or missing translation keys
- [x] T022 Manual verification walkthrough of Scenarios 1–4 from `quickstart.md` on emulator

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **Foundational (Phase 2)**: Depends on Phase 1 strings — BLOCKS all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational (Phase 2) completion
- **User Story 2 (Phase 4)**: Depends on User Story 1 (Phase 3)
- **User Story 3 (Phase 5)**: Depends on User Story 1 & 2 (Phase 3 & 4)
- **Polish (Phase 6)**: Depends on completion of all user stories

### Parallel Opportunities

- T001 (all 3 strings.xml files) can be updated together
- T002 (DAO queries) and T003 (Repository interface) can run in parallel
- T006 (Repository tests) and T019 (ViewModel tests) can run in parallel once their respective classes are implemented
- T020 and T021 can run in parallel in CI or sequential terminal invocations

---

## Implementation Strategy

### MVP First (User Story 1 Only)
1. Complete Phase 1 (Strings)
2. Complete Phase 2 (DAO queries, Repository implementation, unit tests)
3. Complete Phase 3 (User Story 1: CalendarViewModel handlers, green "Mark as Paid" button, cycle reduction)
4. **VALIDATE MVP**: Verify completed lesson can be marked as paid and reduces unpaid cycle balance.

### Incremental Delivery
1. Add User Story 2 (visual distinction, duration locking on paid lessons)
2. Add User Story 3 (outlined "Revert Payment" button and confirmation pop-up)
3. Final Polish (complete test suite, lint verification, manual testing)
