# Feature Specification: Bulk Lesson Scheduling

**Feature Branch**: `008-bulk-lesson-scheduling`  
**Created**: 2026-09-06  
**Status**: Draft  
**Input**: User description: "The bulk lesson scheduling feature to be added to the application allows the tutor to schedule lessons exclusively for the selected student, independent of other students; lessons can be planned using two different methods, either by freely selecting days on a monthly calendar grid or by defining a weekly routine with selected weekdays that repeats until a specific end date or a target number of lessons. Lesson duration is not requested during planning, as it will be entered once the lesson is actually completed; a single default start time is assigned to all selected days, but the time for any individual day can be customized via a compact chip list below. The system defaults to the student's saved hourly rate, but checking an optional box reveals an input field to set a custom hourly rate for this specific batch. If the student already has a lesson scheduled on the exact same date and time, the system skips only that conflicting lesson while creating the remaining ones; a dynamic preview above the confirmation button displays the total number of lessons to be created and the applicable hourly rate; all added lessons are stored in the database as fully independent individual records while sharing a common batch ID (which is purged after a period if the user does not tap the undo button); upon completion, a bottom notification (Snackbar) indicates which lessons were skipped due to conflicts and how many were successfully added, featuring an "Undo" action that deletes the entire batch with a single tap to restore the calendar to its previous state. The tutor can schedule a maximum of 30 days of lessons in a single batch; if they attempt to schedule more, a red validation message is displayed stating something like "You can schedule a maximum of 30 lessons at once.""

## Clarifications

### Session 2026-09-06
- Q: In Weekly Routine mode, from which date should the recurring sequence begin generating lessons? (FR-003) → A: Configurable "Starting from" date picker defaulting to today (or the next matching weekday).
- Q: How long or under what trigger should the batch identifier be retained before being purged to finalize the lessons and expire the Undo action? (FR-016) → A: Active during the completion Snackbar display duration. The inserted lesson IDs are held in memory during the Snackbar lifecycle to perform Undo, and cleared once dismissed or if the app is closed, without adding a persistent batch column to the database.
- Q: Where should tutors be able to launch the bulk lesson scheduling screen from within the application? (FR-001) → A: Exclusively from the Student Calendar screen, automatically inheriting the active student context (studentId) from the navigation hierarchy without requiring any student selection picker.
- Q: Should tutors be permitted to select or generate dates in the past during bulk lesson scheduling? (FR-002) → A: Yes, past dates are permitted, but if any selected or generated date falls in the past, a confirmation dialog must warn the tutor before creating the batch. All lessons are initialized in the standard SCHEDULED status without duration, allowing the tutor to later complete them individually from the calendar.
- Q: When using Weekly Routine mode with a target number of lessons (e.g., 10), how should detected conflicts affect the total generated lessons? (FR-003, FR-009) → A: Fixed window evaluation. The system evaluates the first N calendar occurrences; any conflicting slots are skipped, resulting in fewer than N created lessons. The dynamic preview and completion notification explicitly detail the skipped count.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Bulk Scheduling via Calendar Date Selection (Priority: P1)

As a private tutor, I want to select specific individual dates from a monthly calendar grid for a chosen student, assign a unified default start time with optional day-by-day adjustments, and generate all selected lessons in a single action, so that I can schedule ad-hoc or irregular lesson series without repetitive manual entry.

**Why this priority**: Highest priority (P1). Multi-date selection is a foundational planning mode that delivers immediate value for non-linear schedules (e.g., exam preparation weeks, intensive holiday study sessions).

**Independent Test**: Can be tested by selecting a student, picking 3 specific dates on the monthly calendar, setting a default start time of 16:00, confirming creation, and verifying that 3 separate lessons appear in the student's schedule at 16:00 on those dates.

**Acceptance Scenarios**:

1. **Given** the tutor is on the bulk lesson scheduling screen for a selected student in Calendar Grid mode, **When** the tutor taps multiple dates across the current or upcoming months, **Then** all tapped dates are highlighted as selected and added to the planning pool.
2. **Given** multiple dates are selected in Calendar Grid mode, **When** the tutor deselects an already chosen date by tapping it again, **Then** that date is removed from the planning pool and the preview updates immediately.
3. **Given** multiple dates are selected with a default start time assigned, **When** the tutor taps an individual day chip in the list below the calendar, **Then** the tutor can change the start time specifically for that day without altering the default time for other selected days.
4. **Given** one or more selected or generated dates are prior to the current date, **When** the tutor taps the confirmation button, **Then** a confirmation dialog appears warning that past lessons are being created before the batch is finalized.

---

### User Story 2 - Bulk Scheduling via Weekly Routine (Priority: P1)

