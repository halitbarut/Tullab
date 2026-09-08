# Feature Specification: lesson-times-homework-state

**Feature Branch**: `012-lesson-times-homework-state`

**Created**: 2026-09-08

**Status**: Draft

**Input**: User description: "Please implement lesson start time visibility across the UI and refactor homework overdue state handling: first, surface formatted lesson start times using localized time formatting (DateFormat.getTimeFormat) across the application UI, specifically in the calendar daily details lesson cards alongside an Icons.Outlined.Schedule icon (or duration span), on the student dashboard under upcoming, completed, and unlogged past lesson items, and in the header/details of the Log Lesson sheet; second, refactor the homework status domain logic by treating \"Overdue\" strictly as a dynamically computed presentation state (status == PENDING && dueDate < today) rather than an editable database enum—remove \"Overdue\" from user-selectable dropdowns in the homework creation and edit dialogs (leaving only Pending, Completed, and Cancelled), and ensure that the homework list screen, filter tabs, edit sheet, and calendar daily cards consistently evaluate and render the red Overdue badge and styling whenever a pending assignment's due date is in the past."

## Clarifications

### Session 2026-09-08

- Q: Should the homework list screen add a dedicated "Overdue" filter chip alongside All, Pending, and Completed, or should overdue items remain under the "Pending" filter tab with their red badge styling? (FR-007) → A: Keep existing filter tabs (All, Pending, Completed); "Pending" displays all pending items, dynamically rendering the red badge on past-due items.
- Q: How should existing database records with the "OVERDUE" status be handled during the domain model refactoring? (FR-005) → A: Update the Room HomeworkStatusConverter to map legacy 'OVERDUE' strings to HomeworkStatus.PENDING on read without requiring a schema version bump, allowing OVERDUE to be safely removed from the active domain enum.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Lesson Start Time Visibility (Priority: P1)

Users need to see exactly when their lessons start so they can manage their daily schedule effectively, without guessing based on general time blocks.

**Why this priority**: Precise lesson timing is core to a scheduling app's value proposition.

**Independent Test**: Can be fully tested by creating a lesson at a specific time and verifying that this exact localized time appears in the Calendar daily cards, Student dashboard, and Log Lesson sheet.

**Acceptance Scenarios**:

1. **Given** a scheduled lesson starting at 14:30, **When** the user views the calendar daily details, **Then** the time "14:30" (or "2:30 PM" based on locale) is displayed alongside a schedule icon.
2. **Given** a scheduled lesson, **When** the user navigates to the student's dashboard, **Then** the localized start time is visible in the upcoming, completed, and unlogged lesson lists.
3. **Given** an unlogged past lesson, **When** the user opens the Log Lesson sheet, **Then** the localized start time is clearly visible in the sheet's header or details section.

---

### User Story 2 - Dynamic Homework Overdue State (Priority: P1)

Users should not have to manually mark homework as "Overdue". The system should automatically flag pending homework when its due date passes.

**Why this priority**: Reduces manual data entry errors and ensures the UI always accurately reflects the real-time status of assignments.

**Independent Test**: Can be fully tested by creating a pending homework assignment with a past due date and observing that the system styles it as overdue, and verifying "Overdue" is no longer selectable in dialogs.

**Acceptance Scenarios**:

1. **Given** the homework creation or edit dialog, **When** the user opens the status dropdown, **Then** only "Pending", "Completed", and "Cancelled" are available options.
2. **Given** a homework assignment with status "Pending" and a due date of yesterday, **When** the user views the homework list or calendar daily cards, **Then** the item is rendered with a red Overdue badge and corresponding styling.
3. **Given** a homework assignment with status "Completed" and a due date of yesterday, **When** the user views the homework list, **Then** the item is rendered as completed, without any overdue styling.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST display lesson start times formatted using device-local time formatting (`DateFormat.getTimeFormat` equivalent in Android/Compose) across UI components.
- **FR-002**: System MUST display lesson start time and/or duration span on calendar daily details lesson cards, alongside an `Icons.Outlined.Schedule` icon.
- **FR-003**: System MUST display lesson start time on the student dashboard for upcoming, completed, and unlogged past lesson items.
- **FR-004**: System MUST display lesson start time in the header/details of the Log Lesson sheet.
- **FR-005**: System MUST compute the "Overdue" homework state dynamically based on the rule `status == PENDING && dueDate < today`.
- **FR-006**: System MUST NOT allow users to explicitly set a homework status to "Overdue" in any creation or edit dialogs.
- **FR-007**: System MUST display a red Overdue badge and specific styling for any homework that evaluates to the computed Overdue state in the homework list screen, edit sheet, and calendar daily cards; the homework list screen filter tabs MUST retain the existing categories (All, Pending, Completed), with "Pending" including all pending assignments while rendering overdue styling on past-due items.

### Key Entities

- **Lesson**: Represents a scheduled tutoring session. Requires accurate time representation in the UI without altering domain logic.
- **Homework**: Represents an assignment. Its active `status` enum values are `PENDING`, `COMPLETED`, and `CANCELLED`. Overdue state is dynamically computed for presentation.

### Edge Cases

- **Due exactly today**: A pending homework due today should NOT be marked as overdue until tomorrow (`dueDate < today`).
- **No Due Date**: A pending homework without a due date cannot be overdue.
- **Legacy Data**: Existing homework records with an `'OVERDUE'` string in the database MUST be safely deserialized to `HomeworkStatus.PENDING` by the type converter, ensuring backwards compatibility without requiring a database schema migration.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of lesson list items and cards across the specified screens display localized start times.
- **SC-002**: The "Overdue" option is 0% available in any user-facing homework status dropdowns.
- **SC-003**: 100% of pending homeworks with past due dates visually reflect the red overdue styling across all relevant views automatically.

## Assumptions

- "today" logic for overdue calculation evaluates based on the current local date at midnight (ignoring time components).
- Existing data containing 'OVERDUE' strings will be mapped to `PENDING` within `HomeworkStatusConverter` without bumping the Room database version.
- The UI framework is Jetpack Compose (as per project constitution), so standard localized time formatters will be used in the presentation layer.
