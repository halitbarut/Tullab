# Tasks: Show Homeworks on Calendar

**Input**: Design documents from `/specs/001-calendar-homeworks/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, quickstart.md ✅

**Tests**: Unit tests are included (existing tests need updating, new ViewModel tests required per plan.md and Constitution Principle V: Testing Expectations).

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story. Since core implementation already exists (homework fetch, basic toggle, basic indicator), tasks focus on **updating** existing code to match the clarified spec.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Domain model and theme color changes that all user stories depend on

- [X] T001 [P] Add `CANCELLED` value to `HomeworkStatus` enum in `app/src/main/java/com/barutdev/kora/domain/model/HomeworkStatus.kt`
- [X] T002 [P] Add homework color constants (`HomeworkTeal`, `HomeworkTealContainer`, `HomeworkMagenta`, `HomeworkMagentaContainer`, `HomeworkGray`, `HomeworkGrayContainer`) to `app/src/main/java/com/barutdev/kora/ui/theme/Color.kt`
- [X] T003 [P] Add i18n strings for `CANCELLED` homework status, `Overdue` badge label, and any missing homework action strings in `app/src/main/res/values/strings.xml`, `app/src/main/res/values-tr/strings.xml`, and `app/src/main/res/values-de/strings.xml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core data structure and resolver refactor that MUST be complete before user story UI changes

**⚠️ CRITICAL**: No user story UI work can begin until this phase is complete

- [X] T004 Create `DayIndicators` data class in `app/src/main/java/com/barutdev/kora/ui/screens/calendar/CalendarStatusResolver.kt` with `lessonColor: Color?` and `homeworkColor: Color?` fields
- [X] T005 Refactor `resolveCombinedStatusColor()` to `resolveDayIndicators()` in `app/src/main/java/com/barutdev/kora/ui/screens/calendar/CalendarStatusResolver.kt` — separate lesson and homework color resolution, use homework-specific colors (`StatusOrange` for PENDING, `HomeworkTeal` for COMPLETED, `HomeworkMagenta` for OVERDUE, `HomeworkGray` for CANCELLED), apply render-time overdue detection for PENDING homework past due date, return `DayIndicators` instead of single `Color?`
- [X] T006 Update `toggleHomeworkStatus()` in `app/src/main/java/com/barutdev/kora/ui/screens/calendar/CalendarViewModel.kt` — implement directed state transitions: `PENDING→COMPLETED`, `COMPLETED→PENDING`, `OVERDUE→COMPLETED`, `CANCELLED→no-op`

**Checkpoint**: Foundation ready — resolver returns dual colors, ViewModel enforces transition rules

---

## Phase 3: User Story 1 — View Homework Due Dates on Calendar (Priority: P1) 🎯 MVP

**Goal**: Display homework indicators on calendar days using dual-dot system — left dot for lesson status, right dot for homework status, single centered dot when only one type exists.

**Independent Test**: Create homework with a specific due date → verify that an indicator dot appears on that date. Create lesson + homework on the same day → verify two side-by-side dots appear.

### Implementation for User Story 1

- [X] T007 [US1] Update `CalendarDayCell()` in `app/src/main/java/com/barutdev/kora/ui/screens/calendar/CalendarScreen.kt` — change `indicatorColor: Color?` parameter to accept `DayIndicators`, render two 5dp side-by-side dots with 2dp spacing when both `lessonColor` and `homeworkColor` are non-null, render single centered 6dp dot when only one is non-null, render nothing when both are null
- [X] T008 [US1] Update `MonthlyCalendarGrid()` call site in `app/src/main/java/com/barutdev/kora/ui/screens/calendar/CalendarScreen.kt` — replace `resolveCombinedStatusColor()` call with `resolveDayIndicators()` and pass `DayIndicators` to `CalendarDayCell`

**Checkpoint**: Calendar displays dual-dot indicators for days with lessons and/or homework

---

## Phase 4: User Story 2 — Access Homework Details from Calendar (Priority: P1)

**Goal**: Show homework items in the day details section when a day is selected, with homework title, description, and status visible.

**Independent Test**: Select a day with homework → verify that the homework card appears in the day details. Select a day with no homework → verify no homework card appears.

### Implementation for User Story 2

- [X] T009 [US2] Rename `LessonDetailsSection` to `DayDetailsSection` in `app/src/main/java/com/barutdev/kora/ui/screens/calendar/CalendarScreen.kt` — rename the composable function and all internal call sites
- [X] T010 [US2] Update `HomeworkDetailCard()` in `app/src/main/java/com/barutdev/kora/ui/screens/calendar/CalendarScreen.kt` — use homework-specific colors for status indicators: `StatusOrange`/`StatusOrangeContainer` for PENDING, `HomeworkTeal`/`HomeworkTealContainer` for COMPLETED, `HomeworkMagenta`/`HomeworkMagentaContainer` for OVERDUE, `HomeworkGray`/`HomeworkGrayContainer` for CANCELLED

**Checkpoint**: Day details section shows homework cards with correct homework-specific colors

---

## Phase 5: User Story 3 — Distinguish Homework Status (Priority: P2)

**Goal**: Enable visual distinction of all 4 homework statuses on the calendar and in cards, with actionable toggle button following directed transition rules.

**Independent Test**: Toggle homework between PENDING↔COMPLETED → verify dot and card color changes. View overdue homework → verify Magenta styling. View cancelled homework → verify Gray styling and no toggle button.

### Implementation for User Story 3

