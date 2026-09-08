# Quickstart Validation Guide: Haptic & Visual Feedback with Undo

**Feature**: Comprehensive Haptic & Visual Feedback with Undo (`013-feedback-haptics-undo`)
**Date**: 2026-09-08

## 1. Prerequisites
- Android Studio or Gradle CLI (`./gradlew`)
- Device or Emulator running Android 8.0+ (API 26+)

## 2. Automated Test Commands

Run unit tests verifying ViewModel undo/restore operations and string resources:
```bash
./gradlew testDebugUnitTest
```

Verify build compilation:
```bash
./gradlew assembleDebug
```

## 3. Manual Verification Scenarios

### Scenario 1: Payment Tactile Feedback & Undo
1. Open Tullab and navigate to Calendar or Student details with an unpaid completed/scheduled lesson.
2. Tap "Mark as Paid" and confirm fee if prompted.
3. **Verify**:
   - A distinct confirmation vibration/pulse occurs.
   - The lesson status updates to "Paid".
   - A bottom Snackbar appears: `"Payment of [Amount] recorded"` with an `"Undo"` button.
4. Tap `"Undo"`.
5. **Verify**:
   - The lesson immediately reverts to its previous unpaid state.
   - The Snackbar dismisses.

### Scenario 2: Homework Completion Tactile Click & Undo
1. Navigate to Calendar or Homework view.
2. Tap the complete button on an incomplete homework assignment.
3. **Verify**:
   - A crisp click haptic is felt.
   - Homework status changes to "Completed".
   - Snackbar displays `"Homework completed"` with `"Undo"`.
4. Tap `"Undo"`.
5. **Verify**:
   - Homework status reverts to pending.

### Scenario 3: Destructive Deletion Warning Haptic & Undo
1. In Calendar or Homework sheet, select "Delete Lesson" or "Delete Homework".
2. In the confirmation dialog, tap "Delete".
3. **Verify**:
   - A subtle warning haptic is emitted upon confirming deletion.
   - The item disappears from the list.
   - Snackbar appears: `"Lesson deleted"` (or `"Homework deleted"`) with `"Undo"`.
4. Tap `"Undo"`.
5. **Verify**:
   - The deleted item is restored in the database and reappears on screen.

### Scenario 4: Segmented Button Feedback
1. In Log Lesson dialog, tap between status options (Scheduled, Completed, Cancelled) or pricing modes (Per Hour, Flat Fee).
2. **Verify**:
   - A distinct tactile pulse is felt when switching segments.
   - No redundant vibration when tapping an already active segment.

### Scenario 5: Localization Parity
1. Switch app language to Turkish in Settings.
2. Repeat Scenario 1 & 2. Verify Turkish labels: `"Geri Al"`, `"... tutarında ödeme kaydedildi"`, `"Ödev tamamlandı"`.
3. Switch app language to German in Settings.
4. Repeat Scenario 1 & 2. Verify German labels: `"Rückgängig"`, `"Zahlung von ... erfasst"`, `"Hausaufgabe abgeschlossen"`.
