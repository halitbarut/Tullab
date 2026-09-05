# Feature Specification: Lesson Rate Locking and Historical Payment Calculation

**Feature Branch**: `004-lesson-rate-locking`  
**Created**: 2026-09-04  
**Status**: Draft  
**Input**: User description: "There is a problem with the payment calculation logic. When a tutor defines the hourly rate, then has a lesson but is waiting for the payment, and later decides to change the hourly rate, the app calculates the unpaid lesson using the new rate. Bring a new logic that fits my app's need."

## Clarifications

### Session 2026-09-04
- Q: When a tutor updates a student's profile hourly rate, how should upcoming, uncompleted lessons already on the schedule (status `SCHEDULED`) be treated? → A: Prompt the tutor with a choice dialog asking whether to apply the new rate to existing scheduled lessons or keep their existing rates. Completed and paid lessons must never be affected.
- Q: Should tutors be able to override the hourly rate or fee for an individual lesson directly in the Add/Edit Lesson and Log Lesson dialogs? → A: Yes, allow per-lesson rate overrides prefilled with the student's active rate.
- Q: When customizing pricing on an individual lesson, should tutors specify a custom hourly rate or a fixed total session fee? → A: Flexible toggle: Tutors can choose between "Per Hour" (scales with duration) and "Flat Fee" (fixed amount regardless of duration) for that specific lesson.
- Q: How should "locked" pricing behave and how is lesson value stored? → A: Completed lessons keep their own pricing snapshot and are shielded from automatic student-profile rate changes, but tutors can manually edit the pricing when explicitly editing an unpaid completed lesson. Redundant stored values must be avoided by computing lesson value deterministically from pricing mode, duration, and rate/fee. Student profile rates act strictly as defaults for new lessons.

## Core Domain Rules

1. **Independent Pricing Snapshot**: Every lesson maintains its own pricing snapshot (pricing mode and rate/fee amount). Completed lessons must never be automatically affected by changes to the student's profile rate.
2. **Definition of "Locked"**: "Locked" means protected against automatic cascading updates when a student's profile rate changes. Tutors may still manually adjust the pricing mode or rate/fee of an unpaid completed lesson when explicitly editing that lesson.
3. **Scheduled Lessons on Profile Rate Change**: When a student's rate changes in their profile, the app prompts the tutor to choose whether to apply the new rate to existing future scheduled lessons. Completed and paid lessons are strictly immune.
4. **Dual Pricing Modes**:
   - **Per Hour**: Lesson value is deterministically calculated as `durationInHours × hourlyRate`.
   - **Flat Fee**: Lesson value is a fixed amount regardless of duration.
5. **No Redundant Value Persistence**: The monetary value of a lesson is not stored as a separate database field; it is computed deterministically on demand from `pricingMode`, `durationInHours`, and `rateOrFee`.
6. **Legacy Migration**: Lessons existing prior to this feature without a pricing snapshot are safely backfilled into Per-Hour mode using the student's effective hourly rate at migration time.
7. **Student Rate as Template Only**: The student's profile rate acts solely as a default value when creating new lessons. All payment, debt, and balance calculations must use each lesson's own pricing snapshot.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Preserving Historical Rates for Completed Unpaid Lessons (Priority: P1)

As a private tutor, I want lessons that have already taken place to retain their individual pricing snapshot, so that updating a student's profile rate never retroactively changes the amount owed for past completed lessons awaiting payment.

**Why this priority**: Highest priority (P1). This resolves the core financial integrity defect. Retroactively recalculating past completed lessons when a rate changes distorts accounting and creates billing disputes.

**Independent Test**: Can be tested by creating a student with a rate of $30/hr, completing a 1-hour lesson (value $30), updating the student's profile rate to $50/hr, and verifying that the completed unpaid lesson and the student's pending balance remain exactly $30.

**Acceptance Scenarios**:

1. **Given** a student has an existing profile hourly rate of $30 and a completed unpaid lesson of 2 hours ($60 calculated value), **When** the tutor changes the student's profile rate to $40, **Then** the past completed lesson retains its $30/hr snapshot and its calculated value remains $60.
2. **Given** a completed lesson has a locked pricing snapshot, **When** the tutor views the lesson details in the calendar or dashboard, **Then** the displayed fee and rate reflect the lesson's locked snapshot ($30/hr) rather than the student's updated profile rate ($40/hr).
3. **Given** a tutor marks that completed lesson as paid after the student's profile rate has increased, **Then** the payment record is created for the original $60 amount, and the student's balance decreases by $60.

---

### User Story 2 - Accurate Cumulative Debt and Payment Cycle Settlement (Priority: P2)

