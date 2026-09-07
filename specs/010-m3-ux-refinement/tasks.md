# Tasks: Material 3 Standards & Cross-Screen UX Refinement

**Branch**: `010-m3-ux-refinement` | **Feature**: [specs/010-m3-ux-refinement/spec.md](spec.md) | **Plan**: [specs/010-m3-ux-refinement/plan.md](plan.md)

## Phase 1: Setup & Localization Infrastructure

**Purpose**: Establish resource strings, plural definitions, and common styling tokens across all supported languages.

- [X] T001 [P] Define Turkish localization strings and plurals for relative hours ("%1$s saat"), notes, legends, speed dial actions, and filter chips in `app/src/main/res/values-tr/strings.xml`
- [X] T002 [P] Define English localization strings and plurals for relative hours ("%1$s hours"), notes, legends, speed dial actions, and filter chips in `app/src/main/res/values/strings.xml`
- [X] T003 [P] Define German localization strings and plurals for relative hours ("%1$s Stunden"), notes, legends, speed dial actions, and filter chips in `app/src/main/res/values-de/strings.xml`

---

## Phase 2: Foundational Components & Navigation Scoping

**Purpose**: Core UI helpers, top-bar scoping, and shared navigation behavior that all user stories depend on.

- [X] T004 Refactor top app bar default actions in `app/src/main/java/com/barutdev/tullab/navigation/TullabNavGraph.kt` so the Settings gear icon is NOT added by default to sub-screens
- [X] T005 [P] Create compact currency formatter utility `formatCompactCurrency` in `app/src/main/java/com/barutdev/tullab/util/CurrencyFormatter.kt`
- [X] T006 [P] Create `CalendarSpeedDialFab` composable with expansion micro-animations, labeled actions ("Seçili Güne Planla", "Toplu Ders Ekle"), and scrim in `app/src/main/java/com/barutdev/tullab/ui/components/CalendarSpeedDialFab.kt`
- [X] T007 [P] Create `CalendarLegend` composable displaying status dot color indicators with localized labels in `app/src/main/java/com/barutdev/tullab/ui/screens/calendar/components/CalendarLegend.kt`

**Checkpoint**: Foundation and shared components ready. User stories can proceed.

---

## Phase 3: User Story 1 - Unified Text Fields, Clean Top Bars & Localization (Priority: P1) 🎯 MVP

**Goal**: Standardize all text inputs to M3 `OutlinedTextField`, clean sub-screen top bars, and ensure full localization.

**Independent Test**:
- Open student creation form and homework creation sheet: all inputs are consistent `OutlinedTextField` with rounded corners.
- Check top app bars on sub-screens: no settings gear icon is visible.
- Switch locale across English, Turkish, and German: lesson durations, notes, and labels render accurately in all 3 languages without hardcoded strings.

### Implementation for User Story 1
- [X] T008 [US1] Audit and replace any legacy `TextField` (filled) with `OutlinedTextField` in student profile and dialog forms across `app/src/main/java/com/barutdev/tullab/ui/screens/`
- [X] T009 [US1] Remove redundant settings gear icon action from `app/src/main/java/com/barutdev/tullab/ui/screens/dashboard/DashboardScreen.kt` and `app/src/main/java/com/barutdev/tullab/ui/screens/settings/SettingsScreen.kt`
- [X] T010 [US1] Audit and consume all new strings/plurals across all 3 languages (English, Turkish, German) for duration formatting and note labels in `app/src/main/java/com/barutdev/tullab/util/DurationFormatter.kt`, `app/src/main/java/com/barutdev/tullab/ui/screens/calendar/CalendarScreen.kt`, and dashboard components

**Checkpoint**: At this point, input styling is unified, sub-screen top bars are clean, and all 3 languages are consumed consistently.

---

## Phase 4: User Story 2 - Modern Student List (Home) Screen Experience (Priority: P2)

**Goal**: Upgrade rectangular search to pill-shaped M3 SearchBar (56dp height, 28dp pill radius) and make entire cards tappable (removing pencil/chevron clutter).

**Independent Test**:
- Search bar is pill-shaped adhering to M3 specs (56dp height, 28dp radius, container fill).
- Student cards have no edit pencil or chevron icons; tapping anywhere navigates to dashboard.

### Implementation for User Story 2
- [X] T011 [US2] Redesign `StudentListSearchField` in `app/src/main/java/com/barutdev/tullab/ui/screens/student_list/StudentListScreen.kt` to adhere strictly to Material 3 search guidelines (pill shape with `RoundedCornerShape(28.dp)`, 56.dp standard height, and subtle container surface fill)
- [X] T012 [US2] Update `StudentListItem` in `app/src/main/java/com/barutdev/tullab/ui/screens/student_list/StudentListScreen.kt` to make the full card tappable and remove standalone edit pencil and chevron icons

**Checkpoint**: Student List screen feels modern, fluid, and uncluttered.

---

## Phase 5: User Story 3 - Polished Student Detail / Dashboard (Priority: P2)

**Goal**: Drop "Öğrenci:" prefix from header and elevate "Ödendi olarak işaretle" button contrast.

**Independent Test**:
- Open a student dashboard: header displays just the student name.
- Payment card: "Ödendi olarak işaretle" button has high contrast meeting WCAG AA standards.

