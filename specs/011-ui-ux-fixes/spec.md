# Feature Specification: UI, UX, and Domain Fixes (Homework, Calendar, Settings)

**Feature Branch**: `011-ui-ux-fixes`

**Created**: 2026-09-08

**Status**: Ready for Planning

**Input**: User description: "Please implement the following UI, UX, and domain fixes across the calendar, homework, and settings modules: first, add a permanent homework assignment deletion capability by introducing a destructive delete action inside the edit homework bottom sheet/dialog, ensuring proper database entity removal through the ViewModel/Room pipeline, state emission, and clean sheet dismissal; second, fix the hardcoded daily agenda header in the calendar view so that it dynamically adapts to the selected day's content—displaying a lesson-specific header when only lessons exist, a homework-specific header when only homework exists, and an inclusive schedule/agenda header when both or mixed item types are present; third, upgrade the homework list empty state from a plain text label to a visually consistent, centered empty state matching the design pattern of the student list (featuring an appropriate icon, title, and descriptive subtitle); fourth, resolve the text concatenation bug in the Settings screen notification rows where setting labels and time values merge without proper spacing (such as "Lesson reminder time9:00 AM"), ensuring clean spacing and alignment; and fifth, replace raw ISO date representations (such as YYYY-MM-DD) in the homework input fields and labels with localized date formatting that adheres to the user's active system locale."

## Clarifications

### Session 2026-09-08
- Q: Where should the destructive delete action be placed within the HomeworkBottomSheet? → A: Icon button in the top sheet header (e.g., trash can icon next to the title) with confirmation dialog
- Q: Which phrasing convention should be used for the calendar daily agenda header titles? → A: "Schedule on [Date]" when both exist, "Lessons on [Date]" for lessons only, "Homework on [Date]" for homework only
- Q: Which icon should be used for the centered homework list empty state? → A: Assignment icon (Icons.Outlined.Assignment) with title and subtitle

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Permanent Homework Deletion (Priority: P1)

As a private tutor editing an existing homework assignment, I want the ability to permanently delete the assignment from within the edit sheet header via a trash can icon button with a clear confirmation dialog, so that mistakenly created or obsolete assignments are removed completely from the schedule and database.

**Why this priority**: Users currently have no mechanism to permanently delete an assignment once created from the edit interface, which leaves erroneous or outdated data stuck in their database and calendar.

**Independent Test**:
- Open an existing homework item to edit, tap the trash can icon button in the header, confirm deletion in the dialog, verify that the sheet dismisses and the assignment no longer appears in the homework list or calendar schedule.

**Acceptance Scenarios**:
1. **Given** an existing homework assignment opened in the edit bottom sheet, **When** the user taps the delete icon button located in the sheet header, **Then** a confirmation dialog appears warning the user of permanent deletion.
2. **Given** the delete confirmation dialog is displayed, **When** the user confirms deletion, **Then** the homework assignment is permanently removed from the database, the edit sheet dismisses cleanly, and lists update.
3. **Given** creating a new homework assignment (not editing), **When** the sheet is opened, **Then** the delete icon action is not visible in the header.

---

### User Story 2 - Dynamic Calendar Agenda Header (Priority: P2)

As a tutor inspecting my schedule in the calendar view, I want the daily agenda section header to dynamically reflect the types of items scheduled for the selected day, displaying "Schedule on [Date]" when both lessons and homework are present, "Lessons on [Date]" when only lessons are present, and "Homework on [Date]" when only homework is present.

**Why this priority**: Displaying a misleading or generic hardcoded header causes confusion when the tutor has only lessons or only homework assignments scheduled for a particular day.

**Independent Test**:
- Select a day with only lessons: header shows "Lessons on [Date]".
- Select a day with only homework assignments: header shows "Homework on [Date]".
- Select a day with both lessons and homework: header shows "Schedule on [Date]".
- Select an empty day: header shows "Lessons on [Date]" (or default empty schedule header) with the "No events scheduled for this day" empty state.

**Acceptance Scenarios**:
1. **Given** a selected calendar day containing only scheduled lessons, **When** the user views the daily agenda section, **Then** the header explicitly displays "Lessons on [Date]".
2. **Given** a selected calendar day containing only assigned homework, **When** the user views the daily agenda section, **Then** the header explicitly displays "Homework on [Date]".
3. **Given** a selected calendar day containing both lessons and homework, **When** the user views the daily agenda section, **Then** the header displays "Schedule on [Date]".

---

### User Story 3 - Homework List Empty State Redesign (Priority: P3)

As a tutor viewing an empty homework list, I want to see an engaging, well-proportioned centered empty state with the Assignment icon (`Icons.Outlined.Assignment`), title, and descriptive subtitle matching the design language of the student list, so that the application feels cohesive, polished, and informative.

**Why this priority**: A plain text label looks unpolished and inconsistent with the rest of the application's Material 3 empty-state patterns (such as the student screen).

**Independent Test**:
- Navigate to the Homework tab when no homework assignments exist (or when a filter yields no results), and verify that a centered visual empty state displays featuring the Assignment icon, primary title, and explanatory subtitle.

**Acceptance Scenarios**:
1. **Given** there are no homework assignments in the current filter or overall list, **When** the user navigates to the homework list screen, **Then** the screen presents a vertically centered layout with `Icons.Outlined.Assignment`, a distinct headline, and a helpful description text.
2. **Given** the empty state is displayed across different device screen sizes and font scales, **When** the layout is measured, **Then** it scales gracefully without clipping or misalignment.

---

### User Story 4 - Settings Notification Rows Spacing & Alignment Fix (Priority: P4)