As a private tutor, I want the student list debt indicator and dashboard payment cycle settlements to sum the actual snapshot costs of all unpaid completed lessons, so that my total pending earnings and student balances are mathematically exact even across multiple rate transitions.

**Why this priority**: High priority (P2). Tutors rely on summary cards to see total outstanding debts at a glance. Calculating total debt as `total_hours × current_profile_rate` produces erroneous totals whenever rates or pricing modes vary across lessons.

**Independent Test**: Can be tested by completing Lesson A (1 hr at $30/hr), updating student rate to $40/hr, completing Lesson B (1 hr at $40/hr), and verifying the student list displays a total outstanding debt of $70 ($30 + $40), not $80 ($40 × 2) or $60 ($30 × 2).

**Acceptance Scenarios**:

1. **Given** a student has multiple completed unpaid lessons logged under different historical rates (e.g., Lesson 1 at $30 for 1 hr, Lesson 2 at $40 for 1 hr), **When** viewing the Student List or Dashboard, **Then** the total outstanding balance is displayed as the sum of each lesson's deterministically calculated snapshot value ($70).
2. **Given** a student with multiple completed unpaid lessons at different historical rates or pricing modes, **When** the tutor triggers "Mark Student as Paid" (full cycle settlement), **Then** the total payment record created equals the exact sum of each completed lesson's snapshot value, all completed lessons transition to paid, and the outstanding balance becomes $0.
3. **Given** one lesson from a multi-rate cycle is marked as paid individually from the calendar, **Then** only that specific lesson's calculated value is deducted from the cumulative debt.

---

### User Story 3 - Per-Lesson Rate Transparency, Dual Modes, and Manual Editing (Priority: P3)

As a private tutor, I want to clearly see each lesson's pricing mode (Per Hour or Flat Fee) and rate/fee, and have the freedom to manually edit the pricing of a specific completed lesson when necessary without impacting the student's profile rate.

**Why this priority**: Medium priority (P3). Gives tutors total control over special cases (flat fee for a trial lesson, promotional discounts, or correcting a mistakenly entered fee on a past lesson).

**Independent Test**: Can be tested by scheduling a lesson with Flat Fee mode ($25 flat) instead of the student's profile rate ($40/hr for 1 hr), verifying the calculated value is $25, and later editing the completed lesson to $30 flat, observing the debt recalculate to $30.

**Acceptance Scenarios**:

1. **Given** the tutor is creating, editing, or logging a lesson, **When** the pricing section is inspected, **Then** it defaults to "Per Hour" prefilled with the student's active profile rate (or global default if student rate is unset), with the option to switch to "Flat Fee" or customize the rate/fee amount.
2. **Given** a tutor sets a flat fee of $25 on an individual lesson, **When** that lesson is completed and viewed in the calendar or dashboard, **Then** its calculated value is $25 regardless of the lesson duration.
3. **Given** an existing unpaid completed lesson, **When** the tutor explicitly opens the lesson edit dialog, **Then** the tutor can view and manually adjust the pricing mode or rate/fee for that specific lesson, updating its calculated value without altering the student's profile rate.
4. **Given** a lesson is in "Paid" status, **When** the tutor views the edit dialog, **Then** the pricing and duration remain locked from editing unless payment is first reverted.

---

### Edge Cases

- **Rate Change Impact on Future Scheduled Lessons**: When a tutor updates a student's profile rate and there are existing future scheduled lessons, the system displays a confirmation dialog asking: *"Apply new rate to existing scheduled lessons?"* (Yes / No). If confirmed, future scheduled lessons are updated to the new rate; if declined, they retain their currently assigned rate. In all cases, completed and paid lessons remain completely unaffected.
- **Flat Fee vs. Duration Changes**: If a lesson is in Flat Fee mode, modifying its duration does not alter the total calculated value. If in Per Hour mode, modifying its duration recalculates value as `durationInHours × rateOrFee`.
- **Zero or Unconfigured Rate**: If neither the student nor the app has an hourly rate configured when a lesson is logged, the lesson rate defaults to 0.0, and the tutor is prompted to enter a valid rate/fee before marking as paid.
- **Legacy Lessons Migration**: Existing completed lessons in the database prior to this feature that do not have a stored pricing snapshot must be backfilled in Per Hour mode using the student's effective rate (or default rate) at migration time so they do not default to $0.
- **No Redundant Stored Value**: Total lesson value is never persisted as an independent column to prevent data desynchronization; it is deterministically computed from `pricingMode`, `durationInHours`, and `rateOrFee`.
- **Reverting Payment**: When a paid lesson is reverted to completed, it retains its original locked pricing mode and rate/fee rather than adopting the current student profile rate.
- **Offline Reliability**: All snapshot operations, lesson updates, payment history records, and debt calculations execute completely offline in local storage.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Every lesson record MUST store its own explicit pricing snapshot consisting of a pricing mode (`PER_HOUR` vs. `FLAT_FEE`) and the associated monetary amount (`rateOrFee`), captured at the time of creation or logging.
- **FR-002**: When a lesson is marked as `COMPLETED`, its pricing snapshot MUST be protected from automatic modification whenever the student's profile rate or the global default rate changes.
- **FR-003**: "Locked" status MUST mean protected against automatic profile-rate changes while still permitting tutors to manually modify the pricing mode or rate/fee when explicitly editing an unpaid completed lesson in the lesson edit dialog.
- **FR-004**: The monetary value of an individual lesson MUST NOT be stored as a redundant database field, but MUST be deterministically calculated on demand:
  - For `PER_HOUR`: `(durationInHours ?: 0.0) × rateOrFee`
  - For `FLAT_FEE`: `rateOrFee`
