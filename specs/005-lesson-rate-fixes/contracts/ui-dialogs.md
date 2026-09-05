# UI Contracts: Dialogs

## 1. ScheduledLessonsRatePromptDialog

### Component
`com.barutdev.kora.ui.screens.student_profile.components.ScheduledLessonsRatePromptDialog`

### Parameters
```kotlin
@Composable
fun ScheduledLessonsRatePromptDialog(
    scheduledLessonsCount: Int,
    scope: ScheduledLessonsScope,
    onConfirm: (updateScheduled: Boolean) -> Unit,
    onDismiss: () -> Unit
)
```

### Behavioral Contract
- **Localization**: MUST resolve all strings via `koraStringResource` using `LocalLocale.current`.
- **Text Adaptation**:
  - `scope == PAST_ONLY`:
    - Title: `R.string.rate_change_dialog_title_past` ("Update Past Uncompleted Lessons?")
    - Message: `R.string.rate_change_dialog_message_past` ("This student has uncompleted lessons from earlier dates. Should the new rate apply to them?")
    - Confirm: `R.string.rate_change_dialog_update_past` ("Update Past Lessons")
    - Dismiss: `R.string.rate_change_dialog_keep_original` ("Keep Original Rate")
  - `scope == FUTURE_ONLY`:
    - Title: `R.string.rate_change_dialog_title_future` ("Update Scheduled Lessons?")
    - Message: `R.string.rate_change_dialog_message_future` ("This student has upcoming scheduled lessons. Should the new rate apply to them?")
    - Confirm: `R.string.rate_change_dialog_update_future` ("Update Future Lessons")
    - Dismiss: `R.string.rate_change_dialog_keep_original` ("Keep Original Rate")
  - `scope == MIXED`:
    - Title: `R.string.rate_change_dialog_title_mixed` ("Update Existing Lessons?")
    - Message: `R.string.rate_change_dialog_message_mixed` ("This student has both past and upcoming uncompleted lessons. Should the new rate apply to them?")
    - Confirm: `R.string.rate_change_dialog_update_mixed` ("Update All Lessons")
    - Dismiss: `R.string.rate_change_dialog_keep_original` ("Keep Original Rate")

---

## 2. LogLessonDialog Contextual Action Contract

### Component
`com.barutdev.kora.ui.screens.dashboard.components.LogLessonDialog`

### New / Modified Parameters
```kotlin
@Composable
fun LogLessonDialog(
    showDialog: Boolean,
    lesson: Lesson?,
    onDismiss: () -> Unit,
    onComplete: (duration: String, notes: String, pricingMode: PricingMode, rateOrFee: String) -> Unit,
    onMarkNotDone: (notes: String) -> Unit,
    onSaveScheduled: ((duration: String, notes: String, pricingMode: PricingMode, rateOrFee: String) -> Unit)? = null,
    isMarkAsPaidMode: Boolean = false,
    requiresFeePrompt: Boolean = false,
    onMarkAsPaid: ((duration: String, customFee: String) -> Unit)? = null
)
```

### Action Button Presentation Rules
1. **Past Scheduled Lesson requiring logging** (`lesson.status == SCHEDULED && lessonDate.isBefore(today)`):
   - Confirm button: "Mark as Completed" (`onComplete(...)`).
   - Dismiss button 1: "Mark as Not Done" (`onMarkNotDone(...)`).
   - Dismiss button 2: "Cancel" (`onDismiss()`).
2. **Future Scheduled Lesson being edited** (`lesson.status == SCHEDULED && !lessonDate.isBefore(today)`):
   - Disabled "Complete Lesson" button is **REMOVED**.
   - Primary confirm button: "Save Changes" (`onSaveScheduled?.invoke(...)`).
   - Dismiss button 1: "Mark as Not Done" (`onMarkNotDone(...)`).
   - Dismiss button 2: "Cancel" (`onDismiss()`).
3. **Mark as Paid Mode**:
   - Confirm button: "Mark as Paid" (`onMarkAsPaid(...)`).
   - Dismiss button: "Cancel" (`onDismiss()`).