As a tutor configuring reminder settings, I want notification setting titles and their configured time values to be clearly separated and aligned, so that words and numbers do not merge into unreadable strings like "Lesson reminder time9:00 AM".

**Why this priority**: Text concatenation bugs degrade legibility, convey poor polish, and can impair readability for tutors relying on reminder configurations.

**Independent Test**:
- Navigate to the Settings screen and inspect all reminder rows (e.g., lesson reminder time, log reminder time) to verify proper horizontal spacing or dedicated trailing value chips/labels.

**Acceptance Scenarios**:
1. **Given** the Settings screen is displayed, **When** notification time settings are rendered, **Then** the setting label text and the formatted time string have explicit padding/spacing and clear typographic visual hierarchy.
2. **Given** the user changes system font scale up to 200%, **When** notification rows are rendered, **Then** the label and time adapt without overlapping or touching.

---

### User Story 5 - Localized Date Formatting in Homework Dialogs & Labels (Priority: P5)

As a tutor entering or reviewing homework due dates, I want dates to be formatted according to my active device locale and system settings rather than raw ISO strings (YYYY-MM-DD), so that dates are intuitive and culturally familiar.

**Why this priority**: Raw ISO dates feel mechanical and violate Tullab's internationalization principles across English, Turkish, and German language environments.

**Independent Test**:
- Open the homework creation/edit bottom sheet and inspect date input buttons and due date displays in English, Turkish, and German locales.
- Verify dates are formatted using localized patterns (e.g., "Sep 8, 2026" / "8 Eyl 2026" / "08.09.2026") rather than "2026-09-08".

**Acceptance Scenarios**:
1. **Given** a user opens the homework sheet with a selected date, **When** viewing the date picker trigger button or label, **Then** the date is formatted using the user's localized medium/short date representation.
2. **Given** the user switches device language/locale between English, Turkish, and German, **When** viewing homework dates, **Then** date displays conform to standard localized date conventions.

---

### Edge Cases

- **Deleting Homework Tied to Calendar View**: Deleting an item currently selected or visible in the calendar daily agenda must immediately refresh the daily agenda and calendar dot indicators without leaving orphan state or throwing exceptions.
- **Single Item Dynamic Header**: When deleting the last lesson on a day that also has homework, the agenda header must smoothly transition from the mixed "Schedule on [Date]" header to the "Homework on [Date]" header.
- **Extreme Font Scaling on Settings Rows**: Notification rows on small screens at 200% font scale must wrap gracefully (e.g., column layout fallback or multi-line arrangement) rather than truncate or push time values off-screen.
- **Invalid/Null Due Dates**: If a homework item has an empty or unparseable date, the localized formatter must fall back gracefully to a localized placeholder string without crashing.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide a destructive delete icon action (`Icons.Outlined.Delete`) in the sheet header when editing an existing homework assignment.
- **FR-002**: System MUST require explicit user confirmation via an AlertDialog before executing homework assignment deletion.
- **FR-003**: System MUST remove the homework assignment entity permanently from Room persistence via `HomeworkRepository.deleteHomework(homework)` upon confirmed deletion and dismiss the edit sheet cleanly.
- **FR-004**: Calendar daily agenda header MUST display "Lessons on %1$s" (`calendar_day_details_title`) when only lessons exist for the selected day.
- **FR-005**: Calendar daily agenda header MUST display "Homework on %1$s" (`calendar_day_homework_details_title`) when only homework assignments exist for the selected day.
- **FR-006**: Calendar daily agenda header MUST display "Schedule on %1$s" (`calendar_day_schedule_details_title`) when both lessons and homework exist on the selected day.
- **FR-007**: Homework screen empty state MUST feature a vertically centered layout containing the Assignment icon (`Icons.Outlined.Assignment`), a primary title, and an explanatory subtitle adhering to Material 3 styling matching the student list pattern.
- **FR-008**: Settings screen notification time rows MUST enforce distinct layout separation (spacing/alignment) between the setting label and the formatted time value.
- **FR-009**: All homework date inputs and date display labels MUST display dates formatted according to the user's active locale and device date formatting standards instead of raw ISO local date strings.
- **FR-010**: All user-visible strings (headers, empty state texts, dialog labels, buttons) MUST be externalized with complete parity across English (`values`), Turkish (`values-tr`), and German (`values-de`).

### Key Entities

- **Homework**: Represents an assigned homework task associated with a student, containing title, description, due date, completion status, and unique identifier.
- **Agenda Item / Day Schedule**: The aggregated collection of lessons and homework items scheduled for a specific date in the calendar module, determining the contextual header state (Lessons Only, Homework Only, Mixed).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of existing homework assignments can be permanently deleted directly from the edit dialog within 2 taps (delete icon tap + confirmation tap).
- **SC-002**: 100% of calendar day selections display the exact contextual header matching the scheduled items ("Lessons on [Date]", "Homework on [Date]", or "Schedule on [Date]").
- **SC-003**: 0 raw ISO date formats (e.g., "YYYY-MM-DD") are presented to the end user in homework input fields and labels across all supported locales.
- **SC-004**: Settings notification rows maintain visible separation (minimum 8dp spacing or dedicated end alignment) across all supported screen sizes and font scales up to 200%.
- **SC-005**: 100% linguistic parity across English, Turkish, and German for all newly introduced and modified UI strings.

## Assumptions

- Deletion of homework is permanent (hard delete) and does not require a soft-delete/trash bin stage, in alignment with existing app entity management patterns.
- Existing student list empty state components/patterns can be referenced or reused to ensure visual consistency in the homework list empty state.
- Date localization utilizes standard Android/Java time formatting utilities adhering to `LocalLocale.current` and `DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)`.
