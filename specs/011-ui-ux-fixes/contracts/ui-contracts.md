# UI Contracts & Component Interfaces: 011-ui-ux-fixes

## 1. Homework Bottom Sheet Contract

```kotlin
@Composable
fun HomeworkBottomSheet(
    showSheet: Boolean,
    editingHomework: Homework?,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        description: String,
        dueDate: Long,
        status: HomeworkStatus,
        performanceNotes: String?
    ) -> Unit,
    onDelete: ((Homework) -> Unit)? = null
)
```

### Behavior Contract:
- If `editingHomework != null`:
  - Sheet title: "Edit Homework"
  - Header displays a destructive delete `IconButton` (`Icons.Outlined.Delete`).
  - Tapping the delete icon prompts an `AlertDialog` asking for confirmation.
  - Confirming triggers `onDelete(editingHomework)` and dismisses the sheet.
- If `editingHomework == null`:
  - Sheet title: "Add Homework"
  - Delete icon is omitted from the header.
- Date presentation:
  - Due date field displays date formatted with `DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)`.
  - Never shows raw ISO string "YYYY-MM-DD".

## 2. Calendar Daily Agenda Header Contract

```kotlin
@Composable
private fun CalendarDayDetails(
    selectedDate: LocalDate,
    lessons: List<Lesson>,
    homework: List<Homework>,
    // ... actions
)
```

### Dynamic Title Resolution Logic:
```kotlin
val headerTitle = when {
    lessonsSorted.isNotEmpty() && homework.isNotEmpty() -> 
        tullabStringResource(R.string.calendar_day_schedule_details_title, formattedSelectedDate)
    homework.isNotEmpty() && lessonsSorted.isEmpty() -> 
        tullabStringResource(R.string.calendar_day_homework_details_title, formattedSelectedDate)
    else -> 
        tullabStringResource(R.string.calendar_day_details_title, formattedSelectedDate)
}
```

## 3. Homework Screen Empty State Contract

```kotlin
@Composable
fun HomeworkEmptyState(
    modifier: Modifier = Modifier
)
```
- Centered layout filling container.
- Icon: `Icons.Outlined.Assignment` (size 120.dp, tint `MaterialTheme.colorScheme.surfaceVariant`).
- Title: `R.string.homework_empty_state_title` (headlineSmall, SemiBold).
- Subtitle: `R.string.homework_empty_state_message` (bodyMedium, onSurfaceVariant).

## 4. Settings Screen Notification Row Contract

In `SettingNavigationRow`:
- Title text gets `Modifier.weight(1f).padding(end = 16.dp)` ensuring full separation from value.
- Value text is styled in `MaterialTheme.typography.bodyMedium` with `onSurfaceVariant` and does not run into the title.
