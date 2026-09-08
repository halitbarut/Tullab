# Quickstart Validation Guide: UI, UX, and Domain Fixes (011-ui-ux-fixes)

## Prerequisites
- Android Studio / Android SDK
- Gradle build tooling

## Verification Scenarios

### 1. Homework Deletion
1. Open Tullab app and navigate to a student's Homework tab.
2. Select an existing homework assignment to edit.
3. Verify that the header contains a trash can icon button.
4. Tap the trash can icon button -> Verify that a confirmation dialog appears.
5. Tap Cancel -> Verify dialog closes and homework remains.
6. Tap the trash can icon button again and confirm deletion -> Verify sheet closes and homework is permanently removed from the list and database.
7. Tap the FAB to Add Homework -> Verify that the trash can icon is NOT present in the header.

### 2. Calendar Dynamic Agenda Header
1. Navigate to Calendar.
2. Select a date containing only lessons -> Header displays "Lessons on [Date]" (or Turkish/German equivalent).
3. Select a date containing only homework -> Header displays "Homework on [Date]" (or Turkish/German equivalent).
4. Select a date containing both lessons and homework -> Header displays "Schedule on [Date]" (or Turkish/German equivalent).
5. Select a date with no events -> Header displays default "Lessons on [Date]" with "No events scheduled for this day."

### 3. Homework List Empty State
1. In Homework tab, view a student with no homework assignments (or filter by a category with 0 items).
2. Verify that a centered empty state is rendered with:
   - Assignment icon (`Icons.Outlined.Assignment`) at 120dp.
   - Distinct headline title.
   - Descriptive subtitle.
3. Test under different device orientations and font scaling up to 200%.

### 4. Settings Notification Rows Spacing & Alignment
1. Navigate to Settings.
2. Under "Notifications", inspect "Lesson reminder time" and "Log reminder time".
3. Verify that the title text and the time value (e.g., "9:00 AM") are cleanly separated without concatenation.
4. Increase system font size to maximum / 200% -> verify labels and times remain properly spaced and readable.

### 5. Localized Date Formatting in Homework Dialog
1. Open Homework sheet (Add or Edit).
2. Look at the "Due Date" field.
3. Verify the date string is formatted in the user's locale (e.g., "Sep 8, 2026" / "8 Eyl 2026") and NEVER as "2026-09-08".
4. Check the hint placeholder text.

### Automated Checks
Run unit tests:
```bash
./gradlew test
```
Run static analysis & build:
```bash
./gradlew assembleDebug
```
