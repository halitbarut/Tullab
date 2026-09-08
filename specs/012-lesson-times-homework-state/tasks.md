# Implementation Tasks: lesson-times-homework-state

**Feature Branch**: `012-lesson-times-homework-state` | **Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md)

## Phase 1: Setup (Shared Infrastructure & Helpers)

**Purpose**: Utility functions for localized time formatting

- [x] T001 [P] Implement `formatLessonStartTime` helper function in `app/src/main/java/com/barutdev/tullab/util/TimeFormatter.kt`

---

## Phase 2: Foundational (Domain & Data Layer)

**Purpose**: Core model updates, type converter fallback, and domain unit tests blocking user stories

**⚠️ CRITICAL**: Must complete before UI user story implementation

- [x] T002 [P] Remove `OVERDUE` from `HomeworkStatus` enum in `app/src/main/java/com/barutdev/tullab/domain/model/HomeworkStatus.kt`
- [x] T003 [P] Implement dynamic `Homework.isOverdue(today)` helper method in `app/src/main/java/com/barutdev/tullab/domain/model/Homework.kt`
- [x] T004 [P] Update `HomeworkStatusConverter` to safely map legacy `"OVERDUE"` strings to `HomeworkStatus.PENDING` on read in `app/src/main/java/com/barutdev/tullab/data/local/HomeworkStatusConverter.kt`
- [x] T005 [P] Add unit tests for `HomeworkStatusConverter` verifying legacy `"OVERDUE"` fallback in `app/src/test/java/com/barutdev/tullab/data/local/HomeworkStatusConverterTest.kt`
- [x] T006 [P] Add unit tests for `Homework.isOverdue` validating boundary conditions in `app/src/test/java/com/barutdev/tullab/domain/model/HomeworkTest.kt`

**Checkpoint**: Foundation ready - domain entities and converter support dynamic overdue handling.

---

## Phase 3: User Story 1 - Lesson Start Time Visibility (Priority: P1) 🎯 MVP

**Goal**: Surface localized lesson start times across Calendar daily details cards, Student Dashboard cards, and Log Lesson bottom sheet header.

**Independent Test**: Schedule or view lessons and verify formatted start times and `Icons.Outlined.Schedule` icons display properly in Calendar daily details, Student Dashboard (Upcoming, Completed, Log Past), and Log Lesson sheet header.

### Implementation for User Story 1

- [x] T007 [P] [US1] Surface localized start time with `Icons.Outlined.Schedule` icon and duration span in `LessonDetailCard` in `app/src/main/java/com/barutdev/tullab/ui/screens/calendar/CalendarScreen.kt`
- [x] T008 [P] [US1] Surface localized start time in `UpcomingLessonsCard`, `CompletedLessonsCard`, and `LogPastLessonsCard` in `app/src/main/java/com/barutdev/tullab/ui/screens/dashboard/DashboardScreen.kt`
- [x] T009 [P] [US1] Surface localized start time alongside date with `Icons.Outlined.Schedule` icon in `LogLessonDialog` header in `app/src/main/java/com/barutdev/tullab/ui/screens/dashboard/components/LogLessonDialog.kt`

**Checkpoint**: User Story 1 complete - all specified screens display localized lesson start times.

---

## Phase 4: User Story 2 - Dynamic Homework Overdue State (Priority: P1)

**Goal**: Treat "Overdue" strictly as a dynamically computed presentation state, remove manual selection from dialogs, and consistently render the red Overdue badge.

**Independent Test**: Create a pending homework assignment with a past due date. Verify that "Overdue" is not in the status dropdown, the list and filter tabs render the red Overdue badge under "Pending", the edit sheet displays the overdue badge, and calendar daily cards show the red badge.

### Implementation for User Story 2

- [x] T010 [P] [US2] Update `CalendarStatusResolver.kt` to remove `HomeworkStatus.OVERDUE` branch in `app/src/main/java/com/barutdev/tullab/ui/screens/calendar/CalendarStatusResolver.kt`
- [x] T011 [P] [US2] Update `CalendarViewModel.kt` `toggleHomeworkStatus` to remove `HomeworkStatus.OVERDUE` branch in `app/src/main/java/com/barutdev/tullab/ui/screens/calendar/CalendarViewModel.kt`
- [x] T012 [P] [US2] Update calendar status and toggle unit tests in `app/src/test/java/com/barutdev/tullab/ui/screens/calendar/CalendarStatusLogicTest.kt` and `app/src/test/java/com/barutdev/tullab/ui/screens/calendar/CalendarViewModelTest.kt`
- [x] T013 [US2] Update `CalendarScreen.kt` homework daily card to dynamically evaluate `isOverdue` and render the red Overdue badge in `app/src/main/java/com/barutdev/tullab/ui/screens/calendar/CalendarScreen.kt`
- [x] T014 [P] [US2] Update `HomeworkScreen.kt` list items, `StatusBadge`, and filter logic to dynamically evaluate and render red Overdue badge on past-due pending items in `app/src/main/java/com/barutdev/tullab/ui/screens/homework/HomeworkScreen.kt`
- [x] T015 [P] [US2] Update `HomeworkBottomSheet.kt` and `HomeworkDialog.kt` to remove "Overdue" from selectable status options and display overdue badge when editing past-due items in `app/src/main/java/com/barutdev/tullab/ui/screens/homework/components/HomeworkBottomSheet.kt` and `app/src/main/java/com/barutdev/tullab/ui/screens/homework/components/HomeworkDialog.kt`

**Checkpoint**: User Story 2 complete - Overdue status is strictly dynamic and non-selectable.

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Validation across the application and regression verification

- [x] T016 Execute automated unit tests via `./gradlew testDebugUnitTest` to verify zero regressions
- [x] T017 Execute manual validation steps per `specs/012-lesson-times-homework-state/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Can start immediately.
- **Foundational (Phase 2)**: Can start immediately or in parallel with Phase 1. Blocks Phase 3 & Phase 4.
- **User Story 1 (Phase 3)**: Depends on Phase 1 & Phase 2.
- **User Story 2 (Phase 4)**: Depends on Phase 2.
- **Polish (Phase 5)**: Depends on completion of Phase 3 & Phase 4.

### Parallel Opportunities

- T001 (TimeFormatter), T002 (HomeworkStatus), T003 (Homework), T004 (HomeworkStatusConverter) can all be executed in parallel.
- T005 and T006 unit tests can run in parallel with each other.
- T007, T008, and T009 in User Story 1 modify distinct screens and can be implemented in parallel.
- T010, T011, T012, T014, and T015 in User Story 2 modify distinct files and can be implemented in parallel.

---

## Implementation Strategy

### MVP Delivery (User Story 1)
1. Complete Phase 1 (TimeFormatter) and Phase 2 (Foundational models).
2. Complete Phase 3 (User Story 1 - Lesson Start Times).
3. Verify lesson start times in Calendar, Dashboard, and Log Lesson sheet.

### Full Delivery
4. Complete Phase 4 (User Story 2 - Dynamic Homework Overdue State).
5. Complete Phase 5 (Polish & Verification).
