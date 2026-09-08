# Feature Specification: Comprehensive Haptic & Visual Feedback with Undo

**Feature Branch**: `013-feedback-haptics-undo`

**Created**: 2026-09-08

**Status**: Draft

**Input**: User description: "Please audit and implement comprehensive haptic and visual feedback mechanisms across the application according to Material 3 design and usability heuristics: first, integrate tactile feedback via Jetpack Compose's LocalHapticFeedback on key user interactions—providing a satisfying confirmation haptic when marking lessons as paid, a crisp tactile click when toggling homework to completed, a distinct feedback pulse on segmented button selections, and subtle warning haptics on destructive deletion actions; second, introduce contextual visual feedback using Material 3 SnackbarHost (paired with an actionable "Undo" action where applicable) for critical state changes, specifically when marking lessons as paid ("Payment of [Amount] recorded" with Undo), completing homework ("Homework completed" with Undo), deleting lessons or homework ("Item deleted" with Undo), and executing CSV backup export/import operations with clear success/failure Snackbars; and third, ensure complete linguistic parity across English, Turkish, and German for all newly introduced snackbar feedback labels and undo actions without interrupting user navigation."

## Clarifications

### Session 2026-09-08
- Q: How should the deletion and "Undo" mechanism behave for items (lessons and homework) in terms of data persistence? → A: Immediate deletion from database with full in-memory snapshot re-inserted upon tapping "Undo".
- Q: Where should the Snackbar notifications and Undo state orchestration be hosted to ensure navigation between screens doesn't interrupt active notifications or cause memory leaks? → A: Root Compose scaffold in `TullabNavGraph` orchestrated by `TullabScaffoldController` so Snackbars and Undo persist smoothly across screen navigation.
- Q: If the user performs a new undoable action while a previous Undo Snackbar is still active, how should the system handle the active Undo action? → A: Replace the active Snackbar with the new one and commit the previous action (only the latest action is undoable).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Payment Recording Tactile Feedback & Undoable Snackbar (Priority: P1)

As a private tutor managing lesson records, when I mark a lesson as paid, I want clear physical confirmation and a non-intrusive visual notification displaying the recorded amount along with an "Undo" action, so that I have complete confidence the financial transaction was recorded and can immediately rectify accidental taps.

**Why this priority**: Lesson payment tracking directly impacts the tutor's earnings and financial accuracy. Providing both immediate tactile confirmation and an instant Undo capability prevents erroneous records without interrupting the tutor's workflow.

**Independent Test**: Can be verified by navigating to any unpaid lesson (e.g., in Calendar or Student details), tapping "Mark as Paid", feeling the confirmation haptic pulse, observing the visual Snackbar with formatted payment amount and "Undo" button, tapping "Undo", and verifying the lesson reverts to unpaid status while updating totals.

**Acceptance Scenarios**:

1. **Given** an unpaid lesson with a specific fee (e.g., $50), **When** the user marks the lesson as paid, **Then** the device emits a confirmation tactile feedback pulse, the lesson state updates to paid, and a transient visual Snackbar displays "Payment of [Amount] recorded" with an "Undo" action hosted at the top-level app scaffold.
2. **Given** the payment confirmation Snackbar is visible, **When** the user taps "Undo", **Then** the payment record is reverted (lesson marked unpaid), the UI updates immediately, and the Snackbar dismisses.
3. **Given** the payment confirmation Snackbar is visible, **When** the user navigates away to another screen or ignores the Snackbar until it times out, **Then** the Snackbar continues displaying across the destination screen until timeout, and the payment state remains committed as paid.

---

### User Story 2 - Homework Completion Feedback & Immediate Undo (Priority: P2)

As a tutor tracking student assignments, when I toggle a homework item to completed, I want a crisp tactile click and a visual notification with an "Undo" action, so that I get immediate positive feedback on task completion with a safety net for accidental touches.

**Why this priority**: Homework status changes are high-frequency interactions across the dashboard, calendar, and student views; responsive feedback prevents double-taps and accidental completions.

**Independent Test**: Can be tested by checking off an incomplete homework item, feeling the crisp click feedback, verifying the "Homework completed" Snackbar appears with an "Undo" action, tapping "Undo", and observing the homework item return to incomplete.

**Acceptance Scenarios**:

1. **Given** an incomplete homework item in any homework list or details view, **When** the user toggles the completion checkbox/action, **Then** a crisp tactile click is triggered, the homework is marked completed, and a Snackbar displays "Homework completed" with an "Undo" action.
2. **Given** the homework completed Snackbar is displayed, **When** the user taps "Undo", **Then** the homework item status is restored to incomplete and the Snackbar dismisses.

