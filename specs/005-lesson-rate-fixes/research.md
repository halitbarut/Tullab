# Research: Lesson Rate Management, Multi-Rate Payment Breakdown, and Calendar Rate Editing

## Decision 1: Rate Update Dialog Timing Detection and In-App Localization

### Context
When a tutor changes a student's profile hourly rate on September 4 while having an uncompleted scheduled lesson dated September 1, the app presents a dialog saying "This student has future scheduled lessons". Furthermore, the dialog displays in Turkish even when the app language is set to English.

### Decision
1. **Timing Awareness**: Query the student's uncompleted scheduled lessons and evaluate their dates against `LocalDate.now(ZoneId.systemDefault())`.
   - Categorize into `ScheduledLessonsScope`:
     - `PAST_ONLY`: Scheduled lessons exist strictly before today.
     - `FUTURE_ONLY`: Scheduled lessons exist strictly today or in the future.
     - `MIXED`: Scheduled lessons exist both in the past and in the future.
   - Supply localized strings dynamically:
     - `PAST_ONLY`: References existing past/uncompleted lessons awaiting logging.
     - `FUTURE_ONLY`: References upcoming scheduled lessons.
     - `MIXED`: References all existing uncompleted lessons.
2. **Localization Resolution**: Replace all `androidx.compose.ui.res.stringResource` calls in `ScheduledLessonsRatePromptDialog.kt` with `com.barutdev.kora.util.koraStringResource`.
   - `koraStringResource` explicitly builds a localized configuration context based on `LocalLocale.current`, ensuring the dialog respects the in-app language preference (`en`, `tr`, `de`).

### Rationale
- Standard `stringResource` in Android Jetpack Compose reads the top-level Android system configuration. When the system language is Turkish, standard Compose dialogs fall back to Turkish unless wrapped with `koraStringResource`.
- Differentiating between past and future lessons provides factual accuracy to the tutor and avoids alarming statements about non-existent future lessons.

### Alternatives Considered
- *Single generic message ("This student has scheduled lessons")*: Rejected because it lacks clarity on whether past unpaid lessons will be retroactively affected.
- *Overriding `AppCompatDelegate.setApplicationLocales` globally*: Rejected because Kora's established architecture uses `ProvideLocale` and `koraStringResource` for Compose screens without requiring activity recreate or platform-level locale manipulation.

---

## Decision 2: Multi-Rate Payment Cycle Breakdown Aggregation and Layout

### Context
When a payment cycle contains completed lessons billed at different hourly rates (for example, 5 hours at 200 TL and 2 hours at 400 TL), the Dashboard displays the correct total (1,800 TL) but formats the subtitle breakdown as `total duration × current profile rate` (e.g., `7 hours × 400 TL`), which mathematically equals 2,800 TL and confuses tutors.

### Decision
1. **Aggregated Tier Model**: Define `PaymentBreakdownTier`:
   ```kotlin
   data class PaymentBreakdownTier(
       val pricingMode: PricingMode,
       val rateOrFee: Double,
       val totalHours: Double,
       val lessonCount: Int,
       val subtotal: Double
   )
   ```
2. **Deterministic Grouping**:
   - In `DashboardViewModel`, group `completedLessonsAwaitingPayment` by `(pricingMode, rateOrFee)`.
   - For `PER_HOUR`: Sum `durationInHours` to compute `totalHours`; `subtotal = totalHours * rateOrFee`.
   - For `FLAT_FEE`: `subtotal = rateOrFee * lessonCount`.
3. **Responsive Two-Column Row Presentation**:
   - Render each tier in a dedicated row within `PaymentTrackingCard`:
     - Left side: Calculation formula (e.g., `5 hours × 200 TL` or `2 lessons × 500 TL (Flat Fee)`).
     - Right side: Calculated subtotal (e.g., `1,000 TL`).
   - Use flexible layout constraints (e.g., `Row` with left weight `1f` and right alignment, or `FlowRow`/adaptive wrapping) so that long currency strings or narrow phone screens wrap without truncation or clipping.
   - If the cycle contains only a single tier, display that single breakdown row.

### Rationale
- Grouping by rate tier guarantees that `Σ tier.subtotal == totalAmountDue` with 100% mathematical precision.
- Grouping lessons by rate tier rather than listing every single lesson prevents UI clutter when a student has many lessons at the same rate.
- Two-column presentation with subtotal provides immediate auditability for tutors.

### Alternatives Considered
- *Formula only without subtotals (`5 hours × 200 TL, 2 hours × 400 TL` in one paragraph)*: Rejected based on user clarification preferring two-column alignment with subtotals and responsive wrapping.
- *Listing each individual lesson*: Rejected because displaying 15 separate line items in the summary card would degrade card usability.

---

## Decision 3: Calendar Future Planned Lesson Edit & Save Workflow

### Context
In `CalendarScreen`, clicking "Edit" on a future planned lesson opens `LogLessonDialog`, which has a disabled "Complete Lesson" button (because a future lesson cannot be completed before its date), a "Mark as Not Done" button (which cancels the lesson), and a "Cancel" button (which discards inputs). There is no action to save modifications to the scheduled lesson's rate, mode, or notes.

### Decision
1. **Contextual Dialog Mode**:
   - In `LogLessonDialog`, detect when the lesson is an upcoming scheduled lesson (`lesson.status == LessonStatus.SCHEDULED && !lessonDate.isBefore(today)`).
   - In this mode:
     - Remove the disabled "Complete Lesson" button.
     - Add a primary **"Save Changes"** action (`R.string.calendar_lesson_action_save_changes`).
     - Retain **"Mark as Not Done"** (`R.string.dashboard_log_lesson_mark_not_done_button`) to cancel the lesson.
     - Retain **"Cancel"** (`R.string.dialog_action_cancel`) to dismiss without saving.
2. **ViewModel Save Handler**:
   - In `CalendarViewModel`, implement `onSaveScheduledLesson(lessonId: Int, duration: String?, notes: String?, pricingMode: PricingMode, rateOrFeeInput: String)`:
     - Parses inputs.
     - Updates the lesson via `lessonRepository.updateLesson(updatedLesson)`.
     - Ensures `status` strictly remains `LessonStatus.SCHEDULED`.
     - Closes the dialog and cleans up selection state.

### Rationale
- Directly implements User Clarification Option C.
- Eliminates tutor confusion caused by seeing a disabled primary action with no way to save edited rates.
- Reuses `LogLessonDialog` with a clean conditional action row, preserving existing styling, validation, and error states without code duplication.

### Alternatives Considered
- *Separate `EditScheduledLessonDialog` composable*: Rejected because `LogLessonDialog` already contains the required duration, notes, pricing mode toggle, and rate input fields; branching its bottom action buttons based on status/date is much simpler and maintains visual consistency.
