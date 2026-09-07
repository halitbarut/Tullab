# Quickstart Validation Guide: Refined Improvements & Bug Fixes

## Verification Scenarios

### Scenario 1: Calendar Mark as Paid Duration Editing
1. Open Tullab on an Android device or emulator.
2. Navigate to **Calendar**.
3. Select an unpaid scheduled or completed lesson and click **Mark as Paid**.
4. **Verify**:
   - The "Duration (in hours)" text field is enabled and editable.
   - The locked duration helper text is NOT displayed.
   - Edit the duration (e.g. from `1.0` to `2.0`).
   - Tap **Mark as Paid**.
   - Verify that the lesson is marked as paid and the duration is updated to 2.0 hours with recalculated total value.

### Scenario 2: Log Details Dialog Button Hierarchy
1. On **Calendar**, select a past scheduled lesson and click **Log Details**.
2. **Verify**:
   - The action buttons display Cancel on the left, Mark as Not Done in the middle, and Complete on the right.
   - Now select a future scheduled lesson and click **Edit**.
   - Verify Cancel is on the left and Save Changes is on the right, maintaining visual and spatial consistency.

### Scenario 3: Pluralization (English, German, Turkish)
1. Switch language to English in Settings.
2. Check a lesson with duration of `1.0` hour: verify it reads "1 hour" (singular).
3. Check a lesson with duration of `1.5` or `2.0` hours: verify it reads "1.5 hours" / "2 hours" (plural).
4. Switch language to German in Settings.
5. Verify `1.0` reads "1 Stunde" and `2.0` reads "2 Stunden".
6. Switch language to Turkish in Settings.
7. Verify `1.0` reads "1 saat" and `2.0` reads "2 saat".

### Scenario 4: ISO 4217 Currency Selection & Search
1. Navigate to **Settings** > **Currency**.
2. **Verify**:
   - A search bar is present at the top of the dialog.
   - Typing "GBP" filters the list to British Pound Sterling.
   - Selecting GBP persists the preference.
   - Returning to Student List / Calendar / Reports shows rates and totals formatted in GBP (`£`).

### Scenario 5: Reports Header & Turkish Lira Symbol
1. Set preferred currency to **TRY**.
2. Navigate to **Reports**.
3. **Verify**:
   - The top app bar shows "Reports" (or "Raporlar" / "Berichte").
   - There is NO secondary "Reports" title in the scrollable body content.
   - The Total Earnings card displays the amount formatted with the Turkish Lira symbol (`₺`), e.g., `₺1.250,00` or `1.250,00 ₺`.

---

## Automated Test Execution

Run the full unit test suite from repository root:
```bash
./gradlew testDebugUnitTest
```
Ensure all tests for `CurrencyFormatter`, `SettingsViewModel`, `CalendarViewModel`, and `ReportsScreen` pass cleanly.
