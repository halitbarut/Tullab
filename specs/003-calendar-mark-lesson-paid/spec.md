# Feature Specification: Mark Lessons as Paid in Calendar

**Feature Branch**: `003-calendar-mark-lesson-paid`  
**Created**: 2026-09-03  
**Status**: Draft  
**Input**: User description: "in calendar screen, we should be able to mark lessons as paid. This reduces the amount of that lesson from the payment cycle."

## Clarifications

### Session 2026-09-03
- Q: How should the "Mark as Paid" action be presented on the calendar screen's lesson detail card? → A: Placed below the existing action button as a green-colored button featuring a check/tick icon.
- Q: Can a tutor edit the duration or notes of a lesson that has already been marked as paid? → A: Duration is non-editable/locked for paid lessons (notes can still be edited). To change duration, the tutor must first revert the payment.
- Q: What should the button display and do when a lesson is already in "Paid" status? → A: Transforms into an outlined/secondary "Revert Payment" button, which opens the reversal confirmation pop-up upon click.
- Q: What should happen if a lesson is marked as paid, but the student's effective hourly rate is 0 or unconfigured? → A: Prompt the tutor to input the lesson fee or hourly rate so an accurate, valid payment record is created.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Mark Lessons as Paid from Calendar (Priority: P1)

As a private tutor, I want to mark an individual lesson (whether already completed or scheduled) as paid directly from the calendar screen day details, so that I can immediately record payments as they happen and have that lesson's amount deducted from the student's pending payment cycle balance.

**Why this priority**: High value. This is the core functionality requested. Tutors frequently collect payment per lesson directly after or during a tutoring session and need immediate, seamless balance adjustment without full cycle settlement.

**Independent Test**: Can be tested by selecting a date with a completed or scheduled lesson, marking it as paid (entering duration if not already set), and verifying that the lesson status changes to "Paid", a payment record is generated in payment history, and the student's unpaid cycle balance decreases by the lesson's monetary value.

**Acceptance Scenarios**:

1. **Given** a student has a completed lesson with a logged duration on a selected day, **When** the tutor views the lesson details in the calendar and taps "Mark as Paid", **Then** the lesson status changes to "Paid", the status indicator updates to green, an individual payment record is created in the student's payment history, and the lesson's monetary value is excluded from the student's pending payment cycle.
2. **Given** a student has a scheduled lesson with no duration logged yet, **When** the tutor taps "Mark as Paid", **Then** the system prompts the tutor to enter the lesson duration (and optional notes) before completing the payment calculation.
3. **Given** a student has an outstanding payment cycle balance consisting of multiple completed lessons, **When** one of those lessons is marked as paid from the calendar, **Then** the outstanding balance for that student immediately decreases by `duration × effective hourly rate` for that specific lesson.

---

### User Story 2 - Visual Distinction and Status of Paid Lessons (Priority: P2)

As a private tutor, I want to clearly distinguish paid lessons from unpaid completed and scheduled lessons on the calendar, so that I can verify payment status at a glance without opening extra screens.

**Why this priority**: Medium-High value. Provides visual feedback, transparency, and confidence that payments are recorded accurately.

**Independent Test**: Can be tested by inspecting the monthly calendar cell indicators and day details section for days containing paid lessons.

**Acceptance Scenarios**:

1. **Given** a day contains a paid lesson, **When** viewing the calendar grid, **Then** the day displays a paid status color indicator (green).
2. **Given** a lesson is marked as paid, **When** viewing the day details card, **Then** the card explicitly displays the "Paid" badge and the payment timestamp/date.

---

### User Story 3 - Reversing Accidental Payment Marks via Confirmation Pop-up (Priority: P3)

As a private tutor, I want to revert a lesson accidentally marked as paid by confirming through a pop-up dialog, so that my billing records and payment history remain completely accurate if I tap the action inadvertently.

**Why this priority**: Medium value. Prevents data corruption, accounting mismatches, and user frustration from accidental taps.

**Independent Test**: Can be tested by selecting a paid lesson, tapping the action to unmark or revert it, confirming via the pop-up dialog, and verifying that the lesson returns to completed (unpaid) state, its amount is restored to the payment cycle, and the associated payment record is removed.

**Acceptance Scenarios**:

1. **Given** a lesson is currently marked as "Paid", **When** viewing the lesson detail card in the calendar, **Then** the card displays an outlined/secondary "Revert Payment" button positioned below the "Edit" action button, and tapping it opens the confirmation pop-up dialog.
2. **Given** the confirmation pop-up is displayed, **When** the tutor confirms the reversal, **Then** the lesson status returns to "Completed", the monetary value is added back to the student's active payment cycle, and the associated payment history record is removed.
3. **Given** the confirmation pop-up is displayed, **When** the tutor cancels or dismisses the pop-up, **Then** the lesson remains in "Paid" status and no data is changed.

