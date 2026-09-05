# Quickstart & Verification Guide: Lesson Rate Locking

**Feature**: `004-lesson-rate-locking`  
**Date**: 2026-09-04  

## 1. Prerequisites
- Android Studio Ladybug / Meerkat or later
- JDK 17
- Android SDK Platform API 36
- Connected device or emulator running API 26+

---

## 2. Automated Test Execution

### A. Run Database Migration Tests
Verifies that schema version 9 correctly upgrades to 10 and that legacy lessons receive the student's active rate:
```bash
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.barutdev.kora.data.local.Migration9to10Test
```
**Expected Outcome**: Migration test passes; `pricingMode` defaults to `'PER_HOUR'`, and `rateOrFee` is populated from `students.customHourlyRate` or `students.hourlyRate`.

### B. Run ViewModel and Repository Unit Tests
Executes unit tests validating non-retroactive debt calculations and pricing modes:
```bash
./gradlew testDebugUnitTest --tests "com.barutdev.kora.data.repository.PaymentRepositoryImplTest"
./gradlew testDebugUnitTest --tests "com.barutdev.kora.ui.screens.student_list.StudentListViewModelTest"
./gradlew testDebugUnitTest --tests "com.barutdev.kora.ui.screens.dashboard.DashboardViewModelTest"
./gradlew testDebugUnitTest --tests "com.barutdev.kora.ui.screens.student_profile.EditStudentProfileViewModelTest"
```
**Expected Outcome**: All tests pass. `calculatedValue` evaluates accurately; cumulative debt equals `Σ lesson.calculatedValue`.

---

## 3. End-to-End Manual Verification Scenarios

### Scenario 1: Non-Retroactive Profile Rate Change (Core Fix)
1. Launch Kora and navigate to **Students** tab.
2. Add a new student "Alex" with an hourly rate of `$30`.
3. Add a completed lesson for Alex with duration `2.0` hours (Per-Hour mode at `$30/hr`).
4. Verify Dashboard displays:
   - Total hours: `2h`
   - Total amount due: `$60.00`
5. Open Alex's profile and change the hourly rate to `$50`. Save profile.
6. Return to Dashboard:
   - **Verification**: Completed lesson still shows `$30/hr` and the total amount due remains `$60.00` (NOT $100.00).

### Scenario 2: Multi-Rate Cycle and Settlement
1. Continuing from Scenario 1, add a second completed lesson for Alex with duration `1.0` hour (defaults to new rate `$50/hr`).
2. Verify Dashboard and Student List:
   - Total hours: `3h`
   - Total amount due: `$110.00` ($60 from Lesson 1 + $50 from Lesson 2).
3. Tap "Mark as Paid" (Full settlement):
   - **Verification**: Single payment record for `$110.00` created in payment history; outstanding balance resets to `$0.00`.

### Scenario 3: Flat Fee Lesson Pricing
1. Add a completed lesson for Alex, select **Flat Fee** mode, and enter `$25.00` with duration `2.0` hours.
2. **Verification**: The lesson card shows "Flat Fee: $25.00"; the total amount due increases by exactly `$25.00` (not $50.00).

### Scenario 4: Scheduled Lessons Prompt on Rate Change
1. Schedule a future lesson for Alex for tomorrow (status `SCHEDULED`).
2. Edit Alex's profile rate from `$50` to `$60`.
3. Tap Save:
   - **Verification**: A dialog appears asking *"Apply new rate to existing scheduled lessons?"*.
   - Tap "Yes": Verify tomorrow's scheduled lesson now shows `$60/hr`.
   - Verify previously completed/paid lessons remain completely unchanged.