- **FR-005**: The student's profile rate MUST act strictly as the default template for newly scheduled lessons and MUST NOT be used in payment or debt calculations for existing lessons.
- **FR-006**: The cumulative outstanding balance (debt) for a student across the Student List, Dashboard, and Calendar MUST be calculated as the sum of the deterministically computed values of all unpaid completed lessons (`Σ calculatedLessonValue`).
- **FR-007**: Updating a student's profile rate in the Student Profile MUST NOT retroactively alter or recalculate any previously completed or paid lessons.
- **FR-008**: When a tutor updates a student's profile rate and the student has existing scheduled (future, uncompleted) lessons, the system MUST prompt the tutor with a choice dialog asking whether to apply the new rate to existing scheduled lessons. If confirmed, future scheduled lessons are updated to the new rate; if declined, they retain their previously assigned rates.
- **FR-009**: When performing a full student payment settlement ("Mark Student as Paid"), the recorded payment amount MUST equal the exact sum of each completed unpaid lesson's deterministically calculated snapshot value.
- **FR-010**: When marking an individual lesson as paid, the recorded payment amount MUST equal that specific lesson's deterministically calculated snapshot value.
- **FR-011**: Existing database records from earlier app versions MUST be safely migrated, assigning legacy lessons a `PER_HOUR` pricing mode with the student's effective hourly rate at the time of migration.
- **FR-012**: The Add Lesson, Edit Lesson, and Log Lesson dialogs MUST provide a pricing toggle between "Per Hour" (prefilled with the student's active rate by default) and "Flat Fee" (fixed monetary amount).
- **FR-013**: In lesson detail cards and dialogs across Calendar, Dashboard, and History, the system MUST display the lesson's pricing mode, rate/fee, and deterministically calculated total session amount.
- **FR-014**: All user-visible strings relating to pricing modes, confirmation dialogs, rate change prompts, and fees MUST be externalized to `strings.xml` in English, Turkish, and German.

### Key Entities *(include if feature involves data)*

- **Lesson**: Represents a tutoring session. Attributes include student reference, date/time, status (Scheduled, Completed, Cancelled, Paid), duration in hours, notes, payment timestamp, **pricing mode** (`PER_HOUR` or `FLAT_FEE`), and **pricing amount** (`rateOrFee`). Total monetary value is a computed property, not a stored column.
- **Student**: Represents a student profile. Attributes include name, contact info, notes, and the current profile rate (`customHourlyRate`), which serves strictly as the default rate for newly scheduled lessons.
- **Payment Record**: Represents an actual monetary payment transaction. Attributes include student reference, payment amount, and timestamp.
- **Payment Cycle**: The aggregate collection of unpaid completed lessons for a student, whose total balance is the sum of each lesson's deterministically calculated snapshot value (`Σ calculatedLessonValue`).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 0% retroactive rate contamination: After a student's profile rate is updated, 100% of previously completed and paid lessons retain their original pricing mode, rate, and calculated value.
- **SC-002**: Mathematical parity without data redundancy: The total outstanding debt shown on the Student List and Dashboard always matches `Σ calculatedLessonValue` with 0 rounding errors or synchronization drift.
- **SC-003**: Full cycle payment settlement records match the exact sum of individual snapshot lesson values across all multi-rate and flat-fee lesson cycles.
- **SC-004**: 100% of legacy lessons are migrated with valid historical `PER_HOUR` rate snapshots without data loss or app crashes.
- **SC-005**: All snapshot operations, balance calculations, and payment recordings execute instantly and offline with zero network dependence.
