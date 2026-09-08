# UI Contracts & Screen Specifications: lesson-times-homework-state

## 1. Calendar Daily Details (`CalendarScreen.kt`)

### LessonDetailCard Contract
- **Icon**: `Icons.Outlined.Schedule`
- **Presentation**: Formatted start time using `formatLessonStartTime(context, lesson.date)`.
- **Duration Span**: Displayed adjacent to or with start time (e.g. `$formattedStartTime ($durationText)`).
- **Accessibility**: Content description or clear text semantics for screen readers.

### Homework Items Contract
- **Computed Status**: Evaluated via `homework.isOverdue(today)` (or `status == PENDING && dueDate < today`).
- **Overdue Visuals**:
  - Badge container with `StatusRedContainer`
  - Text: `tullabStringResource(id = R.string.homework_badge_overdue)` colored with `StatusRed`
  - Status label: `tullabStringResource(id = R.string.homework_status_overdue)` with `StatusRed`

---

## 2. Student Dashboard (`DashboardScreen.kt`)

### UpcomingLessonsCard Contract
- **Items**: Each upcoming lesson item surfaces its localized start time alongside the scheduled date.

### CompletedLessonsCard Contract
- **Items**: Each completed lesson item surfaces its localized start time alongside the date.

### LogPastLessonsCard Contract
- **Items**: Each past unlogged lesson item surfaces its localized start time alongside the date.

---

## 3. Log Lesson Sheet (`LogLessonDialog.kt`)

### Header Contract
- Surfaces formatted start time in the header section alongside the formatted date and an `Icons.Outlined.Schedule` icon.
- Displays: `Lesson on <FormattedDate> • <FormattedTime>` (or localized equivalent).

---

## 4. Homework Management (`HomeworkScreen.kt`, `HomeworkBottomSheet.kt`, `HomeworkDialog.kt`)

### HomeworkScreen List & Filter Contract
- **Filter Tabs**: `ALL`, `PENDING`, `COMPLETED`.
- **Filtering Behavior**: `PENDING` includes all pending assignments.
- **Badge Styling**:
  - If `isOverdue`: `homework_status_overdue` label with `StatusRed`
  - If `PENDING` (not overdue): `homework_status_pending` with `StatusYellow`
  - If `COMPLETED`: `homework_status_completed` with `StatusGreen`
  - If `CANCELLED`: `homework_status_cancelled` with `HomeworkGray`

### HomeworkBottomSheet / HomeworkDialog Status Dropdown Contract
- Dropdown options: Strictly `PENDING`, `COMPLETED`, `CANCELLED`.
- No user-selectable `OVERDUE` option.
- When editing a homework assignment that is currently overdue, the sheet displays a non-editable red Overdue badge to indicate its current overdue state.
