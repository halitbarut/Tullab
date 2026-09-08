# Research & Technical Decisions: lesson-times-homework-state

## Decision 1: Localized Lesson Start Time Formatting

- **Decision**: Use `android.text.format.DateFormat.getTimeFormat(context).format(Date(epochMillis))` via a centralized helper function `formatLessonStartTime(context: Context, epochMillis: Long): String` and `@Composable fun formatLessonStartTime(epochMillis: Long): String`.
- **Rationale**: `android.text.format.DateFormat.getTimeFormat(context)` is Android's authoritative API for formatting clock times according to the user's localized preferences, properly reflecting 12-hour (AM/PM) vs 24-hour formats and OEM system settings.
- **Alternatives Considered**:
  - `java.time.format.DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)`: While part of standard Java time, it does not reliably respect Android system-level 24-hour user toggles across all manufacturer device configurations.

## Decision 2: Dynamic Homework Overdue State

- **Decision**: Treat "Overdue" strictly as a computed presentation condition: `homework.status == HomeworkStatus.PENDING && dueDateLocalDate.isBefore(todayLocalDate)`.
- **Rationale**: Eliminates synchronization issues between actual time and database records. The assignment is either pending, completed, or cancelled. If it is pending and its due date has passed, the presentation layer renders the red Overdue badge.
- **Alternatives Considered**:
  - Cron/Worker updating database status to `OVERDUE`: Requires background worker execution, adds state inconsistency if worker fails or app is closed, and complicates status rollbacks.
  - Retaining `OVERDUE` in `HomeworkStatus` enum as a user-selectable state: Violates requirements and causes confusing UX where users could manually select "Overdue" or struggle to undo it.

## Decision 3: Legacy Database Deserialization Strategy

- **Decision**: Update `HomeworkStatusConverter` to map `"OVERDUE"` to `HomeworkStatus.PENDING` on read (`toStatus`), while `fromStatus` writes active enum names (`PENDING`, `COMPLETED`, `CANCELLED`).
- **Rationale**: Safely deserializes existing database rows created under previous versions without throwing `IllegalArgumentException` and without requiring a Room schema migration or database version bump.
- **Alternatives Considered**:
  - Room `@Migration` with database version bump: Converts SQLite records directly via `UPDATE homework SET status = 'PENDING' WHERE status = 'OVERDUE'`, but requires schema increment and migration test setup for no functional difference compared to converter fallback.

## Decision 4: UI Surface Integration Points

- **Calendar Daily Details**:
  - `LessonDetailCard`: Display formatted start time alongside `Icons.Outlined.Schedule` icon and duration span (e.g. `14:30 (60 min)`).
  - Homework items in `CalendarScreen`: Dynamically compute `isOverdue` and render red Overdue badge without depending on `HomeworkStatus.OVERDUE`.
- **Student Dashboard**:
  - `UpcomingLessonsCard`: Display date and formatted start time for each upcoming lesson.
  - `CompletedLessonsCard`: Display date and formatted start time for each completed lesson.
  - `LogPastLessonsCard`: Display date and formatted start time for each unlogged past lesson.
- **Log Lesson Sheet (`LogLessonDialog`)**:
  - Display formatted start time alongside date and `Icons.Outlined.Schedule` icon in sheet header.
- **Homework Screen & Dialogs**:
  - `HomeworkScreen`: Filter tabs retain `ALL`, `PENDING`, `COMPLETED`. `PENDING` tab includes all pending assignments; any item with past due date renders the red Overdue badge.
  - `HomeworkBottomSheet` & `HomeworkDialog`: Dropdown options limited to `PENDING`, `COMPLETED`, `CANCELLED`. In edit mode, if assignment is pending and past due, an Overdue badge is displayed.
