# Feature Specification: Lesson Rate Management, Multi-Rate Payment Breakdown, and Calendar Rate Editing

**Feature Branch**: `005-lesson-rate-fixes`  
**Created**: 2026-09-04  
**Status**: Draft  
**Input**: User description: "When I update the hourly rate on September 4 while there is an unpaid lesson created on September 1, the popup incorrectly says there are future planned lessons and is displayed in Turkish even when the app language is English; fix the popup so it correctly refers to the unpaid/past lesson and respects the active app language, and when I choose to keep the existing rate for already planned lessons and apply the new rate only to subsequent lessons, the Dashboard shows the correct total for the current payment cycle but the breakdown below incorrectly calculates `duration × new rate` — each lesson must be calculated using its actual applicable rate, and if the payment cycle contains lessons with different rates, show them as separate breakdowns (e.g. `5 hours × 200 TL` and `2 hours × 400 TL`); additionally, in the Calendar screen there is currently no button or save action for editing the rate of a future planned lesson, so add the necessary UI and save flow; make all changes without breaking existing working behavior or architecture."

## Clarifications

### Session 2026-09-04
- Q: In the Calendar screen, when a tutor edits a future scheduled lesson, which action buttons should be displayed in the edit dialog? → A: Option C: Replace the disabled "Complete Lesson" button with a dedicated "Save Changes" button (persisting rate, mode, and notes in `SCHEDULED` status), while keeping "Mark as Not Done" (to cancel the lesson) and "Cancel" (to discard changes).
- Q: In the Dashboard payment cycle card, should each rate breakdown line display only the duration and rate formula (e.g., `5 hours × 200 TL`), or should it also display each tier's subtotal amount? → A: Option C: Display the formula on the left and the tier subtotal on the right, handling line width and wrapping gracefully on narrow screens to prevent text clipping or layout overflow.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Context-Aware and Fully Localized Rate Change Confirmation Dialog (Priority: P1)

As a private tutor updating a student's profile rate, I want the confirmation dialog to accurately reflect the actual timing of affected uncompleted lessons (distinguishing between past uncompleted lessons and future scheduled lessons) and strictly respect my chosen in-app language, so that I am not misled by incorrect descriptions or foreign language text.

**Why this priority**: Highest priority (P1). An inaccurate dialog stating that past lessons are "future planned lessons" creates severe confusion about lesson scheduling and data integrity. Presenting the dialog in a language different from the app's selected language violates core internationalization requirements.

**Independent Test**: Can be tested by creating an unpaid scheduled lesson dated in the past (e.g., September 1 with current date September 4), switching app language to English, updating the student's profile rate, and verifying that the prompt appears in English and explicitly refers to existing uncompleted/past lessons rather than future scheduled lessons.

**Acceptance Scenarios**:

1. **Given** a student has an uncompleted scheduled lesson dated before today (e.g., September 1 when today is September 4), **When** the tutor updates the student's profile hourly rate, **Then** the confirmation popup accurately states that the student has existing past/uncompleted lessons (not future planned lessons) and asks whether to apply the new rate to them.
2. **Given** the app language is set to English, **When** the rate update confirmation dialog is shown, **Then** all dialog text (title, message, and action buttons) is displayed in English.
3. **Given** the app language is set to Turkish or German, **When** the rate update confirmation dialog is shown, **Then** all dialog text (title, message, and action buttons) is displayed in Turkish or German respectively.
4. **Given** a student has only upcoming future lessons on or after today, **When** the tutor updates the student's profile rate, **Then** the confirmation popup refers to future scheduled lessons.
5. **Given** a student has both past uncompleted lessons and future scheduled lessons, **When** the tutor updates the student's profile rate, **Then** the confirmation popup refers clearly to all existing uncompleted scheduled lessons.

---

### User Story 2 - Accurate Multi-Rate Payment Cycle Breakdown on Dashboard (Priority: P1)

As a private tutor reviewing a student's payment cycle on the Dashboard, I want to see a mathematically accurate breakdown of all lessons grouped by their actual applicable rate, so that the displayed hours and rates transparently account for the total balance due, even when different lessons in the same cycle have different rates.

**Why this priority**: Highest priority (P1). When tutors choose to preserve existing rates for planned lessons while setting a new rate for future lessons, a single cycle contains lessons at different prices. Displaying `total hours × new rate` produces an incorrect equation that contradicts the total amount due, destroying tutor trust in the financial calculations.

**Independent Test**: Can be tested by having 5 hours of completed lessons at 200 TL and 2 hours of completed lessons at 400 TL in the same payment cycle, navigating to the student's Dashboard, and verifying that the total balance displays 1,800 TL and the breakdown explicitly shows both `5 hours × 200 TL` and `2 hours × 400 TL`.