---

### User Story 3 - Destructive Deletion Warning Haptic & Undo Recovery (Priority: P3)

As a user deleting a lesson or a homework assignment, I want a distinct subtle warning tactile feedback when confirming deletion, followed by a contextual Snackbar notification with an "Undo" action, so that I am alerted to the destructive nature of the action and can effortlessly recover unintentionally deleted records.

**Why this priority**: Deleting lessons or homework causes data loss. Even if preceded by a confirmation dialog, providing a warning haptic and an instant post-deletion Undo action dramatically improves user trust and system safety.

**Independent Test**: Can be tested by deleting a lesson or homework item, feeling the subtle warning haptic, verifying the item is removed from view, and tapping "Undo" on the "Item deleted" (or specific "Lesson deleted" / "Homework deleted") Snackbar to immediately restore the item.

**Acceptance Scenarios**:

1. **Given** an existing lesson or homework item, **When** the user executes deletion, **Then** a subtle warning tactile feedback is emitted, the item is immediately deleted from the database while keeping an in-memory snapshot, and an app-level Snackbar appears stating "Item deleted" (or specific entity) with an "Undo" action.
2. **Given** the deletion Snackbar is active, **When** the user taps "Undo", **Then** the in-memory snapshot is re-inserted into the database, fully restoring the item with all its original attributes and relationships.
3. **Given** the deletion Snackbar is active, **When** the timeout expires without tapping Undo, **Then** the in-memory snapshot is discarded and the deletion remains permanent without requiring background database purges.

---

### User Story 4 - Distinct Tactile Feedback on Segmented Control / Filter Selection (Priority: P4)

As a user switching tabs, views, or filtering options (e.g., segmented buttons in Calendar, Student details, or Financial views), I want a subtle, distinct feedback pulse on selection, so that the interface feels physically responsive and deliberate.

**Why this priority**: Tactile polish on state selection elevates overall perceived app quality and satisfies Material 3 interaction ergonomics.

**Independent Test**: Can be tested by tapping segmented buttons (such as Calendar view toggles or lesson filter selectors) and feeling the distinct tactile pulse on selection change.

**Acceptance Scenarios**:

1. **Given** an interactive segmented button or view selector, **When** the user selects a new segment, **Then** a distinct tactile feedback pulse is performed.
2. **Given** an interactive segmented button, **When** the user taps an already selected segment, **Then** redundant tactile feedback is suppressed.

---

### User Story 5 - Backup Data Export & Import Visual Feedback (Priority: P5)

As a tutor exporting or importing CSV backup files in Settings, I want explicit visual Snackbar notifications indicating success or descriptive failure, so that I know the exact outcome of data operations without blocking navigation.

**Why this priority**: Data export/import directly relates to data integrity and user peace of mind; explicit non-blocking visual feedback confirms completion or explains errors clearly.

**Independent Test**: Can be tested by triggering a CSV backup export or import in Settings and observing the resulting success or failure Snackbar message.

**Acceptance Scenarios**:

1. **Given** the user is in Settings/Backup, **When** a CSV export completes successfully, **Then** a Snackbar displays a success message confirming export completion.
2. **Given** an export or import operation fails (e.g., invalid file format or storage access issue), **Then** a Snackbar displays a clear failure message explaining the error.

---

### User Story 6 - Complete Trilingual Localization Parity (Priority: P6)

As a tutor using Tullab in Turkish, German, or English, I want all newly introduced feedback messages, status alerts, and undo actions to be accurately translated and grammatically natural in my selected language.

**Why this priority**: Tullab strictly requires complete linguistic parity across English, Turkish, and German per project constitution.

**Independent Test**: Can be tested by switching the device or app locale among English, Turkish, and German, performing payment/homework/deletion/backup actions, and verifying all displayed Snackbar texts and action buttons render the appropriate localized strings.

**Acceptance Scenarios**:

1. **Given** the app language is set to Turkish, **When** a payment is recorded, **Then** the Snackbar shows "[Tutar] tutarında ödeme kaydedildi" (or grammatical equivalent) with action "Geri Al".
2. **Given** the app language is set to German, **When** a payment is recorded, **Then** the Snackbar shows "Zahlung von [Betrag] erfasst" with action "Rückgängig".
3. **Given** the app language is set to English, **When** a payment is recorded, **Then** the Snackbar shows "Payment of [Amount] recorded" with action "Undo".

---

### Edge Cases

