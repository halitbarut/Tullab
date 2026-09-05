# Quickstart & Validation Guide: Mark Lessons as Paid in Calendar

**Branch**: `003-calendar-mark-lesson-paid`  
**Date**: 2026-09-04  
**Feature**: [spec.md](file:///home/halit/AndroidStudioProjects/Kora/specs/003-calendar-mark-lesson-paid/spec.md)

## Overview

This guide provides end-to-end verification workflows and testing commands to validate the single-lesson payment marking and reversal feature in Kora.

---

## Automated Verification

### 1. Run Unit Tests

Execute unit tests covering the `PaymentRepositoryImpl` single-lesson payment logic, transaction handling, reversal, and `CalendarViewModel` state flows:

```bash
./gradlew testDebugUnitTest
```

### 2. Run Database & Migration Verification

Verify Room DAO operations and queries:

```bash
./gradlew testDebugUnitTest --tests "com.barutdev.kora.data.repository.PaymentRepositoryImplTest"
```

### 3. Run Static Code Analysis & Lint

Ensure no syntax errors, lint regressions, or forbidden imports:

```bash
./gradlew lintDebug
```

---

## Manual User Journey Scenarios

### Scenario 1: Mark Completed Lesson as Paid
1. Launch Kora and select a student who has completed lessons awaiting payment.
2. Note the student's active payment cycle balance in the Dashboard (e.g., "$150.00").
3. Navigate to the **Calendar** tab.
4. Select a date containing a completed lesson (indicated by yellow dot).
5. In the day details card, locate the green **"Mark as Paid"** button (with check icon) below the "Edit" button.
6. Tap **"Mark as Paid"**.
7. **Expected Outcome**:
   - The lesson card updates immediately to show a green "Paid" status indicator.
   - The green button transforms into an outlined **"Revert Payment"** button.
   - The calendar day dot updates to green (paid).
   - Return to the Dashboard or Payment History: The unpaid cycle balance has decreased by the lesson amount, and a new payment record appears in Payment History.

---

### Scenario 2: Mark Scheduled Lesson as Paid (with Duration Input)
1. In the Calendar, select a date containing a **Scheduled** lesson.
2. Tap the green **"Mark as Paid"** button.
3. **Expected Outcome**:
   - A dialog prompts to enter lesson duration and optional notes.
   - Enter a valid duration (e.g., `1.5`) and confirm.
   - The lesson transitions directly to "Paid", records the payment history, and decreases the pending cycle balance.

---

### Scenario 3: Revert Payment via Confirmation Pop-up
1. On the Calendar screen, select a date containing a **Paid** lesson.
2. Tap the outlined **"Revert Payment"** button on the lesson card.
3. **Expected Outcome**:
   - A confirmation pop-up appears: "Revert Payment? Reverting will mark this lesson as unpaid and remove this payment from the student's payment history."
4. Tap **"Cancel"**:
   - Pop-up dismisses; lesson remains "Paid".
5. Tap **"Revert Payment"** again, then tap **"Revert"**:
   - The pop-up dismisses.
   - The lesson returns to "Completed" (yellow status).
   - The button transforms back to the green "Mark as Paid" button.
   - The lesson amount is restored to the student's unpaid payment cycle balance.
   - The corresponding payment record is deleted from Payment History.

---

### Scenario 4: Attempting to Edit Duration on Paid Lesson
1. Select a **Paid** lesson in the Calendar.
2. Tap the top **"Edit"** button.
3. **Expected Outcome**:
   - The edit dialog opens.
   - The **Duration** input field is disabled (read-only) with a helper note indicating payment must be reverted to change duration.
   - The **Notes** field remains editable.
   - Save updates the notes without modifying duration or payment record.
