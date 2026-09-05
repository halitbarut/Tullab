# Phase 0 Research: Mark Lessons as Paid in Calendar

**Branch**: `003-calendar-mark-lesson-paid`  
**Date**: 2026-09-04  
**Feature**: [spec.md](file:///home/halit/AndroidStudioProjects/Kora/specs/003-calendar-mark-lesson-paid/spec.md)

## Overview

This research phase investigates and documents technical decisions required to allow tutors to mark lessons as paid directly from the Calendar screen, adjust the active unpaid cycle balance, log transactions to payment history, and revert payments under confirmation.

---

## Technical Decisions

### 1. Atomic Payment State Transitions in Repository

- **Decision**: Implement `markLessonAsPaid` and `revertLessonPayment` methods on `PaymentRepository` / `PaymentRepositoryImpl` executed inside Room transactions via `KoraDatabase.withTransaction`.
- **Rationale**:
  - Marking a lesson as paid involves updating three distinct database tables simultaneously:
    1. Updating the `LessonEntity` status to `LessonStatus.PAID` and setting `paymentTimestamp`.
    2. Inserting a new `PaymentRecordEntity` with the calculated amount in minor currency units (`amountMinor = (duration * rate * 100).roundToLong()`) and `paidAtEpochMs`.
    3. Updating `StudentEntity.lastPaymentDate` to the transaction timestamp.
  - Executing these mutations inside an atomic transaction ensures that a crash or disk failure cannot leave the database in an inconsistent state (such as a paid lesson with no payment record, or vice versa).
- **Alternatives Considered**:
  - *Non-transactional individual DAO calls*: Rejected because a failure between updating the lesson and inserting the payment record would cause financial accounting discrepancies.
  - *Handling mutations in ViewModel*: Rejected because it violates Clean Architecture (Principle II), leaking database coordination into the UI layer.

---

### 2. Payment Record Linking and Reversal Strategy

- **Decision**: Link single-lesson payment records using the `(studentId, paidAtEpochMs)` composite index where `paidAtEpochMs == lesson.paymentTimestamp`. When reverting, delete the record matching this tuple and recalculate the student's `lastPaymentDate` from the latest remaining `PaymentRecord`.
- **Rationale**:
  - The `payment_records` table already defines `Index(value = ["studentId", "paidAtEpochMs"])`.
  - When marking a lesson as paid, assigning `val now = System.currentTimeMillis()` as both `lesson.paymentTimestamp` and `record.paidAtEpochMs` creates a deterministic, indexed link between the lesson and its transaction.
  - This avoids bumping the Room database schema version to 10 or writing custom migration scripts, preserving data backward compatibility and rapid local testing.
- **Alternatives Considered**:
  - *Adding `lessonId: Int?` to `PaymentRecordEntity`*: Evaluated and rejected as unnecessary overhead; requires Room schema migration from v9 to v10, migration tests, and schema export modifications without providing user-facing benefits beyond the timestamp match.
  - *Retaining payment record as a voided record*: Rejected because the user specifically clarified that reverting should completely remove the payment record from the history log.

---

### 3. Duration & Hourly Rate Capture for Uncompleted Lessons

- **Decision**: When a tutor initiates "Mark as Paid" on a lesson that lacks a logged duration (e.g. `SCHEDULED`) or for a student whose effective rate is 0/unset:
  - If duration is missing, present a dialog prompting for lesson duration (reusing `LogLessonDialog` with a "Mark as Paid" completion mode, or a dedicated payment dialog).
  - If the effective hourly rate is 0.0, present an additional input field for the session fee / hourly rate.
  - Calculate `amount = duration * rate`.
- **Rationale**:
  - Directly satisfies clarified user requirement: "tutor can mark all lessons as paid but he must enter the duration to calculate the amount" and "prompt the tutor to input the lesson fee or hourly rate so an accurate, valid payment record is created".
  - Validates duration > 0 and amount > 0 before triggering payment persistence.
- **Alternatives Considered**:
  - *Defaulting duration to 1.0 hour*: Rejected because private lessons often vary (45m, 60m, 90m, 120m); guessing duration leads to inaccurate billing.
  - *Silently logging $0 payments*: Rejected because $0 payment records pollute financial logs and fail the domain invariant of `amountMinor > 0L`.

---

### 4. Duration Locking on Paid Lessons

- **Decision**: When editing a lesson that is already in `PAID` status, the duration text field in `LogLessonDialog` is disabled (`enabled = false`) and displays an informative helper label. The notes field remains editable. To alter the duration, the tutor must first revert the payment.
- **Rationale**:
  - Directly matches user decision in clarification: "editing the duration of a paid lesson cannot be editable, but tutor can do that by using revert payment button".
  - Prevents subtle desynchronization where a lesson's duration is updated from 1.0h to 2.0h while the associated `PaymentRecord` remains at the 1.0h price.
- **Alternatives Considered**:
  - *Disabling the entire edit dialog*: Rejected because tutors often need to update lesson notes or homework references even after payment is collected.

---

### 5. UI Layout & Accessibility on `LessonDetailCard`

- **Decision**:
  - On `LessonDetailCard` in `CalendarScreen.kt`:
    - Below the existing action button (`Button` for "Edit" / "Log Details"):
      - If `lesson.status != LessonStatus.PAID`: A full-width `Button` with container color `StatusGreen` (`#2E7D32`), on-container text color `Color.White`, and leading `Icons.Default.Check` icon. Text: "Mark as Paid".
      - If `lesson.status == LessonStatus.PAID`: A full-width `OutlinedButton`. Text: "Revert Payment".
    - Clicking "Revert Payment" opens an `AlertDialog` asking for confirmation:
      - Title: "Revert Payment?"
      - Message: "Reverting will mark this lesson as unpaid and remove this payment from the student's payment history."
      - Confirm Button: "Revert" (destructive tone)
      - Dismiss Button: "Cancel"
  - Touch target height: 48dp minimum per Constitution Principle IV.
  - Localization: Externalized to `strings.xml` for `values/`, `values-tr/`, and `values-de/`.
- **Rationale**:
  - Directly follows user's exact specification: "below, green colored and has a tick icon" and "transforms into an outlined 'Revert Payment' button with a pop-up".
  - Satisfies WCAG AA contrast (> 4.5:1 for white text on `#2E7D32`).

---

## Summary of Architectural Impact

| Component | Layer | Nature of Change |
|---|---|---|
| `PaymentRepository` | Domain | Add `markLessonAsPaid(lessonId, duration, customFee)` and `revertLessonPayment(lessonId)` |
| `PaymentRecordDao` | Data | Add `deleteByStudentAndTimestamp(studentId, timestamp)` and `getLatestPaymentRecord(studentId)` |
| `PaymentRepositoryImpl` | Data | Implement transactional payment logging & reversal |
| `CalendarViewModel` | UI | Inject `PaymentRepository`; expose state & handlers for marking paid, prompting duration, and reverting |
| `CalendarScreen` | UI | Add green "Mark as Paid" button with tick icon, outlined "Revert Payment" button, and confirmation pop-up |
| `LogLessonDialog` | UI | Support read-only duration state when `lesson.status == PAID` |
| `strings.xml` (EN, TR, DE) | Resources | Add externalized localized strings for all actions, dialogs, and confirmations |