**Acceptance Scenarios**:

1. **Given** a student's current payment cycle contains completed lessons at multiple distinct hourly rates (e.g., 5 hours at 200 TL and 2 hours at 400 TL), **When** the tutor views the payment cycle tracking card on the Dashboard, **Then** the total amount due displays 1,800 TL, and the breakdown shows separate rows for each rate tier with the calculation formula on the left (e.g., `5 hours × 200 TL`) and the tier subtotal on the right (e.g., `1,000 TL`), wrapping or adapting gracefully to screen width without clipping or horizontal overflow.
2. **Given** all completed lessons in the current payment cycle share the same single rate (e.g., 6 hours at 300 TL), **When** the tutor views the Dashboard, **Then** the breakdown displays a single line showing `6 hours × 300 TL`.
3. **Given** a payment cycle contains a mixture of hourly lessons and flat-fee lessons, **When** the tutor views the Dashboard, **Then** each distinct rate or flat-fee tier is shown as a separate breakdown entry, and the sum of all entries equals the total balance due.
4. **Given** a payment cycle has zero completed lessons, **When** viewing the Dashboard, **Then** the card displays a 0 balance with no breakdown rows.

---

### User Story 3 - Editing and Saving Future Planned Lesson Rates from Calendar (Priority: P2)

As a private tutor managing lessons in the Calendar screen, I want to edit and save the rate or pricing mode of an upcoming planned lesson directly from the calendar, without prematurely marking the lesson as completed or cancelled.

**Why this priority**: High priority (P2). Tutors often need to negotiate or adjust the rate for a specific future session in advance. Currently, the calendar only allows completing or cancelling a lesson, leaving tutors unable to adjust planned rates on individual scheduled lessons.

**Independent Test**: Can be tested by selecting an upcoming future scheduled lesson in the Calendar, opening its edit flow, modifying the rate amount, tapping the dedicated Save action, and verifying that the lesson remains in scheduled status with the updated rate saved.

**Acceptance Scenarios**:

1. **Given** an upcoming scheduled lesson on the Calendar, **When** the tutor opens the lesson edit dialog, **Then** the dialog displays three actions: a primary "Save Changes" action to update rate, pricing mode, and notes while keeping the lesson `SCHEDULED`; "Mark as Not Done" to cancel the lesson; and "Cancel" to discard changes, with the disabled "Complete Lesson" button removed.
2. **Given** the tutor modifies the rate or pricing mode of an upcoming scheduled lesson in the Calendar edit dialog and taps "Save Changes", **When** the dialog closes, **Then** the lesson status remains `SCHEDULED`, the new rate is persisted, and the calendar card updates to reflect the new rate and calculated value.
3. **Given** the tutor opens the edit dialog for an upcoming scheduled lesson, **When** the tutor makes changes but taps "Cancel" or dismisses the dialog, **Then** the lesson's rate, mode, notes, and status remain unchanged.
4. **Given** an upcoming scheduled lesson in the edit dialog, **When** the tutor taps "Mark as Not Done", **Then** the lesson transitions to `CANCELLED` status.
5. **Given** a past uncompleted lesson that requires logging, **When** viewed in the Calendar, **Then** the tutor retains the option to log completion, mark not done, or adjust pricing details.

---

### Edge Cases

- **Rate Update with Zero Scheduled Lessons**: When a tutor changes a student's profile rate and the student has no uncompleted or scheduled lessons, the profile update is saved immediately without presenting any confirmation dialog.
- **Rate Update with Only Flat-Fee Scheduled Lessons**: If existing scheduled lessons use Flat Fee pricing mode, changing the profile hourly rate only offers to update hourly lessons, leaving flat fees intact.
- **Fractional Hours in Payment Breakdown**: When lessons have fractional durations (e.g., 1.5 hours at 200 TL and 2.25 hours at 300 TL), durations must be formatted according to the active locale's number format without floating-point precision artifacts (such as 1.50000001).
- **Multiple Lessons at the Same Rate with Different Durations**: Lessons within the same payment cycle that share the identical hourly rate must have their durations combined into a single aggregated line for that rate tier (e.g., two 1.5-hour lessons at 200 TL display as `3 hours × 200 TL`).
- **Dynamic Language Change**: If the tutor changes the app language in Settings from Turkish to English (or vice versa), all dialogs, breakdown text, and calendar actions must immediately display in the newly selected language without requiring an application restart.
- **Paid and Completed Lessons Immunity**: Updating a student's profile rate or editing a future scheduled lesson's rate must never alter previously paid lessons or completed lessons from earlier cycles.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The rate change confirmation dialog MUST strictly use the active in-app language preference rather than defaulting to the device system language.
- **FR-002**: The rate change confirmation dialog MUST dynamically adapt its message text based on the scheduled dates of existing uncompleted lessons:
  - If all uncompleted scheduled lessons are dated prior to the current day, the message MUST specifically refer to existing past/uncompleted lessons.
  - If all uncompleted scheduled lessons are dated on or after the current day, the message MUST refer to future scheduled lessons.
  - If uncompleted scheduled lessons exist both before and on/after the current day, the message MUST refer to all existing scheduled lessons.