- [X] T011 [US3] Update homework toggle button in `HomeworkDetailCard()` in `app/src/main/java/com/barutdev/kora/ui/screens/calendar/CalendarScreen.kt` — show "Mark as Complete" for PENDING and OVERDUE homework, show "Mark as Pending" for COMPLETED homework, hide toggle button entirely for CANCELLED homework, update button label i18n keys as needed
- [X] T012 [US3] Add overdue visual treatment to `HomeworkDetailCard()` in `app/src/main/java/com/barutdev/kora/ui/screens/calendar/CalendarScreen.kt` — display an overdue badge or visual indicator (e.g., "Overdue" chip) when homework status is OVERDUE or when PENDING homework's due date is in the past
- [X] T013 [US3] Add cancelled visual treatment to `HomeworkDetailCard()` in `app/src/main/java/com/barutdev/kora/ui/screens/calendar/CalendarScreen.kt` — apply strikethrough or muted styling to title/description for CANCELLED homework

**Checkpoint**: All 4 homework statuses are visually distinguishable and actionable on the calendar

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Testing, validation, and code quality improvements

- [x] T014 [P] Update unit tests in `app/src/test/java/com/barutdev/kora/ui/screens/calendar/CalendarStatusLogicTest.kt` — rewrite tests to target new `resolveDayIndicators()` function: test `DayIndicators` return type, verify homework uses `StatusOrange`/`HomeworkTeal`/`HomeworkMagenta`/`HomeworkGray` colors, verify lessons use `StatusGreen`/`StatusYellow`/`StatusBlue`/`StatusRed` colors, test dual-dot scenarios (both types present), test single-dot scenarios (only lessons or only homework), test render-time overdue detection (PENDING + past date → Magenta), test CANCELLED homework → Gray
- [x] T015 [P] Create `CalendarViewModelTest.kt` in `app/src/test/java/com/barutdev/kora/ui/screens/calendar/CalendarViewModelTest.kt` — test `toggleHomeworkStatus()` directed transitions: PENDING→COMPLETED, COMPLETED→PENDING, OVERDUE→COMPLETED, CANCELLED→no-op. Test homework loading via `homeworkState` StateFlow.
- [x] T016 Run `./gradlew :app:testDebugUnitTest --tests "com.barutdev.kora.ui.screens.calendar.*"` to verify all unit tests pass
- [x] T017 Run `./gradlew assembleDebug` to verify build success
- [ ] T018 Manual verification — walk through all 8 scenarios from plan.md Verification Plan (MV-001 through MV-008) on emulator

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately. All 3 tasks are parallelizable.
- **Foundational (Phase 2)**: Depends on Phase 1 completion (needs `CANCELLED` enum and new colors). BLOCKS all user story work.
- **User Story 1 (Phase 3)**: Depends on Phase 2 (`DayIndicators` data class and `resolveDayIndicators` function).
- **User Story 2 (Phase 4)**: Depends on Phase 2 (new colors for homework cards). Can run in parallel with US1.
- **User Story 3 (Phase 5)**: Depends on Phase 2 (directed transitions in ViewModel). Depends on US2 (modifies `HomeworkDetailCard`).
- **Polish (Phase 6)**: Depends on all user stories being complete. T014 and T015 are parallelizable.

### User Story Dependencies

- **User Story 1 (P1)**: Phase 2 → T007 → T008 (sequential within story)
- **User Story 2 (P1)**: Phase 2 → T009, T010 (T009 and T010 are sequential — rename first, then update colors)
- **User Story 3 (P2)**: Phase 2 + US2 → T011, T012, T013 (T011–T013 all modify same card, sequential)

### Within Each User Story

```
                     ┌── US1: T007 → T008        (CalendarDayCell dual-dot)
Phase 1 → Phase 2 ──┤
                     └── US2: T009 → T010        (DayDetailsSection + card colors)
                               └── US3: T011 → T012 → T013  (toggle + overdue + cancelled)
                                         └── Phase 6: T014–T018 (tests + build + verify)
```

### Parallel Opportunities

- **Phase 1**: T001, T002, T003 — all different files, fully parallel
- **Phase 2**: T004 → T005 (sequential, same file), T006 (parallel with T004/T005, different file)
- **Phase 3 + Phase 4**: US1 and US2 can run in parallel (different composables in same file, but careful orchestration needed)
- **Phase 6**: T014 and T015 — different test files, fully parallel

---

## Parallel Example: Phase 1 Setup

```bash
# All 3 setup tasks can run in parallel (different files):
Task T001: "Add CANCELLED to HomeworkStatus.kt"
Task T002: "Add homework colors to Color.kt"
Task T003: "Add i18n strings to strings.xml (EN/TR/DE)"
```

## Parallel Example: Phase 6 Tests

```bash
# Both test tasks can run in parallel (different test files):
Task T014: "Update CalendarStatusLogicTest.kt for dual-dot API"
Task T015: "Create CalendarViewModelTest.kt for state transitions"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001–T003)
2. Complete Phase 2: Foundational (T004–T006)
3. Complete Phase 3: User Story 1 (T007–T008)
4. **STOP and VALIDATE**: Build and test — calendar shows dual-dot indicators
5. This is the minimum viable increment of the feature

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Dual-dot indicator on calendar (MVP!)
3. Add User Story 2 → Homework details with distinct colors in day view
4. Add User Story 3 → Full status differentiation with toggle actions
5. Polish → Tests pass, build succeeds, manual verification complete

### Suggested MVP Scope

**User Story 1 alone** (T001–T008) delivers the core value: homework indicators visible on the calendar grid with the new dual-dot system and homework-specific color palette. This can be tested and demoed independently.

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story is independently completable and testable after Phase 2
- All tasks modify existing code — this is a *refactor-to-match-spec* workflow, not greenfield
- The `CalendarScreen.kt` file is touched by multiple stories — serialize US2 before US3
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
