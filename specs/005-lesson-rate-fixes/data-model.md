# Data Model: Lesson Rate Management, Multi-Rate Payment Breakdown, and Calendar Rate Editing

## Entities and Data Structures

### 1. PaymentBreakdownTier (New Read Model)

Represents an aggregated group of completed unpaid lessons sharing the identical pricing mode and unit rate within a single student payment cycle.

- **Package**: `com.barutdev.kora.ui.screens.dashboard`
- **Fields**:
  - `pricingMode: PricingMode` — Pricing mode (`PER_HOUR` or `FLAT_FEE`).
  - `rateOrFee: Double` — Applicable hourly rate or flat fee amount for this tier.
  - `totalHours: Double` — Accumulated duration in hours for this tier (0.0 for flat fee).
  - `lessonCount: Int` — Number of completed lessons included in this tier.
  - `subtotal: Double` — Total monetary value computed for this tier (`totalHours * rateOrFee` for hourly, `rateOrFee * lessonCount` for flat fee).
- **Invariants**:
  - `totalHours >= 0.0`
  - `subtotal >= 0.0`
  - For any payment cycle: `Σ tier.subtotal == totalAmountDue`.

---

### 2. ScheduledLessonsScope (New UI Model)

Classifies the temporal scope of uncompleted scheduled lessons when a tutor updates a student's profile rate.

- **Package**: `com.barutdev.kora.ui.screens.student_profile`
- **Values**:
  - `PAST_ONLY` — All uncompleted scheduled lessons have dates strictly before today (`lessonDate < today`).
  - `FUTURE_ONLY` — All uncompleted scheduled lessons have dates on or after today (`lessonDate >= today`).
  - `MIXED` — Uncompleted scheduled lessons exist both before and on/after today.
- **Usage**: Controls which dialog title, message, and confirmation buttons are rendered in `ScheduledLessonsRatePromptDialog`.

---

### 3. Existing Core Entities (Unchanged Schema)

#### Lesson
- **Fields**:
  - `id: Int` (Primary Key)
  - `studentId: Int` (Foreign Key → Student)
  - `date: Long` (Epoch milliseconds)
  - `status: LessonStatus` (`SCHEDULED`, `COMPLETED`, `PAID`, `CANCELLED`)
  - `durationInHours: Double?`
  - `notes: String?`
  - `pricingMode: PricingMode` (`PER_HOUR`, `FLAT_FEE`)
  - `rateOrFee: Double`
  - `paymentTimestamp: Long?`
- **Computed**:
  - `calculatedValue: Double`:
    - `PER_HOUR`: `(durationInHours ?: 0.0) * rateOrFee`
    - `FLAT_FEE`: `rateOrFee`

#### Student
- **Fields**:
  - `id: Int` (Primary Key)
  - `fullName: String`
  - `parentName: String?`
  - `parentContact: String?`
  - `notes: String?`
  - `customHourlyRate: Double?`
  - `hourlyRate: Double`

---

## State Transitions

### Scheduled Lesson Editing Lifecycle (Calendar)

```text
[Existing Lesson: SCHEDULED]
       │
       ├── Tutor edits rate/fee/notes and taps "Save Changes"
       │     └── Status: SCHEDULED (Updated rateOrFee, duration, notes persisted)
       │
       ├── Tutor taps "Mark as Not Done"
       │     └── Status: CANCELLED (Alarms cancelled, notes saved)
       │
       └── Tutor taps "Cancel" / Dismisses
             └── Status: SCHEDULED (No changes persisted)
```

### Rate Cascade Lifecycle (Edit Student Profile)

```text
[Profile Rate Changed]
       │
       ├── Student has 0 scheduled lessons
       │     └── Profile saved immediately (no dialog)
       │
       └── Student has > 0 scheduled lessons
             │
             ├── Dialog evaluated:
             │     ├── All dates < today  → Scope: PAST_ONLY
             │     ├── All dates >= today → Scope: FUTURE_ONLY
             │     └── Mixed dates        → Scope: MIXED
             │
             ├── Tutor selects "Update Lessons"
             │     └── Profile saved + existing scheduled lessons updated to new rate
             │
             └── Tutor selects "Keep Original Rates"
                   └── Profile saved + existing scheduled lessons retain snapshot rates
```
