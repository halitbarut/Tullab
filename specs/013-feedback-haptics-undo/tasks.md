# Tasks: Comprehensive Haptic & Visual Feedback with Undo

**Branch**: `013-feedback-haptics-undo`
**Spec**: [spec.md](file:///home/halit/AndroidStudioProjects/Tullab/specs/013-feedback-haptics-undo/spec.md)
**Plan**: [plan.md](file:///home/halit/AndroidStudioProjects/Tullab/specs/013-feedback-haptics-undo/plan.md)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Establish tactile feedback primitives and string resources across all supported languages.

- [x] T001 [P] Define feedback and undo string resources in `app/src/main/res/values/strings.xml`
- [x] T002 [P] Define matching feedback and undo string resources in `app/src/main/res/values-tr/strings.xml`
- [x] T003 [P] Define matching feedback and undo string resources in `app/src/main/res/values-de/strings.xml`
- [x] T004 Create `TullabHapticFeedback` helper with `HapticFeedbackConstants` & `LocalHapticFeedback` mapping in `app/src/main/java/com/barutdev/tullab/ui/components/TullabHaptics.kt`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core visual notification and undo orchestration infrastructure in `TullabScaffoldController`.

- [x] T005 Extend `TullabScaffoldController` with `showUndoSnackbar` and `showMessage` in `app/src/main/java/com/barutdev/tullab/ui/navigation/TullabScaffoldController.kt`
- [x] T006 Ensure bottom margin and safe-area positioning for `SnackbarHost` in `app/src/main/java/com/barutdev/tullab/navigation/TullabNavGraph.kt`

**Checkpoint**: Foundation ready — all user stories can now leverage `TullabHaptics` and `TullabScaffoldController.showUndoSnackbar`.

---

## Phase 3: User Story 1 - Payment Recording Tactile Feedback & Undoable Snackbar (Priority: P1) 🎯 MVP

**Goal**: Provide a confirmation tactile pulse when marking lessons as paid, and show a top-level Snackbar with recorded amount and actionable "Undo".

**Independent Test**: Mark any unpaid lesson as paid, feel confirmation haptic pulse, verify `"Payment of [Amount] recorded"` Snackbar appears with `"Undo"`, tap `"Undo"`, and verify payment reverts to unpaid.

- [x] T007 [P] [US1] Implement `revertLessonPayment` in `app/src/main/java/com/barutdev/tullab/ui/screens/calendar/CalendarViewModel.kt` to restore previous unpaid lesson state upon undo
- [x] T008 [US1] Integrate `TullabHapticFeedback.CONFIRMATION` and `scaffoldController.showUndoSnackbar` when marking lessons as paid in `app/src/main/java/com/barutdev/tullab/ui/screens/calendar/CalendarScreen.kt`

**Checkpoint**: User Story 1 fully functional and testable independently as MVP.

---

## Phase 4: User Story 2 - Homework Completion Feedback & Immediate Undo (Priority: P2)

**Goal**: Deliver a crisp click haptic when toggling homework to completed, accompanied by a visual notification with an "Undo" action.

**Independent Test**: Complete an assignment in Calendar or Homework screen, feel the click tactile feedback, observe `"Homework completed"` Snackbar with `"Undo"`, tap `"Undo"`, and verify status returns to pending.

- [x] T009 [P] [US2] Integrate `TullabHapticFeedback.CLICK` and `scaffoldController.showUndoSnackbar` on homework status toggle in `app/src/main/java/com/barutdev/tullab/ui/screens/calendar/CalendarScreen.kt`
- [X] T010 [US2] Support homework completion toggle undo in `app/src/main/java/com/barutdev/tullab/ui/screens/homework/HomeworkScreen.kt`

**Checkpoint**: User Stories 1 & 2 operate independently.

---

## Phase 5: User Story 3 - Destructive Deletion Warning Haptic & Undo Recovery (Priority: P3)

**Goal**: Trigger warning tactile feedback when confirming deletion of lessons or homework, followed by an "Undo" Snackbar that restores the deleted item from an in-memory snapshot.

**Independent Test**: Delete a lesson or homework item, feel warning haptic on confirm, observe `"Item deleted"` Snackbar with `"Undo"`, tap `"Undo"`, and verify item is re-inserted into the database.

- [x] T011 [P] [US3] Add `restoreLesson` method in `app/src/main/java/com/barutdev/tullab/ui/screens/calendar/CalendarViewModel.kt`
- [x] T012 [US3] Integrate `TullabHapticFeedback.WARNING` and deletion undo Snackbar for lessons in `app/src/main/java/com/barutdev/tullab/ui/screens/calendar/CalendarScreen.kt`
- [x] T013 [P] [US3] Add `restoreHomework` in `app/src/main/java/com/barutdev/tullab/ui/screens/homework/HomeworkViewModel.kt`
- [x] T014 [US3] Integrate `TullabHapticFeedback.WARNING` and deletion undo Snackbar in `app/src/main/java/com/barutdev/tullab/ui/screens/homework/components/HomeworkBottomSheet.kt` and `app/src/main/java/com/barutdev/tullab/ui/screens/homework/HomeworkScreen.kt`

**Checkpoint**: Deletion safety net active across both lessons and homework.

---

## Phase 6: User Story 4 - Distinct Tactile Feedback on Segmented Control / Filter Selection (Priority: P4)

**Goal**: Provide distinct tactile pulse on segmented button choices (status choice and pricing mode) without redundant feedback when re-tapping selected options.

**Independent Test**: In `LogLessonDialog`, tap between Scheduled, Completed, and Cancelled, and between Per Hour and Flat Fee; observe a tactile pulse on each selection change.

- [x] T015 [US4] Integrate `TullabHapticFeedback.SEGMENT_PULSE` on `SingleChoiceSegmentedButtonRow` changes in `app/src/main/java/com/barutdev/tullab/ui/screens/dashboard/components/LogLessonDialog.kt`

**Checkpoint**: Segmented buttons feel physically responsive.

---

## Phase 7: User Story 5 - Backup Data Export & Import Visual Feedback (Priority: P5)

**Goal**: Present explicit visual Snackbars for CSV backup export and import success/failure without obstructing navigation.

**Independent Test**: Export or import CSV backup in Settings and observe the resulting confirmation or failure Snackbar.

- [x] T016 [US5] Audit and route backup export/import success and error notifications through `scaffoldController.snackbarHostState` in `app/src/main/java/com/barutdev/tullab/ui/screens/settings/SettingsScreen.kt`

**Checkpoint**: Backup visual feedback validated.

---

## Phase 8: User Story 6 - Complete Trilingual Localization Parity (Priority: P6)

**Goal**: Ensure all feedback and action strings are localized with full grammatical parity across EN, TR, and DE.

**Independent Test**: Switch system/app language between EN, TR, and DE; verify that all Snackbar texts and button labels render accurately.

- [x] T017 [US6] Verify and validate full linguistic parity across `values/strings.xml`, `values-tr/strings.xml`, and `values-de/strings.xml` for all feedback strings

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: Validation, regression testing, and build verification.

- [X] T018 Run automated unit tests via `./gradlew testDebugUnitTest`
- [X] T019 Verify build compilation via `./gradlew assembleDebug`
- [X] T020 Validate all scenarios outlined in `quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies
- **Setup (Phase 1)**: No dependencies — can start immediately.
- **Foundational (Phase 2)**: Depends on Phase 1 (requires string keys & haptics utility). Blocks all user stories.
- **User Stories (Phases 3 to 8)**: Depend on Foundational (Phase 2) completion. Can be implemented incrementally in priority order (P1 → P2 → P3 → P4 → P5 → P6).
- **Polish (Phase 9)**: Depends on all user stories.

### Parallel Opportunities
- T001, T002, T003 (string files) can run in parallel.
- T007 (US1 ViewModel) and T008 (US1 Screen) can be developed once Phase 2 completes.
- T009 and T010 (US2) can run in parallel with US3 (T011, T013).
- T015 (US4) is independent of US1-US3.

---

## Implementation Strategy

### MVP First (User Story 1 Only)
1. Complete Phase 1 (Setup) and Phase 2 (Foundational).
2. Complete Phase 3 (User Story 1: Payment Tactile Feedback + Undo).
3. Validate User Story 1 independently with `./gradlew testDebugUnitTest`.

### Incremental Delivery
1. Add Phase 4 (US2: Homework completion click + Undo).
2. Add Phase 5 (US3: Deletion warning haptic + Undo).
3. Add Phase 6 (US4: Segmented control pulse).
4. Add Phase 7 & 8 (US5: Backup feedback & US6: Trilingual audit).
5. Final verification (Phase 9).