---

### Edge Cases

- **Scheduled Lessons Without Duration**: When marking a scheduled lesson as paid, the system must require a valid numeric duration (> 0) before proceeding.
- **Zero or Invalid Duration Input**: If the user enters an invalid or non-numeric duration, the input is validated and the payment action cannot proceed until a valid duration is provided.
- **Payment History Logging**: Marking an individual lesson as paid automatically inserts a new `PaymentRecord` with the calculated amount (`duration × hourly rate`), timestamp, and updates the student's `lastPaymentDate`.
- **Reversal and Payment Record Clean-up**: Reverting a paid lesson with confirmation permanently removes or voids the corresponding single-lesson payment record so that historical income totals remain synchronized with actual lesson statuses.
- **Modifying Duration on Paid Lessons**: Lesson duration is locked and cannot be edited while in "Paid" status. To modify duration, the tutor must first revert the payment via the confirmation pop-up dialog, adjust duration while in "Completed" status, and mark as paid again.
- **Zero or Unset Hourly Rate**: If neither the student nor the app has an hourly rate configured (effective rate is 0), the system MUST prompt the tutor to enter the session fee (or hourly rate) before creating the payment record when marking as paid.
- **Offline Reliability**: All payment state transitions, history record creation/deletion, and balance recalculations must occur immediately in local storage without requiring network connectivity.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST allow tutors to mark any lesson (completed or scheduled) as "Paid" via a dedicated green-colored button featuring a tick/check icon positioned below the existing action button on the lesson detail card in the Calendar screen.
- **FR-002**: For lessons that do not yet have a recorded duration (such as scheduled lessons), the system MUST require the tutor to input the lesson duration before completing the "Mark as Paid" action.
- **FR-003**: Marking a lesson as "Paid" MUST immediately exclude that lesson from the student's pending payment cycle calculation (hours awaiting payment and total amount due).
- **FR-004**: Marking a lesson as "Paid" MUST automatically create a new payment history record for the student with the calculated amount (`duration × effective hourly rate`), set the payment timestamp on the lesson, and update the student's last payment date.
- **FR-005**: The system MUST update the visual status and indicator of the marked lesson to reflect its "Paid" state (green status indicator and "Paid" label) across both the calendar grid and the day details view.
- **FR-006**: For lessons in "Paid" status, the system MUST transform the green payment button into an outlined/secondary "Revert Payment" button positioned below the "Edit" button, which opens the confirmation pop-up dialog upon click.
- **FR-007**: Before reverting a "Paid" lesson, the system MUST display a confirmation pop-up dialog explaining the reversal impact.
- **FR-008**: Upon confirmation of reversal in the pop-up dialog, the system MUST restore the lesson status to "Completed", add its monetary value back to the pending payment cycle, and remove the associated payment record from payment history.
- **FR-009**: All payment operations in the calendar MUST function completely offline and persist immediately to local storage.
- **FR-010**: All user-facing strings (labels, button texts, status badges, pop-up dialog titles, descriptions, and action buttons) MUST be externalized and localized in English, Turkish, and German.
- **FR-011**: The system MUST lock the duration field (making it read-only) for lessons in "Paid" status while permitting edits to notes; altering the duration requires first reverting the payment.
- **FR-012**: If the student's effective hourly rate is 0 or unconfigured when marking a lesson as paid, the system MUST prompt the tutor to provide the payment amount or hourly rate to ensure an accurate, valid payment record is created.

### Key Entities *(include if feature involves data)*

- **Lesson**: Represents a scheduled, completed, cancelled, or paid tutoring session. Key attributes include student reference, session date/time, status (Scheduled, Completed, Cancelled, Paid), duration in hours, notes, and payment timestamp.
- **Payment Cycle**: The aggregate collection of completed lessons awaiting payment for a student, along with the calculated total unpaid hours and total unpaid amount due (based on the student's effective hourly rate).
- **Payment Record**: An entry recording a financial transaction for a student, containing student reference, paid amount, and payment timestamp.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Tutors can mark a completed lesson as paid from the calendar in 2 taps or fewer.
- **SC-002**: For scheduled lessons without a duration, tutors can log duration and mark as paid in a single streamlined dialog flow.
- **SC-003**: The student's unpaid cycle balance immediately decreases by the exact value of the paid lesson with 0 latency or delay.
- **SC-004**: Reverting a paid lesson requires explicit confirmation through a pop-up dialog, eliminating accidental reversals.
- **SC-005**: 100% of payment status updates, record creations, reversals, and balance adjustments succeed and persist without an internet connection.
- **SC-006**: Zero discrepancy between the sum of unpaid completed lesson amounts and the total cycle amount displayed across the dashboard and student views.
