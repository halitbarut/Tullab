# Research: Bulk Lesson Scheduling Architecture & Decisions

**Feature**: `008-bulk-lesson-scheduling`  
**Date**: 2026-09-06  
**Status**: Completed  

## 1. Storage & Rollback (Undo) Architecture

### Decision
Do not add a `batch_id` column to the Room `lessons` table or create a persistent batch metadata table. Instead, return the newly generated lesson IDs (`List<Int>`) upon insertion, hold them in memory during the completion Snackbar's lifecycle, and clear them once the notification is dismissed or the screen/app closes.

### Rationale
- **Schema Simplicity**: Avoids an unnecessary Room database version bump, schema migration (`Migration_10_11`), and schema JSON exports.
- **Zero Ghost Records**: If an undo window is transient (several seconds to a minute while the user views the result), in-memory retention is standard Android practice (e.g. Gmail message send/delete undo).
- **Domain Independence**: Lessons are immediately autonomous individual records from the moment they are inserted.
- **Instant Rollback**: Calling `lessonRepository.deleteLessons(insertedIds)` provides single-operation atomic deletion directly via Room `DELETE FROM lessons WHERE id IN (:lessonIds)`.

### Alternatives Considered
- **Add nullable `batchId: String?` to `LessonEntity`**: Rejected. Requires Room schema migration, database version bump, and background purge worker or cleanup sweeps to prevent stale batch IDs lingering in the database.
- **Separate `batches` Room Table**: Rejected. Excessive relational complexity for an ephemeral 5–10 second undo window.

---

## 2. Weekly Routine & Calendar Date Generation

### Decision
Implement a pure Kotlin domain calculator `BulkLessonGenerator` utilizing `java.time` (`LocalDate`, `LocalTime`, `ZonedDateTime`, `DayOfWeek`).

### Rationale
- **Testability**: Pure Kotlin domain logic with zero Android framework dependencies (`Clean Architecture` Principle II), allowing 100% unit test coverage via JVM unit tests in `src/test/`.
- **Fixed-Window Occurrence Evaluation**: When given a target count $N$, the algorithm generates the first $N$ chronological dates matching the selected `DayOfWeek` set starting on or after `startDate`.
- **Exact Date-Time Conflict Filter**: Queries the student's existing lesson timestamps (`date: Long` epoch millis). An occurrence is flagged as a conflict if `studentLessons.any { it.date == proposedEpochMillis }`.
- **Cap Enforcement**: If the total candidate occurrences exceed 30, the generator returns a validation error (`EXCEEDS_MAX_LIMIT_30`), enabling the UI to display the inline red error and disable confirmation.

### Alternatives Considered
- **Target Fulfillment Loop (advancing to future weeks until $N$ non-conflicting found)**: Rejected in clarification Q5. Fixed-window evaluation respects the tutor's expected timeline and makes skipped occurrences transparent.
- **Cross-Student Schedule Collision Checking**: Rejected in clarification / spec. The feature explicitly operates strictly on the selected student's schedule.

---

## 3. Navigation & Screen Scope

### Decision
Register `TullabDestination.BulkSchedule : TullabDestination.StudentScoped("bulk_schedule", ...)` taking `studentId: Int`. Access is provided from `CalendarScreen`'s TopBar via a dedicated action button ("Bulk Add Lessons").

### Rationale
- **Context Preservation**: In Tullab, the Calendar tab is already scoped to an active student (`calendar/{studentId}`). Navigating to `bulk_schedule/{studentId}` automatically carries the student context without requiring any student selection dropdown.
- **Lifecycle Integration**: Upon confirming the batch, the app navigates back to `CalendarScreen` and emits a transient UI event `ShowBulkUndoSnackbar(createdCount, skippedCount, createdLessonIds)` via `TullabScaffoldController.snackbarHostState`.

### Alternatives Considered
- **Modal BottomSheet on CalendarScreen**: Rejected. The scheduling interface contains a monthly calendar grid, weekday toggles, chips for up to 30 days, time picker dialogs, and price override inputs. A full-screen destination provides a far superior user experience and ample room for the 48dp touch targets required by Principle IV.

---

## 4. Past Dates & Confirmation Warning

### Decision
Allow tutors to pick or generate dates prior to the current system date (`today`), but if any proposed lesson timestamp is before the start of today, display an `AlertDialog` warning the tutor before persistence: *"Some lessons are scheduled for dates in the past. They will be created in Scheduled status so you can log their completion later."*

### Rationale
- Satisfies clarification Q4: tutors frequently backfill past lessons taught earlier in the month.
- Creates lessons in `LessonStatus.SCHEDULED` with `durationInHours = null`, consistent with the application's domain rule that duration is entered upon completion.

---

## 5. Localization & Plurals

### Decision
Externalize all strings into `values/strings.xml`, `values-tr/strings.xml`, and `values-de/strings.xml`. Use Android plurals (`<plurals>`) for created and skipped counts:
- `bulk_schedule_snackbar_success_with_skipped`: e.g. *"%1$d lessons scheduled, %2$d skipped due to conflicts"*
- `bulk_schedule_snackbar_success`: e.g. *"%1$d lessons scheduled"*
- `bulk_schedule_max_limit_error`: *"You can schedule a maximum of 30 lessons at once."*
