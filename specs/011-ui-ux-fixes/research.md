# Phase 0 Research: UI, UX, and Domain Fixes

## Decision 1: Permanent Homework Deletion Pipeline
- **Decision**: Add `suspend fun delete(homework: HomeworkEntity)` to `HomeworkDao`, expose `suspend fun deleteHomework(homework: Homework)` in `HomeworkRepository` and `HomeworkRepositoryImpl`. In `HomeworkViewModel`, expose `fun onDeleteHomework(homework: Homework)` which calls the repository in `viewModelScope` and dismisses the sheet. In `HomeworkBottomSheet`, add an `onDelete: () -> Unit` parameter. If `editingHomework != null`, display an `IconButton` with `Icons.Outlined.Delete` and tint `MaterialTheme.colorScheme.error` in the top header row next to the title. When clicked, show an `AlertDialog` confirming deletion. Upon confirm, invoke `onDelete` and close the dialog and sheet.
- **Rationale**: Follows Tullab Constitution Principles II (Clean Architecture) and V (MVVM/UDF). Ensures direct entity removal from Room SQLite DB, cascades flow emissions through Room's reactive `Flow`, automatically refreshing `HomeworkScreen` and `CalendarScreen` (if calendar listens to homework flow).
- **Alternatives Considered**: 
  - Soft-delete (flag as deleted): Rejected because Tullab uses hard deletes for entities (e.g. students, lessons), and prompt specifically states "permanent homework assignment deletion capability".
  - Bottom action bar button: Rejected based on user clarification preferring top sheet header icon button.

## Decision 2: Calendar Dynamic Agenda Header
- **Decision**: Introduce three string resources (or reuse existing) across `values/strings.xml`, `values-tr/strings.xml`, and `values-de/strings.xml`:
  - English: 
    - `calendar_day_details_title`: "Lessons on %1$s" (existing)
    - `calendar_day_homework_details_title`: "Homework on %1$s" (new)
    - `calendar_day_schedule_details_title`: "Schedule on %1$s" (new)
  - Turkish:
    - `calendar_day_details_title`: "%1$s tarihindeki dersler" (existing)
    - `calendar_day_homework_details_title`: "%1$s tarihindeki ödevler" (new)
    - `calendar_day_schedule_details_title`: "%1$s programı" (new)
  - German:
    - `calendar_day_details_title`: "Stunden am %1$s" (existing)
    - `calendar_day_homework_details_title`: "Hausaufgaben am %1$s" (new)
    - `calendar_day_schedule_details_title`: "Plan am %1$s" (new)
  In `CalendarDayDetails`, resolve header string resource ID dynamically:
  ```kotlin
  val headerRes = when {
      lessonsSorted.isNotEmpty() && homework.isNotEmpty() -> R.string.calendar_day_schedule_details_title
      homework.isNotEmpty() && lessonsSorted.isEmpty() -> R.string.calendar_day_homework_details_title
      else -> R.string.calendar_day_details_title
  }
  ```
- **Rationale**: Meets FR-004, FR-005, FR-006 and the clarified user preference cleanly without complex layout branches.
- **Alternatives Considered**: Creating distinct sub-headers inside the cards; rejected because user specifically selected unified dynamic section header.

## Decision 3: Centered Homework Empty State
- **Decision**: Create a dedicated `HomeworkEmptyState(modifier: Modifier = Modifier)` matching the structure and styling of `StudentListEmptyState`:
  - Centered `Box` / `Column(horizontalAlignment = Alignment.CenterHorizontally)`
  - Icon: `Icons.Outlined.Assignment`, `tint = MaterialTheme.colorScheme.surfaceVariant`, `size = 120.dp`
  - Title: `R.string.homework_empty_state_title` ("No homework yet" / "Henüz ödev yok" / "Noch keine Hausaufgaben"), `style = MaterialTheme.typography.headlineSmall`, `fontWeight = FontWeight.SemiBold`
  - Subtitle: `R.string.homework_empty_state_message` ("Homework assignments will appear here once created." / "Ödevler oluşturulduğunda burada görünecektir." / "Hier erscheinen Hausaufgaben, sobald sie erstellt wurden."), `style = MaterialTheme.typography.bodyMedium`, `color = MaterialTheme.colorScheme.onSurfaceVariant`
  - Ensure it fills the vertical viewport when empty.
- **Rationale**: Delivers visual parity with `StudentListScreen` adhering to Constitution Principle IV (Modern UX & a11y).
- **Alternatives Considered**: In-line text within a small Card; rejected because it fails user request for visual consistency with the student list empty state.

## Decision 4: Settings Notification Rows Spacing & Alignment Fix
- **Decision**: In `SettingsScreen.kt`, update `SettingNavigationRow`:
  - Ensure `title` Text has appropriate `Modifier.weight(1f)` with `padding(end = 16.dp)` so it never collides with `value`.
  - Display `value` inside a styled container or with explicit minimum padding:
    ```kotlin
    Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.weight(1f).padding(end = 16.dp)
    )
    Text(
        text = value,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1
    )
    Spacer(modifier = Modifier.width(8.dp))
    Icon(
        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
        ...
    )
    ```
  - For small devices or high font scaling (200%), add a responsive arrangement or overflow strategy so the title and value do not concatenate.
- **Rationale**: Prevents text concatenation and ensures clean alignment across all locales and font scales.

## Decision 5: Localized Date Formatting in Homework Dialogs & Labels
- **Decision**: In `HomeworkBottomSheet.kt`:
  - Remove `format(DateTimeFormatter.ISO_LOCAL_DATE)` (which was hardcoding `YYYY-MM-DD`).
  - Use `DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)` consistently for both the read-only date text field value and any helper/preview text.
  - Update `R.string.homework_dialog_due_date_hint` from hardcoded "Example: 2024-04-15" to localized format hint or descriptive text (e.g. "Select a due date" / "Son teslim tarihi seçin").
- **Rationale**: Eliminates raw ISO format strings as required by FR-009 and SC-003, ensuring Tullab Constitution Principle VII (Internationalization & Extensible Formatting).
