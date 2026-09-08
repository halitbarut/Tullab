# Tasks: UI, UX, and Domain Fixes (011-ui-ux-fixes)

**Input**: Design documents from `specs/011-ui-ux-fixes/` (spec.md, plan.md, data-model.md, contracts/, research.md, quickstart.md)

---

## Phase 1: Setup (Strings & Infrastructure)

**Purpose**: Externalize required strings across all supported locales (`values`, `values-tr`, `values-de`)

- [X] T001 [P] Add deletion, calendar headers, empty-state, and date localization strings to English resources in `app/src/main/res/values/strings.xml`
- [X] T002 [P] Add deletion, calendar headers, empty-state, and date localization strings to Turkish resources in `app/src/main/res/values-tr/strings.xml`
- [X] T003 [P] Add deletion, calendar headers, empty-state, and date localization strings to German resources in `app/src/main/res/values-de/strings.xml`

---

## Phase 2: Foundational (Data Access Pipeline for Deletion)

**Purpose**: Core persistence methods that enable permanent entity removal

**⚠️ CRITICAL**: Must complete before User Story 1 can invoke repository deletion

- [X] T004 Add delete query/method to `HomeworkDao` in `app/src/main/java/com/barutdev/tullab/data/local/HomeworkDao.kt`
- [X] T005 Update `HomeworkRepository` interface in `app/src/main/java/com/barutdev/tullab/domain/repository/HomeworkRepository.kt` with `deleteHomework(homework: Homework)`
- [X] T006 Implement `deleteHomework(homework: Homework)` in `app/src/main/java/com/barutdev/tullab/data/repository/HomeworkRepositoryImpl.kt`

**Checkpoint**: Foundational data deletion pipeline ready.

---

## Phase 3: User Story 1 - Permanent Homework Deletion (Priority: P1) 🎯 MVP

**Goal**: Allow tutors to permanently delete a homework assignment from the edit bottom sheet with confirmation.

**Independent Test**: Open an existing homework assignment in the edit sheet, tap the delete trash can icon in the header, confirm deletion in the AlertDialog, verify sheet dismisses and the item is deleted from the database and screen.

### Implementation for User Story 1

- [X] T007 [US1] Add `onDeleteHomework(homework: Homework)` function to `HomeworkViewModel` in `app/src/main/java/com/barutdev/tullab/ui/screens/homework/HomeworkViewModel.kt`
- [X] T008 [US1] Update `HomeworkBottomSheet` in `app/src/main/java/com/barutdev/tullab/ui/screens/homework/components/HomeworkBottomSheet.kt` to add `onDelete: ((Homework) -> Unit)? = null` parameter, header delete icon button when editing, and confirmation AlertDialog
- [X] T009 [US1] Wire `onDeleteHomework` callback from `HomeworkViewModel` to `HomeworkBottomSheet` in `app/src/main/java/com/barutdev/tullab/ui/screens/homework/HomeworkScreen.kt`

**Checkpoint**: User Story 1 complete and independently testable.

---

## Phase 4: User Story 2 - Dynamic Calendar Agenda Header (Priority: P2)

**Goal**: Dynamically adapt daily agenda section header in calendar view to reflect the types of items scheduled for the selected day ("Schedule on [Date]", "Lessons on [Date]", or "Homework on [Date]").

**Independent Test**: In Calendar view, select a day with only lessons (verify "Lessons on [Date]"), a day with only homework (verify "Homework on [Date]"), and a day with both (verify "Schedule on [Date]").

### Implementation for User Story 2

- [X] T010 [US2] Update `CalendarDayDetails` in `app/src/main/java/com/barutdev/tullab/ui/screens/calendar/CalendarScreen.kt` to dynamically evaluate scheduled items and display `calendar_day_schedule_details_title`, `calendar_day_homework_details_title`, or `calendar_day_details_title`

**Checkpoint**: User Story 2 complete and independently testable.

---

## Phase 5: User Story 3 - Homework List Empty State Redesign (Priority: P3)

**Goal**: Upgrade the homework list empty state from a plain text label to a visually consistent, centered empty state matching the design pattern of the student list.

**Independent Test**: Open Homework tab with no assignments (or filtered to 0 items); verify vertically centered layout with `Icons.Outlined.Assignment` (120dp), title, and descriptive subtitle.

### Implementation for User Story 3

- [X] T011 [US3] Create `HomeworkEmptyState` composable matching `StudentListEmptyState` pattern and replace the plain text message in `HomeworkScreen.kt` in `app/src/main/java/com/barutdev/tullab/ui/screens/homework/HomeworkScreen.kt`

**Checkpoint**: User Story 3 complete and independently testable.

---

## Phase 6: User Story 4 - Settings Notification Rows Spacing & Alignment (Priority: P4)

**Goal**: Resolve text concatenation bug in Settings notification rows where labels and times merge without proper spacing.

**Independent Test**: Navigate to Settings -> Notifications, verify label ("Lesson reminder time") and formatted time ("9:00 AM") have clean horizontal spacing across all screen sizes and font scales up to 200%.

### Implementation for User Story 4

- [X] T012 [US4] Update `SettingNavigationRow` in `app/src/main/java/com/barutdev/tullab/ui/screens/settings/SettingsScreen.kt` with explicit padding and alignment between title and value texts

**Checkpoint**: User Story 4 complete and independently testable.

---

## Phase 7: User Story 5 - Localized Date Formatting in Homework Dialogs & Labels (Priority: P5)

**Goal**: Replace raw ISO date formatting (`YYYY-MM-DD`) in homework input fields and labels with localized date formatting.

**Independent Test**: Open homework bottom sheet in English, Turkish, and German; verify due date field displays localized medium date and hint text is localized without raw ISO patterns.

### Implementation for User Story 5

- [X] T013 [US5] Update `dueDateText` in `HomeworkBottomSheet.kt` in `app/src/main/java/com/barutdev/tullab/ui/screens/homework/components/HomeworkBottomSheet.kt` to format using `DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)` and update hint text

**Checkpoint**: User Story 5 complete and independently testable.

---

## Phase 8: Polish & Verification

**Purpose**: Project-wide verification, build checks, and test suite execution

- [X] T014 Run existing unit tests via `./gradlew test` to ensure zero regressions
- [X] T015 Run build check via `./gradlew assembleDebug` to verify compilation across all modules
- [X] T016 Validate all scenarios against `specs/011-ui-ux-fixes/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies
- **Phase 1 (Setup Strings)**: No dependencies.
- **Phase 2 (Foundational Data Pipeline)**: Depends on Phase 1; blocks US1.
- **Phase 3 (User Story 1 - Deletion)**: Depends on Phase 2.
- **Phase 4 (User Story 2 - Calendar Header)**: Depends on Phase 1 strings. Can run in parallel with US1.
- **Phase 5 (User Story 3 - Empty State)**: Depends on Phase 1 strings. Can run in parallel with US1, US2.
- **Phase 6 (User Story 4 - Settings Alignment)**: Independent UI fix. Can run in parallel with others.
- **Phase 7 (User Story 5 - Date Formatting)**: In `HomeworkBottomSheet.kt`. Best completed alongside or after US1.
- **Phase 8 (Polish & Verification)**: Runs after all implementation tasks.

---

## Parallel Opportunities
- T001, T002, T003 (String files in EN, TR, DE) can be edited in parallel.
- T010 (Calendar header), T011 (Homework empty state), and T012 (Settings alignment) touch completely separate files and can execute in parallel.
