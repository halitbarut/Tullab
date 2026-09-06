# Quickstart & Validation Guide: Lesson Rate Fixes

This guide describes end-to-end manual and automated validation scenarios to verify the bug fixes and enhancements for feature `005-lesson-rate-fixes`.

## Prerequisites

- Android SDK installed with `adb` available.
- Debug build compiled: `./gradlew assembleDebug`
- Unit tests runnable: `./gradlew testDebugUnitTest`

---

## Validation Scenario 1: Rate Update Dialog Timing & In-App Localization

### Goal
Verify that updating a student's profile rate on a date after an uncompleted scheduled lesson (e.g. September 4 vs September 1) displays a prompt referencing past/uncompleted lessons in the active in-app language (English, Turkish, or German).

### Steps
1. Launch Tullab and set app language to **English** (`Settings → Language → English`).
2. Create a Student (e.g., "Alice") with an hourly rate of 200 TL.
3. In Calendar, schedule a lesson for Alice on a past date (e.g., 3 days before today) or use test seed data.
4. Navigate to Alice's profile and tap **Edit**.
5. Change the hourly rate from 200 TL to 300 TL and tap **Save**.
6. **Expected Outcome**:
   - The confirmation prompt appears in **English** (not Turkish).
   - Dialog title and message accurately describe that the student has **past uncompleted lessons** (not "future scheduled lessons").
   - Action buttons clearly say "Update Past Lessons" and "Keep Original Rate".
7. Switch app language to **Turkish** (`Ayarlar → Dil → Türkçe`) and repeat with another student having past uncompleted lessons.
   - Dialog appears in proper Turkish ("Geçmiş Dersleri Güncelle").

---

## Validation Scenario 2: Multi-Rate Payment Cycle Breakdown on Dashboard

### Goal
Verify that when a payment cycle contains lessons completed under different rates, the Dashboard displays accurate, separate breakdown rows with subtotals that sum exactly to the total amount due.

### Steps
1. Create a Student (e.g., "Bob") with an hourly rate of 200 TL.
2. Complete a 5-hour lesson for Bob at 200 TL (Lesson value: 1,000 TL).
3. Update Bob's profile rate to 400 TL, selecting "Keep Original Rate" for existing lessons.
4. Complete a 2-hour lesson for Bob at 400 TL (Lesson value: 800 TL).
5. Open Bob's **Dashboard**.
6. **Expected Outcome**:
   - The payment tracking card headline displays **1,800 TL** (`1,000 TL + 800 TL`).
   - The breakdown area below displays separate rows:
     - Row 1: `5 hours × 200 TL` on left, `1,000 TL` on right.
     - Row 2: `2 hours × 400 TL` on left, `800 TL` on right.
   - Text does not truncate or clip on narrow screen resolutions or large system font scales.

---

## Validation Scenario 3: Calendar Rate Editing for Future Planned Lessons

### Goal
Verify that a tutor can edit the rate or pricing mode of an upcoming planned lesson directly from the Calendar screen and save the change while the lesson remains in `SCHEDULED` status.

### Steps
1. In the Calendar screen, locate or schedule an upcoming lesson (e.g., tomorrow at 10:00 AM) with a rate of 250 TL.
2. Tap on the lesson card to open its action dialog.
3. Observe the dialog interface:
   - The disabled "Complete Lesson" button is **removed**.
   - A primary **"Save Changes"** button is present.
   - "Mark as Not Done" (to cancel) and "Cancel" (to discard) are present.
4. Change the rate from 250 TL to 350 TL and tap **"Save Changes"**.
5. **Expected Outcome**:
   - The dialog closes.
   - The lesson card in the Calendar updates its displayed rate to 350 TL.
   - The lesson status remains **SCHEDULED** (yellow/blue indicator intact, not marked completed or paid).
   - Tapping "Mark as Not Done" cancels the lesson; tapping "Cancel" leaves it untouched.

---

## Automated Verification Suite

Run all unit tests verifying the domain logic, ViewModels, and state transformations:
```bash
./gradlew testDebugUnitTest --tests "com.barutdev.tullab.ui.screens.dashboard.*"
./gradlew testDebugUnitTest --tests "com.barutdev.tullab.ui.screens.student_profile.*"
./gradlew testDebugUnitTest --tests "com.barutdev.tullab.ui.screens.calendar.*"
```