- **FR-003**: The rate change confirmation dialog MUST offer two explicit choices:
  - Apply the new rate to existing uncompleted lessons.
  - Keep the original rates for existing uncompleted lessons and apply the new rate only to future lessons created subsequently.
- **FR-004**: In the Dashboard payment tracking card, the system MUST calculate and display the rate breakdown grouped by distinct applicable rate.
- **FR-005**: If all completed unpaid lessons in the current cycle have the same rate, the Dashboard MUST display a single breakdown line showing the total hours and that applicable rate.
- **FR-006**: If completed unpaid lessons in the current cycle have multiple distinct rates, the Dashboard MUST display separate breakdown rows for each rate tier (e.g., `5 hours × 200 TL` with subtotal `1,000 TL`).
- **FR-007**: Each breakdown tier MUST show the duration and rate formula on the left and the formatted subtotal amount on the right, formatting text to wrap or stack responsively to accommodate narrow line widths without clipping or layout overflow.
- **FR-008**: The total amount due displayed on the Dashboard payment tracking card MUST equal the sum of each completed unpaid lesson's calculated value.
- **FR-009**: In the Calendar screen, the edit dialog for a future scheduled lesson MUST replace the disabled "Complete Lesson" action with a primary "Save Changes" action, while retaining "Mark as Not Done" (to cancel the lesson) and "Cancel" (to discard changes).
- **FR-010**: Tapping the "Save Changes" action in the Calendar edit dialog MUST persist the modified rate, pricing mode, and notes while retaining the lesson in `SCHEDULED` status.
- **FR-011**: For past uncompleted lessons that require logging, the Calendar interface MUST retain the "Log Details" flow allowing tutors to mark the lesson as completed, mark it not done, or adjust its pricing.
- **FR-012**: All user-visible strings for dialog titles, messages, breakdown labels, and button actions MUST be externalized and available in English (`values`), Turkish (`values-tr`), and German (`values-de`).

### Key Entities *(include if feature involves data)*

- **Lesson**: Represents a scheduled or completed tutoring session. Relevant attributes include scheduled timestamp/date, lesson status (`SCHEDULED`, `COMPLETED`, `PAID`, `CANCELLED`), duration in hours, pricing mode (`PER_HOUR` or `FLAT_FEE`), and rate or fee amount (`rateOrFee`).
- **Student**: Represents a student profile. Contains the student's default profile hourly rate (`customHourlyRate`) used as the initial pricing template for newly scheduled lessons.
- **Payment Cycle Rate Breakdown Tier**: A read model representing an aggregated group of completed unpaid lessons within a single payment cycle that share the same pricing mode and rate. Attributes include total accumulated duration, unit rate or fee, and subtotal amount.

## Assumptions

- "Unpaid lesson created on September 1" refers to a scheduled lesson whose date is September 1 (in the past relative to September 4) and has not yet been marked completed or paid.
- When multiple lessons in a payment cycle share the exact same hourly rate, grouping them into a single line item (summing their durations) provides the clearest and most concise breakdown for tutors.
- The existing rate locking architecture established in feature 004 remains intact: completed lessons retain their historical pricing snapshots, and profile rate updates only cascade to uncompleted lessons when explicitly authorized by the tutor.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of user interface text across the rate update dialog, payment cycle breakdown, and calendar editing screens strictly matches the user's active in-app language selection (EN, TR, DE).
- **SC-002**: 0% false references to future lessons: When only past uncompleted lessons exist, 100% of rate update dialog occurrences correctly describe them as past or existing lessons.
- **SC-003**: 100% mathematical consistency on the Dashboard: For any payment cycle with single or multiple rate tiers, the sum of all breakdown tiers exactly equals the displayed total amount due.
- **SC-004**: Tutors can edit and save the rate of a future planned lesson from the Calendar screen in 3 or fewer taps without changing its scheduled status.
- **SC-005**: All rate editing, dialog prompts, and payment breakdown calculations execute completely offline with zero network calls and immediate persistence.
