# Phase 0 Research: Lesson Rate Locking and Historical Payment Calculation

**Feature**: `004-lesson-rate-locking`  
**Date**: 2026-09-04  
**Status**: Completed  

## Overview
This document consolidates architectural decisions, data models, and migration strategies for eliminating retroactive rate corruption in Kora's lesson payment calculations.

---

## Decisions and Rationales

### 1. Pricing Snapshot Representation on Lesson
- **Decision**: Add `pricingMode` (`PER_HOUR` vs. `FLAT_FEE`) and `rateOrFee` (`Double`) directly to `LessonEntity` and the domain model `Lesson`.
- **Rationale**: 
  - Every lesson must be an immutable financial record of the terms agreed upon when held.
  - Storing only `pricingMode` and `rateOrFee` allows deterministic calculation of `calculatedValue` without maintaining a third redundant column (`lessonValue`).
- **Domain Model Property**:
  ```kotlin
  val calculatedValue: Double
      get() = when (pricingMode) {
          PricingMode.PER_HOUR -> (durationInHours ?: 0.0) * rateOrFee
          PricingMode.FLAT_FEE -> rateOrFee
      }
  ```
- **Alternatives Considered**:
  - *Storing `totalAmount` in database*: Rejected per user requirement to avoid redundant data that can desynchronize if duration is edited.
  - *Historical rate table for students*: Over-engineered and does not support per-lesson discounts or flat fees.

---

### 2. Room Schema Migration (v9 → v10)
- **Decision**: Implement manual `MIGRATION_9_10` in `com.barutdev.kora.data.local.migrations` and increment `KoraDatabase.version` from 9 to 10.
- **SQL Execution**:
  ```sql
  ALTER TABLE lessons ADD COLUMN pricingMode TEXT NOT NULL DEFAULT 'PER_HOUR';
  ALTER TABLE lessons ADD COLUMN rateOrFee REAL NOT NULL DEFAULT 0.0;
  UPDATE lessons SET rateOrFee = COALESCE(
      (SELECT customHourlyRate FROM students WHERE students.id = lessons.studentId),
      (SELECT hourlyRate FROM students WHERE students.id = lessons.studentId),
      0.0
  );
  ```
- **Rationale**:
  - Backfills all legacy lessons with the student's active rate at migration time so historical records retain their expected value instead of defaulting to $0.
  - Accommodates auto-incrementing SQLite column alterations safely.
- **Testing**:
  - Add `Migration9to10Test.kt` in `androidTest` verifying schema evolution, column defaults, and legacy rate backfilling.

---

### 3. Future Scheduled Lessons Update Flow
- **Decision**: In `EditStudentProfileViewModel`, when the tutor saves a modified hourly rate, detect if the student has any `SCHEDULED` lessons. If so, display a confirmation dialog:
  - *"Apply new rate to existing scheduled lessons?"*
  - **Yes**: Update `rateOrFee` on `SCHEDULED` lessons that use `PER_HOUR` pricing mode.
  - **No**: Leave existing scheduled lessons untouched; only subsequent new lessons inherit the new rate.
  - In all cases, `COMPLETED` and `PAID` lessons are strictly excluded from updates.
- **Rationale**:
  - Respects tutor intent: a tutor may have already quoted a rate for upcoming scheduled sessions or may intend for a price hike to take effect immediately on all upcoming sessions.
  - Keeps completed historical sessions completely protected.

---

### 4. Centralized Debt and Payment Settlement Math
- **Decision**: Refactor all outstanding balance and settlement math across `StudentListViewModel`, `DashboardViewModel`, and `PaymentRepositoryImpl` to strictly sum `completedLessons.sumOf { it.calculatedValue }`.
- **Changes**:
  - `PaymentRepositoryImpl.markStudentAsPaid(studentId)`: Records payment of `completedLessons.sumOf { it.calculatedValue }`.
  - `PaymentRepositoryImpl.markLessonAsPaid(lessonId, ...)`: Records payment of `lesson.calculatedValue` (or custom override if passed).
  - `StudentListViewModel`: Groups completed lessons by student and sums `it.calculatedValue`.
  - `DashboardViewModel`: `totalAmountDue = computation.completedLessons.sumOf { it.calculatedValue }`.
- **Rationale**:
  - Completely decouples payment calculation from the student's current profile rate.
  - Guarantees 0% mathematical discrepancy across all screens.

---

### 5. UI Dialog Enhancements (Add, Edit, Log Lesson)
- **Decision**: Update `AddLessonDialog` and `LogLessonDialog` to include:
  - Pricing mode selector: "Per Hour" (default) or "Flat Fee".
  - Rate/Fee input field: Prefilled with student's profile rate (or global default if student rate is null).
  - Instant calculation preview: Shows calculated total based on entered duration and rate.
- **Rationale**:
  - Gives tutors full transparency and one-off flexibility without cluttering the main flow.
  - All labels and placeholders localized in EN, TR, and DE.