### Implementation for User Story 3
- [X] T013 [US3] Update `topBarTitle` in `app/src/main/java/com/barutdev/tullab/ui/screens/dashboard/DashboardScreen.kt` to display the student name without the "Öğrenci:" prefix
- [X] T014 [US3] Upgrade the "Ödendi olarak işaretle" button styling in `PaymentTrackingCard` in `app/src/main/java/com/barutdev/tullab/ui/screens/dashboard/DashboardScreen.kt` to ensure high contrast against the card container

**Checkpoint**: Dashboard header is clean and payment button is immediately distinct and accessible.

---

## Phase 6: User Story 4 - Streamlined Calendar & Lesson Logging (Priority: P2)

**Goal**: Add dot legend, integrate Speed Dial FAB (remove full-width bulk button), and replace 3 action buttons in lesson dialog with M3 SegmentedButton.

**Independent Test**:
- Calendar grid shows dot legend.
- No in-feed "Toplu Ders Ekle" button; tapping FAB opens Speed Dial with "Seçili Güne Planla" and "Toplu Ders Ekle".
- `LogLessonDialog` displays an M3 SegmentedButton ("Planlandı", "Yapıldı", "Yapılmadı") with smart date defaults and a single "Kaydet" button.

### Implementation for User Story 4
- [X] T015 [US4] Add `CalendarLegend` beneath the monthly calendar grid in `app/src/main/java/com/barutdev/tullab/ui/screens/calendar/CalendarScreen.kt`
- [X] T016 [US4] Replace the primary FAB and in-feed `FilledTonalButton` with `CalendarSpeedDialFab` in `app/src/main/java/com/barutdev/tullab/ui/screens/calendar/CalendarScreen.kt`
- [X] T017 [US4] Refactor `LogLessonDialog` in `app/src/main/java/com/barutdev/tullab/ui/screens/dashboard/components/LogLessonDialog.kt` to use an M3 `SingleChoiceSegmentedButtonRow` for "Planlandı", "Yapıldı", "Yapılmadı" with smart defaults, a single primary Save button, and full IME window inset padding (`Modifier.imePadding()`) with scrollable layout to prevent keyboard from obscuring buttons and inputs

**Checkpoint**: Calendar screen scheduling flow is cohesive with clear visual hierarchy.

---

## Phase 7: User Story 5 - One-Handed Homework Management & Filtering (Priority: P3)

**Goal**: Add horizontal filter chips (Tümü, Bekleyenler, Tamamlananlar) and convert Add Homework dialog into an M3 `ModalBottomSheet`.

**Independent Test**:
- Filter chips filter homework list smoothly between All, Pending, and Completed.
- Tapping Add Homework slides up an M3 bottom sheet with outlined inputs and IME inset handling.

### Implementation for User Story 5
- [X] T018 [P] [US5] Implement `HomeworkFilterChips` composable in `app/src/main/java/com/barutdev/tullab/ui/screens/homework/components/HomeworkFilterChips.kt`
- [X] T019 [US5] Convert `HomeworkDialog` into `HomeworkBottomSheet` using `ModalBottomSheet` with `OutlinedTextField` inputs and IME padding in `app/src/main/java/com/barutdev/tullab/ui/screens/homework/components/HomeworkBottomSheet.kt`
- [X] T020 [US5] Integrate `HomeworkFilterChips` and `HomeworkBottomSheet` into `app/src/main/java/com/barutdev/tullab/ui/screens/homework/HomeworkScreen.kt`

**Checkpoint**: Homework management is optimized for one-handed thumb interaction.

---

## Phase 8: User Story 6 - Legible Reports Chart with Values & Tooltips (Priority: P3)

**Goal**: Display compact monetary values above chart bars and interactive tap tooltips with full unrounded currency.

**Independent Test**:
- Monthly earnings chart displays compact values (e.g. "₺12.5K") above each bar.
- Tapping a bar shows an interactive tooltip with the full amount and month.

### Implementation for User Story 6
- [X] T021 [US6] Refactor `BarChart` in `app/src/main/java/com/barutdev/tullab/ui/screens/reports/ReportsScreen.kt` to render compact formatted currency text above each bar
- [X] T022 [US6] Add tap gesture selection and detailed popup tooltip displaying full unrounded currency and period in `app/src/main/java/com/barutdev/tullab/ui/screens/reports/ReportsScreen.kt`

**Checkpoint**: Reports monthly chart is immediately legible with on-demand precision.

---

## Phase 9: Polish & Verification

**Purpose**: Verify end-to-end integration, run checks, and validate accessibility.

- [X] T023 [P] Verify dynamic font scaling up to 200% and dark theme rendering across all modified screens
- [X] T024 Run quickstart verification scenarios per `specs/010-m3-ux-refinement/quickstart.md`
- [X] T025 Execute project test and build verification (`./gradlew testDebugUnitTest assembleDebug`)

---

## Dependencies & Execution Order

### Phase Dependencies
- **Phase 1 (Setup)**: No dependencies.
- **Phase 2 (Foundational)**: Depends on Phase 1 strings.
- **Phase 3+ (User Stories)**: Depend on Phase 2.
  - US1 (P1): MVP milestone.
  - US2 (P2): Independent.
  - US3 (P2): Independent.
  - US4 (P2): Depends on `CalendarSpeedDialFab` and `CalendarLegend`.
  - US5 (P3): Independent.
  - US6 (P3): Depends on `formatCompactCurrency`.
- **Phase 9 (Polish)**: Depends on all user stories.

### Parallel Opportunities
- T001, T002, T003 can run in parallel (string files).
- T005, T006, T007 can run in parallel (distinct new component files).
- T011, T013, T015, T018, T021 can be developed in parallel across screen files once foundational components exist.