As a private tutor, I want to define a recurring weekly routine by choosing weekdays, a configurable starting date, and specifying either a target end date or a target number of lessons, so that I can establish a student's regular ongoing weekly lesson program in seconds.

**Why this priority**: Highest priority (P1). Weekly repeating routines are the primary scheduling workflow for private tutors managing regular student commitments throughout school terms.

**Independent Test**: Can be tested by choosing Weekly Routine mode for a student, setting the starting date, picking Tuesdays and Thursdays, setting a target limit of 8 lessons, and confirming creation to verify exactly 8 lessons are generated on successive Tuesdays and Thursdays.

**Acceptance Scenarios**:

1. **Given** the tutor selects Weekly Routine mode, **When** the tutor toggles specific weekdays (e.g., Monday, Wednesday), sets a Start Date (defaulting to today or next matching weekday), and sets an End Date, **Then** the system calculates and lists every occurrence of those weekdays between the start date and the chosen end date.
2. **Given** the tutor selects Weekly Routine mode, **When** the tutor sets a Start Date, toggles the stop condition to "Target number of lessons", and enters a count (e.g., 10), **Then** the system evaluates the first 10 calendar occurrences matching the weekdays, skips any slots conflicting with the student's schedule, and updates the preview count to reflect the remaining non-conflicting lessons (with skipped count noted).
3. **Given** occurrences generated by a weekly routine, **When** the occurrences are reviewed in the compact chip list, **Then** all occurrences default to the chosen batch start time while allowing per-day time adjustments via chips.

---

### User Story 3 - Custom Batch Pricing Override (Priority: P2)

As a private tutor, I want the bulk scheduler to prefill with the student's saved hourly rate while providing an optional override for the batch, so that I can apply package discounts, semester promotions, or special rates to a specific series of lessons without altering the student's standard profile rate.

**Why this priority**: Medium-high priority (P2). Tutors frequently sell multi-lesson packages at discounted or promotional rates. Rigid rates would force tutors to edit each lesson individually or distort future lesson defaults.

**Independent Test**: Can be tested by opening bulk scheduling for a student whose profile rate is $50/hr, checking the custom hourly rate box, inputting $40/hr, generating 4 lessons, and verifying that each created lesson is recorded at $40/hr while the student profile rate remains $50/hr.

**Acceptance Scenarios**:

1. **Given** the bulk scheduling screen opens for a student with a saved profile hourly rate, **Then** the hourly rate defaults to the student's saved rate and the custom rate input field remains hidden.
2. **Given** the default rate is displayed, **When** the tutor checks the optional custom rate box, **Then** an input field appears allowing entry of a custom hourly rate for this batch.
3. **Given** a custom rate is entered, **When** the tutor unchecks the custom rate box, **Then** the rate reverts to the student's saved profile hourly rate and the dynamic preview updates accordingly.

---

### User Story 4 - Conflict Detection, Feedback, and Single-Tap Undo (Priority: P2)

As a private tutor, I want the system to automatically detect and skip lessons that collide with existing lessons for the same student on the exact same date and start time, display a summary of created versus skipped lessons, and offer an immediate Undo action, so that I never duplicate a lesson and can immediately reverse an accidental batch creation.

**Why this priority**: Medium-high priority (P2). Automated conflict resolution prevents duplicate entries for the student, and single-tap undo provides immediate peace of mind and effortless recovery from human errors during bulk generation.

**Independent Test**: Can be tested by creating an existing lesson for Student A on Friday at 15:00, then scheduling a 3-lesson batch for Student A that includes Friday at 15:00. Verify 2 lessons are created, 1 is skipped, a notification reports "2 lessons scheduled, 1 skipped due to conflict", and tapping "Undo" deletes both created lessons.

**Acceptance Scenarios**:

1. **Given** a batch includes a date and start time that exactly matches an existing lesson for the selected student, **When** the tutor confirms the batch, **Then** only the conflicting lesson is skipped, all non-conflicting lessons are successfully saved, and existing lessons remain untouched.
2. **Given** a batch is successfully processed with or without skipped lessons, **When** creation completes, **Then** a bottom notification displays the number of successfully created lessons, indicates which lessons were skipped due to conflicts, and presents an "Undo" action.
3. **Given** the bottom completion notification is visible, **When** the tutor taps "Undo", **Then** all lessons created in that batch are deleted in a single tap, returning the schedule to its state before the batch was confirmed.
4. **Given** the notification period expires or is dismissed without tapping "Undo", **Then** the lessons remain as permanent independent records and the batch identifier is purged or finalized.

---

### User Story 5 - Batch Cap Enforcement and Validation (Priority: P3)

