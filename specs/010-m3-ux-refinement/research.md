# Research: Material 3 Standards & Cross-Screen UX Refinement

## Decision 1: Speed Dial FAB on Calendar Screen

### Decision
Implement a custom Material 3 animated Speed Dial FAB on `CalendarScreen` consisting of:
- A primary FloatingActionButton (using `Icons.Filled.Add` with rotation animation to close icon when expanded).
- A scrim / dismissible overlay when open.
- Two mini action items arranged vertically above the FAB with clear text labels:
  1. "Seçili Güne Planla" (`Icons.Outlined.Event` / `Icons.Outlined.EditCalendar`) — opens the single lesson creation dialog for `selectedDate`.
  2. "Toplu Ders Ekle" (`Icons.Outlined.DateRange`) — navigates to `BulkScheduleScreen`.
- Remove the in-feed `FilledTonalButton` from `CalendarScreenContent`.

### Rationale
- Completely eliminates visual competition between the bottom-right FAB and the full-width in-feed button.
- Keeps primary scheduling actions right under the user's thumb in one ergonomic location.
- Respects Material 3 FAB and Speed Dial guidelines with smooth scale/fade micro-animations.

### Alternatives Considered
- *Moving Bulk Schedule to TopAppBar*: Overcrowds the top bar on mobile and separates creation actions across opposite corners of the screen.
- *Extended FAB with in-place text change*: Only allows one action at a time without selection.

---

## Decision 2: Inline Status Selection in Save/Log Lesson Sheet

### Decision
In `LogLessonDialog` (and bottom sheet variant), replace the three competing action buttons (`TextButton` Cancel, red `TextButton` Not Done, and `Button` Save) with:
- An M3 `SingleChoiceSegmentedButtonRow` (or stylized segmented toggle) with three options:
  1. "Planlandı" (`LessonStatus.SCHEDULED`)
  2. "Yapıldı" (`LessonStatus.COMPLETED`)
  3. "Yapılmadı" (`LessonStatus.CANCELLED`)
- Smart initialization based on lesson date:
  - If lesson date is in the past or today: default selection is "Yapıldı" (Completed).
  - If lesson date is in the future: default selection is "Planlandı" (Scheduled).
  - If editing an existing lesson: initialize to its actual current status (`SCHEDULED`, `COMPLETED`, or `CANCELLED`).
- A single high-contrast primary `Button` labeled "Kaydet" (Save) and a neutral "İptal" (Cancel) button.

### Rationale
- Decouples status selection from the commit action, eliminating confusing button collisions.
- Gives the user full explicit control over lesson attendance before saving.
- Standard M3 segmented button provides clear visual feedback and accessible touch targets (min 48 dp height).

### Alternatives Considered
- *2-state Switch (Held vs Not Held)*: Inadequate for scheduled lessons because a tutor may edit details of a future scheduled lesson without marking it held or cancelled.
- *Dropdown menu*: Requires extra taps and hides choices behind a popup.

---

## Decision 3: Reports Chart Data Labels & Interactive Tooltip

### Decision
Update `BarChart` in `ReportsScreen`:
- Render compact formatted currency amounts directly above each bar (e.g. "₺12.5K", "₺800", "0") using a helper formatter function `formatCompactCurrency(amount, currencyCode, locale)`.
- Enable tap selection on each bar column.
- When a bar is tapped, display an anchored tooltip / popup container above the selected bar showing:
  - Full unrounded formatted amount (e.g. "₺12.450,00").
  - Full formatted month and year (e.g. "Ağustos 2026").
- Dismiss tooltip when tapping outside or tapping another bar.

### Rationale
- Compact numbers above bars give instant overview readability without risk of horizontal text overlap or ugly line truncation.
- Tooltips provide 100% precision on demand with zero clutter.
- Works cleanly with dynamic font scaling up to 200%.

### Alternatives Considered
- *Static full currency text*: Causes severe horizontal text overlap when multiple bars have 5-6 digit figures or on narrow screen widths.
- *Tooltip only (no numbers above bars)*: Forces the user to tap every individual bar to see any monetary figures.

---

## Decision 4: Material 3 Pill SearchBar on Student List

### Decision
Replace `StudentListSearchField`'s rectangular `OutlinedTextField` with a pill-shaped container using `RoundedCornerShape(28.dp)` (or standard M3 SearchBar styling) with a subtle container fill (`MaterialTheme.colorScheme.surfaceContainerHigh` or `surfaceVariant.copy(alpha = 0.5f)`), a search magnifying glass leading icon, and an instant clear button.

### Rationale
- Complies directly with Material 3 SearchBar design guidelines.
- Visually distinguishes the top search query area from data entry form fields.

---

## Decision 5: Tap Target & Clutter Elimination on Student List Cards

### Decision
In `StudentListItem`:
- Make the entire `Card` clickable to navigate to the student dashboard (`onStudentClick`).
- Remove the standalone `IconButton` with `Icons.Outlined.Edit` and the trailing `Icons.Outlined.ChevronRight`.
- The student profile remains editable from the Student Dashboard top app bar (`Icons.Outlined.Edit` is already present there).

### Rationale
- Eliminates dual competing actions on list cards (tapping card vs tapping pencil vs tapping chevron).
- Maximizes the touch target area for one-handed operation while providing clean, modern typography and initials avatar.

---

## Decision 6: Student Detail Header & Payment Card Contrast

### Decision
- Update `topBarTitle` in `DashboardScreen` so when a student is selected, it displays `it` directly (e.g., "Elif Yılmaz") rather than `dashboard_student_label` ("Öğrenci: Elif Yılmaz").
- In `PaymentTrackingCard`, upgrade the "Ödendi olarak işaretle" button:
  - Use `Button` with high-contrast container (primary or high-contrast container with distinct elevation/border), ensuring WCAG AA contrast ratio (> 4.5:1) against the card surface.

---

## Decision 7: Homework Horizontal Filter Chips & BottomSheet Conversion

### Decision
- In `HomeworkScreen`, add a horizontal row of `FilterChip` items below the header:
  - "Tümü" (`HomeworkFilter.ALL`)
  - "Bekleyenler" (`HomeworkFilter.PENDING`)
  - "Tamamlananlar" (`HomeworkFilter.COMPLETED`)
- Replace `AlertDialog` in `HomeworkDialog.kt` with `ModalBottomSheet`:
  - Anchored to bottom of the screen.
  - Full IME window inset padding (`Modifier.imePadding()`) so keyboard does not obscure fields.
  - Unify all inputs into `OutlinedTextField` with rounded corners matching the design system.

---

## Decision 8: TopBar Settings Gear Icon Scoping

### Decision
In `TullabNavGraph.kt`:
- Change `derivedStateOf` default actions: do NOT default to `listOf(defaultSettingsAction)` when `topBarConfig.actions` is empty or null.
- Settings gear icon MUST only be displayed on top-level root destinations where explicitly desired (Student List Screen), and MUST be removed from:
  - `DashboardScreen` (remove settings action, keep edit action)
  - `CalendarScreen` (empty actions)
  - `HomeworkScreen` (empty actions)
  - `ReportsScreen` (empty actions)
  - `SettingsScreen` (no settings icon on itself!)
  - `NewStudentScreen` / creation forms.
