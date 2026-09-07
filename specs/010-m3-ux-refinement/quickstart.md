# Quickstart: Material 3 Standards & Cross-Screen UX Refinement

This guide outlines end-to-end validation procedures to verify all UX refinements across the application.

## Prerequisites
- Android device or emulator running API 26+
- Build system configured with Gradle JVM 17

## Test Scenarios

### Scenario 1: Unified Outlined Text Fields & Dialog to BottomSheet
1. Navigate to Homeworks screen (`Ödevler`).
2. Tap the floating `+` button.
3. **Verify**:
   - The form slides up from the bottom as an M3 `ModalBottomSheet`.
   - The title and description fields are `OutlinedTextField` components matching the New Student creation styling.
   - Text fields are fully visible and not blocked when the on-screen keyboard appears.

### Scenario 2: Student List Modernization
1. Navigate to the Home / Student List screen (`Öğrencilerim`).
2. **Verify**:
   - Search bar has pill-shaped rounded corners (`RoundedCornerShape(28.dp)`) with search icon and subtle background tint.
   - Student list cards do NOT have separate edit-pencil or chevron buttons.
   - Tapping anywhere on a student card opens the Student Detail / Dashboard screen.
   - TopAppBar shows the Reports icon and Settings gear icon (settings only on root).

### Scenario 3: Student Detail & Payment Contrast
1. Open a student's detail screen.
2. **Verify**:
   - Header displays the student's name directly (e.g., "Elif Yılmaz") without "Öğrenci:" prefix.
   - TopAppBar shows the Back arrow and Edit student profile pencil; no settings gear icon is shown.
   - "Ödendi olarak işaretle" button has high visual contrast against the card background.

### Scenario 4: Calendar Speed Dial, Legend & Log Lesson Status
1. Navigate to the Calendar screen (`Takvim`).
2. **Verify**:
   - A color legend is visible under the calendar grid or header explaining the meaning of the dots (green = paid, yellow = completed, blue = scheduled, red = cancelled/attention).
   - No separate full-width "Toplu Ders Ekle" button is present in the scrollable feed.
   - Tapping the FAB expands into two labeled buttons: "Seçili Güne Planla" and "Toplu Ders Ekle".
   - Tapping "Toplu Ders Ekle" opens the Bulk Add screen.
   - Tapping a lesson to edit opens `LogLessonDialog`:
     - An M3 SegmentedButton displays "Planlandı", "Yapıldı", "Yapılmadı".
     - Default selection is "Yapıldı" for past/today lessons and "Planlandı" for future lessons.
     - A single primary "Kaydet" button saves the lesson.

### Scenario 5: Homework Status Filtering
1. On the Homeworks screen, view the filter chips beneath the header: "Tümü", "Bekleyenler", "Tamamlananlar".
2. Tap "Bekleyenler" -> Only pending/overdue assignments are listed.
3. Tap "Tamamlananlar" -> Only completed assignments are listed.
4. Tap "Tümü" -> All assignments are shown.

### Scenario 6: Reports Screen Chart Numbers & Tooltips
1. Navigate to Reports screen (`Raporlar`).
2. **Verify**:
   - TopAppBar has NO settings gear icon.
   - Monthly earnings chart displays compact values above each bar (e.g., "₺12.5K", "0").
   - Tapping any bar highlights the bar and displays a tooltip popup containing the full formatted amount (e.g., "₺12.500,00") and month name.