As a private tutor, I want the system to enforce a maximum limit of 30 lessons in a single batch with clear visual validation, so that I am prevented from unintentionally overpopulating the calendar with excessive entries.

**Why this priority**: Lower priority (P3). Guardrail to prevent accidental infinite generation, interface lockup, or database bloat.

**Independent Test**: Can be tested by selecting 31 dates in the calendar grid or defining a routine targeting 35 lessons, verifying that a red validation message appears, and verifying that the confirmation button is disabled.

**Acceptance Scenarios**:

1. **Given** the tutor selects up to 30 dates or configures a routine yielding up to 30 lessons, **Then** the dynamic preview shows the valid total count and the confirmation button is active.
2. **Given** the tutor selects 31 or more dates or configures a routine yielding more than 30 lessons, **Then** a prominent red validation message states "You can schedule a maximum of 30 lessons at once" and the confirmation button is disabled.
3. **Given** the red validation message is visible, **When** the tutor reduces the count to 30 or fewer, **Then** the validation message disappears and the confirmation button is re-enabled.

---

### Edge Cases

- **Zero Available Lessons**: If all dates selected by the tutor conflict with existing lessons for the selected student (yielding 0 new lessons), the system informs the tutor that all selected slots conflict and does not create an empty batch.
- **Cross-Student Independence**: Scheduling is strictly scoped to the selected student. A lesson scheduled for Student B at the same date and time does not block or skip a lesson scheduled for Student A.
- **Duration Omission**: In accordance with application domain principles, lesson duration is strictly not requested or populated during bulk scheduling; all generated lessons are created in scheduled status with duration unassigned, to be entered when each lesson is completed.
- **Non-Standard Time per Day**: If a tutor assigns custom times to 5 different days using the chip list, each custom time is preserved alongside the date when verifying conflicts and creating the final lesson records.
- **Past Date Scheduling with Warning Dialog**: Tutors are permitted to select or generate dates in the past (e.g. to backfill lessons). If any date in the batch is prior to today, the system displays a confirmation dialog warning the tutor before persistence. Lessons are created in scheduled status without duration, ready to be completed individually from the calendar.
- **Target Count with Partial Conflicts**: When using Weekly Routine mode with a target count of N, the generator evaluates only the first N matching calendar occurrences. Any of those occurrences that conflict with an existing lesson are skipped, resulting in fewer than N created lessons. The system does not automatically generate additional dates in future weeks to offset skipped conflicts.
- **Undo Retention Lifecycle**: The rollback mechanism operates during the active completion Snackbar window by retaining the newly inserted lesson IDs in memory. Once the Snackbar dismisses, expires, or the screen/app closes, the in-memory IDs are cleared and the lessons remain permanent independent records without modifying the database schema.
- **Student Profile Rate Changes After Batch**: Each created lesson stores an independent pricing snapshot (using the student's rate or custom override). Subsequent changes to the student's profile rate do not alter the rates of the batch-created lessons.
- **Offline Persistence**: All conflict checks, batch generation, individual record creation, and rollback undo operations must execute completely offline in local storage.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Bulk scheduling MUST be accessible exclusively from the Student Calendar screen, automatically inheriting the active student context (`studentId`) from the navigation hierarchy without requiring an embedded student selection picker.
- **FR-002**: The system MUST support two distinct planning methods:
  - **Calendar Grid Mode**: Free multi-date selection across a monthly calendar grid.
  - **Weekly Routine Mode**: Selection of one or more recurring weekdays with a termination rule.
- **FR-003**: In Weekly Routine mode, the system MUST provide a configurable Start Date picker (defaulting to today or the next matching weekday) and MUST support two mutually exclusive termination rules:
  - Repeat until a specified end date.
  - Repeat until a target number of lessons is reached.
- **FR-004**: The system MUST assign a single default start time to all selected dates in the batch.
- **FR-005**: The system MUST provide a compact list of day chips below the primary selection interface allowing tutors to inspect and customize the start time for any individual selected day.
- **FR-006**: The system MUST NOT request or require lesson duration during bulk scheduling; all created lessons MUST be initialized in scheduled status with duration unassigned (`null`).
- **FR-007**: The system MUST default the hourly rate to the selected student's saved profile hourly rate.
- **FR-008**: The system MUST offer an optional checkbox that, when checked, reveals an input field to set a custom hourly rate applied to all lessons in this specific batch.
- **FR-009**: The system MUST detect whether the selected student already has a lesson scheduled on the exact same date and start time, and MUST skip only the conflicting lesson while scheduling all remaining non-conflicting lessons. In Weekly Routine mode with a target count of N lessons, the system MUST evaluate the first N occurrences and skip conflicting slots without generating extra compensatory occurrences beyond the initial N dates.
- **FR-010**: The system MUST enforce a strict upper limit of 30 lessons per batch.
- **FR-011**: If the tutor attempts to schedule more than 30 lessons, the system MUST display an inline red validation message stating "You can schedule a maximum of 30 lessons at once" (or localized equivalent) and MUST disable the confirmation action.
- **FR-012**: The system MUST display a dynamic preview directly above the confirmation button showing the total number of lessons to be created and the applicable hourly rate.
- **FR-013**: All created lessons MUST be stored in the database as fully independent individual records, without adding a persistent batch column to the database schema.
- **FR-014**: Upon successful creation of a batch, the system MUST display a bottom notification (Snackbar) detailing:
  - The number of lessons successfully created.
  - The lessons or count skipped due to exact date-time conflicts.
  - An "Undo" action.
- **FR-015**: Tapping the "Undo" action on the completion notification MUST delete all newly created lessons in that batch in a single atomic operation using the in-memory lesson IDs, restoring the schedule to its exact pre-batch state.
- **FR-016**: The in-memory list of created lesson IDs MUST be cleared once the completion notification (Snackbar) dismisses or expires, or if the tutor navigates away or closes the app, finalizing the lessons permanently.
- **FR-017**: All user-visible strings, date formats, and plurals MUST be externalized and localized in English, Turkish, and German, adhering to the project constitution.
- **FR-018**: The system MUST permit selecting or generating dates in the past. If one or more selected or generated dates fall prior to today, the system MUST display a confirmation warning dialog before creating the batch, alerting the tutor that past lessons will be added in scheduled status.

### Key Entities *(include if feature involves data)*

- **Bulk Schedule Request**: A transient domain/UI specification capturing the selected student, scheduling method (Calendar Grid vs. Weekly Routine), routine start date (if Weekly Routine), date set, default start time, per-day time overrides, pricing mode, and applicable hourly rate.
- **Lesson**: The autonomous scheduled entity representing an individual lesson session, containing student identifier, date and start time timestamp, status (`SCHEDULED`), duration (`null`), and pricing snapshot (`PER_HOUR` and rate).
- **Batch Undo Session (In-Memory)**: Ephemeral in-memory state tracking the list of newly created lesson IDs and student ID during the active completion Snackbar window to support atomic single-tap rollback.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Tutors can configure and schedule a multi-lesson batch (up to 30 lessons) in under 30 seconds.
- **SC-002**: 100% of conflicting lessons matching an existing lesson's exact date and start time for the student are skipped without blocking or corrupting the creation of the remaining lessons in the batch.
- **SC-003**: 100% of lessons created in a batch are cleanly removed when the tutor taps "Undo" in the completion notification, leaving zero orphan or phantom records.
- **SC-004**: Real-time validation feedback appears in under 100ms when the 30-lesson limit is exceeded, preventing confirmation until the selection is within the limit.
- **SC-005**: 100% of batch-created lessons can be individually edited, rescheduled, completed, or deleted in the calendar and student views without affecting other lessons created in the same batch.
- **SC-006**: The entire bulk scheduling workflow, conflict check, and undo rollback function 100% offline with zero external network dependencies.

## Assumptions

- **Student Calendar Navigation Scoping**: In the app's navigation architecture, the Calendar screen is already scoped strictly to an individual student (Home -> Select Student -> Calendar Tab -> Bulk Add Lessons). The bulk scheduling flow directly inherits this active `studentId` without redundant selection UI.
- **Student-Specific Conflicts**: Conflict detection evaluates only the selected student's schedule. Tutors may intentionally or unintentionally double-book their own time across different students; cross-student schedule locking is out of scope for this feature.
- **Exact Date-Time Collision**: A conflict is defined specifically as an existing lesson for the same student on the same calendar day with the exact same start time (e.g., both at 14:00 on October 12).
- **Time Selection Granularity**: Standard 12-hour or 24-hour time picker formats (matching device locale) are used for the default start time and per-day chips.
- **Undo Window Duration**: The "Undo" action is available for the standard duration of the bottom completion Snackbar (or until the tutor navigates away or performs another destructive action). Once the window closes, the batch is finalized and lessons can only be deleted individually.
- **Pricing Mode**: Bulk-scheduled lessons are created with `PER_HOUR` pricing mode using either the student's saved profile hourly rate or the custom batch hourly rate override. Flat-fee bulk packages can be edited on individual lessons after creation if needed.
- **Zero Profile Rate Default**: If the selected student has no saved hourly rate (e.g. 0.0 or unconfigured), the custom rate field is surfaced or prefilled with 0.0, prompting the tutor to verify pricing.
