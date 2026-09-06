# Quickstart & Verification Guide: Bulk Lesson Scheduling

**Feature**: `008-bulk-lesson-scheduling`  
**Date**: 2026-09-06  
**Status**: Ready for Verification  

This guide details automated and manual validation procedures to verify end-to-end functionality of the bulk lesson scheduling feature.

---

## 1. Automated Verification

Execute local JVM unit tests covering domain generation, conflict handling, and repository/DAO methods:

```bash
# Run unit tests for bulk lesson generation and use cases
./gradlew testDebugUnitTest --tests "com.barutdev.tullab.domain.usecase.lesson.*"

# Run ViewModel tests
./gradlew testDebugUnitTest --tests "com.barutdev.tullab.ui.screens.bulk_schedule.BulkScheduleViewModelTest"
```

### Key Automated Test Scenarios:
1. `BulkLessonGeneratorTest`:
   - Generates dates correctly for Calendar Grid mode.
   - Generates recurring weekday sequences up to `endDate`.
   - Generates fixed-window occurrences for `targetCount` and stops at count or 30.
   - Correctly identifies conflicts against a mock list of existing lesson timestamps.
   - Enforces limit $\le 30$ and flags limit violation if exceeded.
2. `CreateBulkLessonsUseCaseTest`:
   - Persists only non-conflicting candidate lessons.
   - Applies custom hourly rate if enabled, or inherits student profile rate.
   - Confirms all inserted lessons have `LessonStatus.SCHEDULED` and `durationInHours == null`.
3. `UndoBulkLessonsUseCaseTest`:
   - Invokes `deleteLessons(lessonIds)` and verifies records are removed.

---

## 2. Manual Verification Flow

### Scenario A: Monthly Calendar Grid Scheduling & Custom Day Time
1. **Prerequisite**: Open the app and select a student (e.g. "John Doe").
2. **Navigate**: On the Student Calendar screen, tap the TopBar action **"Bulk Add Lessons"**.
3. **Select Dates**: In "Calendar Grid" mode, tap 3 distinct dates (e.g., the 10th, 12th, and 14th of next month).
4. **Time & Price Preview**:
   - Verify default start time (e.g. 15:00) is shown.
   - Tap the day chip for the 12th and customize its time to 17:30.
   - Verify the dynamic preview above the confirm button reads: `3 lessons • $X.XX/hr`.
5. **Confirm**: Tap **"Schedule 3 Lessons"**.
6. **Expected Outcome**:
   - Screen pops back to the Student Calendar.
   - Lessons appear on the 10th (15:00), 12th (17:30), and 14th (15:00).
   - Bottom Snackbar appears: *"3 lessons scheduled"* with an **"Undo"** button.

### Scenario B: Weekly Routine with Target Count & Conflict Skipping
1. **Setup Conflict**: Ensure John Doe already has a lesson scheduled on next Tuesday at 16:00.
2. **Navigate**: Open **Bulk Add Lessons**.
3. **Switch Mode**: Select the **"Weekly Routine"** tab.
4. **Configure Routine**:
   - Select weekdays: **Tuesday** and **Thursday**.
   - Default time: **16:00**.
   - End condition: **Target number of lessons = 6**.
5. **Verify Conflict in Preview**:
   - Next Tuesday at 16:00 is detected as a conflict.
   - Dynamic preview displays: `5 lessons to create (1 skipped due to conflict) • $X.XX/hr`.
6. **Confirm**: Tap the confirmation button.
7. **Expected Outcome**:
   - 5 lessons are created; the conflicting Tuesday slot is skipped.
   - Snackbar reads: *"5 lessons scheduled, 1 skipped due to conflict"*.

### Scenario C: Single-Tap Undo Rollback
1. Immediately following Scenario B while the Snackbar is visible:
2. Tap the **"Undo"** action on the Snackbar.
3. **Expected Outcome**:
   - All 5 newly created lessons are instantly deleted.
   - The original pre-existing Tuesday lesson remains intact.
   - Schedule returns exactly to its pre-batch state.

### Scenario D: Past Date Warning Confirmation Dialog
1. In Bulk Add Lessons, pick a date from last week.
2. Tap **"Schedule Lessons"**.
3. **Expected Outcome**:
   - An alert dialog appears: *"Some lessons are scheduled in the past. They will be added as scheduled lessons so you can complete them later."*
   - Tapping **"Cancel"** aborts without writing to the database.
   - Tapping **"Confirm"** creates the lesson in `SCHEDULED` status.

### Scenario E: Maximum 30-Lesson Guardrail
1. Select 31 dates or a routine targeting 35 lessons.
2. **Expected Outcome**:
   - A red validation error appears: *"You can schedule a maximum of 30 lessons at once."*
   - The confirmation button is disabled until count $\le 30$.