- **Rapid Consecutive Actions**: If a user marks multiple lessons as paid or completes several homework items in rapid succession, the newest action commits the prior pending undo and immediately displays the new action's Snackbar with its own "Undo", ensuring deterministic single-action undo behavior.
- **Screen Navigation During Active Snackbar**: When an action triggers a Snackbar with Undo on one screen (e.g., student detail) and the user navigates back to the dashboard/calendar, the visual feedback and undo capability MUST NOT cause navigation crashes or leak lifecycle references, persisting across routes via the root scaffold `SnackbarHost`.
- **Haptic Feedback on Unsupported Devices**: If a device lacks an advanced haptic motor or haptics are disabled in system settings, calls to tactile feedback must fail silently without exceptions or UI delay.
- **Undo After Entity Modification**: If an undo action is triggered after related state has changed, the restoration must maintain relational integrity without corrupted foreign keys.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST integrate tactile feedback using standard system haptic types via the Jetpack Compose environment across designated user actions.
- **FR-002**: The system MUST provide a confirmation tactile feedback when marking a lesson as paid.
- **FR-003**: The system MUST provide a crisp tactile click when toggling a homework item's completion status.
- **FR-004**: The system MUST provide a distinct tactile pulse when selecting an unselected option in segmented button groups.
- **FR-005**: The system MUST provide a subtle warning tactile feedback when confirming destructive actions (deleting lessons or homework).
- **FR-006**: The system MUST provide contextual visual feedback via a non-intrusive Material 3 notification banner (Snackbar) anchored to the root application scaffold (`TullabApp`), allowing notifications to survive screen transitions.
- **FR-007**: When marking a lesson as paid, the visual notification MUST display the formatted amount recorded and provide an interactive "Undo" action.
- **FR-008**: When toggling homework to completed, the visual notification MUST display a completion confirmation and provide an interactive "Undo" action.
- **FR-009**: When deleting a lesson or homework item, the system MUST immediately remove the record from the database while retaining an in-memory snapshot, and the visual notification MUST confirm deletion with an interactive "Undo" action.
- **FR-010**: Tapping "Undo" on any notification MUST immediately revert the underlying state change (reverting payment status, uncompleting homework, or re-inserting the in-memory snapshot into the database) and dismiss the notification.
- **FR-011**: The system MUST display visual notifications confirming the success or failure of CSV backup export and import operations.
- **FR-012**: All newly introduced visual feedback labels, formatted text templates, and action button titles MUST be localized with complete parity across English (`values`), Turkish (`values-tr`), and German (`values-de`).
- **FR-013**: Visual feedback notifications MUST NOT obstruct bottom navigation or core floating actions and MUST respect safe area insets.
- **FR-014**: If a new undoable action occurs while a prior Undo notification is active, the system MUST commit the previous action, dismiss its notification, and present the new action with its respective Undo option.

### Key Entities *(include if feature involves data)*

- **UndoableAction**: Represents a transient state-reversal payload holding the action identifier, target entity ID, prior state snapshot, and localized message description.
- **PaymentRecordFeedback**: Encapsulates the recorded lesson ID, payment amount, currency symbol, and the previous payment state.
- **ItemDeletionSnapshot**: Encapsulates the deleted entity (Lesson or Homework) data model to enable lossless in-memory restoration upon Undo.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of the specified interactions (marking paid, completing homework, segmented button selection, deleting items) trigger their respective tactile feedback without perceptible UI latency (< 16ms).
- **SC-002**: Users can revert an accidental payment record, homework completion, or deletion with a single tap on the "Undo" action within 4 to 5 seconds of the action.
- **SC-003**: 100% of visual feedback strings and undo action labels are externalized and localized across English, Turkish, and German, with zero hardcoded strings.
- **SC-004**: Screen navigation remains uninterrupted and responsive during active or expiring visual notifications across all primary screens (Dashboard, Calendar, Students, Homework, Settings), with notifications surviving transitions via the root `SnackbarHost`.

## Assumptions

- Devices without haptic motor hardware or with haptics disabled at the OS level will silently ignore tactile feedback calls without errors or performance penalty.
- The standard display duration for actionable Snackbars (containing an "Undo" action) is Material 3's `SnackbarDuration.Short` or `SnackbarDuration.Long` (approx. 4-5 seconds), providing adequate reaction time without lingering indefinitely.
- Deletions are executed immediately in the Room database, and clicking "Undo" restores the record by re-inserting the exact in-memory entity snapshot, avoiding complex soft-delete database migrations.
- Rapid consecutive undoable actions commit preceding actions, keeping at most one active reversible action in memory at a time.
- Visual formatters for currency and numbers already exist in the codebase and will be utilized to format payment amounts within feedback messages.
