# Quickstart & Verification Guide: lesson-times-homework-state

## Overview

This guide outlines end-to-end verification steps for:
1. Lesson start time visibility across UI screens.
2. Dynamic homework overdue computation and removal of manual "Overdue" selection.

---

## Prerequisites

- Local build environment with Android SDK API 36 / Java 17.
- Working project workspace (`halitbarut/Tullab`).

---

## Scenario 1: Lesson Start Time Visibility

1. **Calendar Daily Details**:
   - Navigate to the Calendar tab.
   - Select a date containing a scheduled or completed lesson.
   - Verify the lesson card displays an `Icons.Outlined.Schedule` icon, the localized start time (e.g. `14:30` or `2:30 PM`), and the duration span.
2. **Student Dashboard**:
   - Navigate to any student's Dashboard screen.
   - Inspect the **Upcoming Lessons** card: verify localized start times appear alongside dates.
   - Inspect the **Completed Lessons** card: verify localized start times appear alongside dates.
   - Inspect the **Log Past Lessons** card: verify localized start times appear alongside dates.
3. **Log Lesson Sheet**:
   - Tap an unlogged past lesson or edit a lesson from the dashboard or calendar.
   - Verify the bottom sheet header clearly shows the formatted localized start time alongside the date and schedule icon.

---

## Scenario 2: Dynamic Homework Overdue Behavior

1. **Create Homework with Past Due Date**:
   - Navigate to the Homework tab for a student.
   - Tap the "+" FAB to create a new assignment.
   - Verify the status dropdown **only** displays "Pending", "Completed", and "Cancelled" (no "Overdue").
   - Pick a due date in the past (e.g. yesterday) and save as "Pending".
2. **Verify List Screen & Filter Tabs**:
   - On the Homework list, verify the item has the red "Overdue" badge.
   - Select the "Pending" filter tab: verify the overdue item is displayed.
   - Select the "Completed" filter tab: verify the overdue item is hidden.
3. **Edit Homework & Change Status**:
   - Tap the overdue homework to open the edit sheet.
   - Verify the status dropdown contains only "Pending", "Completed", and "Cancelled".
   - Switch status to "Completed" and save.
   - Verify the red Overdue badge is replaced by the green "Completed" badge.
4. **Calendar Screen Daily Cards**:
   - Navigate to Calendar and select the date of the homework.
   - Verify pending homework with past due dates renders the red Overdue badge and styling.

---

## Automated Verification Commands

```bash
# Run unit tests across domain, data, and UI logic
./gradlew testDebugUnitTest
```
