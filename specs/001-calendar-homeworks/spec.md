# Feature Specification: Show Homeworks on Calendar

**Feature Branch**: `001-calendar-homeworks`  
**Created**: 2026-03-04  
**Status**: Draft  
**Input**: User description: "I have a AI-Powered Private Tutoring Tracking Application jetpack compose project here. I have 3 screens which are moderated by a bottom nav bar. In calendar screen, it shows upcoming and past lessons. I want it to show upcoming and past homeworks. Firstly analyze the project and understand the structure."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View Homework Due Dates on Calendar (Priority: P1)

As a tutor, I want to see which days have homework deadlines directly on the monthly calendar view so that I can plan my tutoring schedule effectively and follow up with students.

**Why this priority**: High value. This is the core request. It provides an immediate overview of student workload and deadlines alongside lessons.

**Independent Test**: Can be tested by creating homework with a specific due date and verifying that an indicator appears on that date in the monthly calendar.

**Acceptance Scenarios**:

1. **Given** a student has homework due on March 15th, **When** the user views the March calendar, **Then** the day cell for March 15th shows a visual indicator for homework.
2. **Given** multiple homeworks are due on the same day, **When** the user views that day, **Then** the indicator remains clearly visible (either combined or distinct).

---

### User Story 2 - Access Homework Details from Calendar (Priority: P1)

As a tutor, I want to click on a day in the calendar and see the details of any homework due on that day, including the title, description, and status.

**Why this priority**: High value. Seeing that homework is due is only useful if the user can then see *what* homework it is.

**Independent Test**: Can be tested by selecting a day with homework and verifying that the homework details (title, description, status) appear in the day's detail list.

**Acceptance Scenarios**:

1. **Given** the user selects a day with homework due, **When** the detail section updates, **Then** it lists the homework title and description alongside any scheduled lessons.
2. **Given** the user selects a day with no homework, **When** the detail section updates, **Then** it does not show any homework-related items.

---

### User Story 3 - Distinguish Homework Status (Priority: P2)

As a tutor, I want to see whether homework is pending or completed on the calendar and in the details view so that I can quickly identify overdue or unfinished tasks.

**Why this priority**: Medium-High value. Helps with "past homeworks" tracking to see if they were actually done.

**Independent Test**: Can be tested by toggling homework status and verifying the visual change in both the calendar indicator and the detail list.

**Acceptance Scenarios**:

1. **Given** a homework is marked as "Completed", **When** viewed on the calendar/detail list, **Then** it uses a "completed" style (Teal color, `#009688`).
2. **Given** a homework is "Pending" and past its due date, **When** viewed, **Then** it uses an "overdue" alert style (Magenta color, `#E91E63`).

---

### Edge Cases

- **No Homework/Lessons**: How does the detail section look when a day is selected but has neither lessons nor homework? (It should show a friendly "No events for this day" message).
- **Time Zones**: How are due dates handled if the tutor moves across time zones? (Dates should remain consistent with the student's local date).
- **Overlapping Events**: How are indicators displayed if a day has both a lesson and multiple homeworks? Two side-by-side dots are shown: left dot = lesson status color (most urgent lesson), right dot = homework status color (most urgent homework). If only one type exists, a single dot is shown.
- **State Transitions**: Allowed homework status transitions:
    - `PENDING` ↔ `COMPLETED` (toggle via calendar action)
    - `PENDING` → `OVERDUE` (automatic, system-determined when past due date)
    - `OVERDUE` → `COMPLETED` (tutor can mark overdue homework as done)
    - Any status → `CANCELLED` (one-way; cancelled homework cannot be reactivated from calendar)

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST display homework indicators on the Monthly Calendar View for days that match the `dueDate` of any homework.
- **FR-002**: System MUST list homework items in the `DayDetailsSection` (renamed from `LessonDetailsSection`) when a specific day is selected.
- **FR-003**: Homework items in the detail list MUST include the Title, Description, and Status.
- **FR-004**: System MUST use distinct color schemas for lesson and homework indicators/cards:
    - *Lessons*: Paid = Green, Completed (awaiting payment) = Yellow, Scheduled (future) = Blue, Cancelled = Red.
    - *Homework*: Completed = Teal, Pending (future) = Orange, Overdue = Magenta, Cancelled = Gray.
    - Calendar day indicators use two side-by-side dots when both types are present (left = lesson, right = homework). Each dot shows the most urgent status for its type. If only one type exists on a day, a single dot is shown.
- **FR-005**: The `CalendarViewModel` MUST fetch homework data for the current student alongside lesson data.
- **FR-006**: System MUST allow users to mark homework as completed directly from the calendar's daily details list via a quick action (e.g., a "Mark as Complete" button).
- **FR-007**: System MUST allow navigating to the full homework details/edit screen by clicking on a homework item in the calendar's daily details list.

## Assumptions

- **A-001**: Calendar day cells use two side-by-side indicator dots when both lessons and homework exist on the same day (left dot = lesson status, right dot = homework status). If only one type is present, a single centered dot is shown.
- **A-002**: Toggling homework status from the calendar will immediately persist the change to the local database.
- **A-003**: The user wants to see homework based on its `dueDate`.

### Key Entities *(include if feature involves data)*

- **Homework**: Represents a student task.
    - `id`: Unique identifier (Int).
    - `studentId`: Foreign key to Student (Int).
    - `title`: Short name of the homework (String).
    - `description`: Detailed instructions (String).
    - `creationDate`: Timestamp when the homework was created (Long).
    - `dueDate`: The timestamp for the deadline (Long).
    - `status`: One of `PENDING`, `COMPLETED`, `OVERDUE`, or `CANCELLED`.
    - `performanceNotes`: Optional tutor notes on student performance (String?).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of homeworks with a valid `dueDate` are correctly represented on the monthly calendar.
- **SC-002**: Users can access homework details with a single click on the calendar day.
- **SC-003**: Visual distinction between "Pending" and "Completed" homework is clear, with 0% ambiguity in status reporting.
- **SC-004**: Performance of the calendar screen remains responsive (less than 100ms for day selection) even with 100+ homework items per student.

## Clarifications

### Session 2026-03-05

- Q: How many homework statuses should the system formally support? → A: 4 statuses: `PENDING`, `COMPLETED`, `OVERDUE`, `CANCELLED`.
- Q: What color should represent homework on the calendar? → A: Homework uses a completely distinct palette from lessons — Orange (pending), Teal (completed), Magenta (overdue), Gray (cancelled). Combined calendar day indicator uses priority-based urgency logic.
- Q: What state transitions should be allowed for homework status? → A: Directed: `PENDING↔COMPLETED`, `PENDING→OVERDUE` (auto), `OVERDUE→COMPLETED`, `Any→CANCELLED` (one-way, no reactivation).
- Q: Should `LessonDetailsSection` be renamed? → A: Yes, rename to `DayDetailsSection` to reflect that the component now displays both lessons and homework.
- Q: How should the calendar day indicator handle days with both lessons and homework? → A: Two side-by-side dots: left = lesson status color, right = homework status color. Single dot if only one type exists.
