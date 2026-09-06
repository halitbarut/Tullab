# Contract: UI Navigation & ViewModel State

**Feature**: `008-bulk-lesson-scheduling`  
**Package**: `com.barutdev.tullab.ui.screens.bulk_schedule`  

## 1. Navigation Route

```kotlin
// TullabDestinations.kt
object BulkSchedule : StudentScoped(
    baseRoute = "bulk_schedule",
    labelRes = R.string.bulk_schedule_title
)
```

In `TullabNavGraph.kt`:
```kotlin
composable(
    route = TullabDestination.BulkSchedule.route,
    arguments = TullabDestination.BulkSchedule.arguments()
) { backStackEntry ->
    val studentId = backStackEntry.requireStudentId()
    BulkScheduleScreen(
        studentId = studentId,
        onNavigateBack = { navController.popBackStack() },
        onBatchCreated = { createdCount, skippedCount, lessonIds ->
            navController.popBackStack()
            // Forward to CalendarScreen or emit to TullabScaffoldController
        }
    )
}
```

---

## 2. ViewModel Contract (`BulkScheduleViewModel.kt`)

### UI State Model
```kotlin
data class BulkScheduleUiState(
    val studentName: String = "",
    val studentDefaultHourlyRate: Double = 0.0,
    val currencyCode: String = "USD",
    val draft: BulkScheduleDraft = BulkScheduleDraft(studentId = 0),
    val candidates: List<BulkLessonCandidate> = emptyList(),
    val isEvaluating: Boolean = false,
    val isSaving: Boolean = false,
    val showPastDateWarningDialog: Boolean = false,
    val errorMessageRes: Int? = null
) {
    val totalCandidateCount: Int get() = candidates.size
    val validCount: Int get() = candidates.count { !it.isConflict }
    val skippedCount: Int get() = candidates.count { it.isConflict }
    val hasPastLessons: Boolean get() = candidates.any { it.isPast && !it.isConflict }
    val isExceedingLimit: Boolean get() = totalCandidateCount > 30
    val canConfirm: Boolean get() = validCount in 1..30 && !isSaving && !isExceedingLimit
}
```

### UI Events (`Channel<BulkScheduleUiEvent>`)
```kotlin
sealed interface BulkScheduleUiEvent {
    data class BatchCreated(val createdCount: Int, val skippedCount: Int, val lessonIds: List<Int>) : BulkScheduleUiEvent
    data class ShowMessage(val messageRes: Int) : BulkScheduleUiEvent
}
```

### User Actions (ViewModel Methods)
```kotlin
fun onSelectMode(mode: BulkScheduleMode)
fun onToggleDate(date: LocalDate)
fun onToggleWeekday(dayOfWeek: DayOfWeek)
fun onSetRoutineStartDate(date: LocalDate)
fun onSetEndCondition(condition: WeeklyRoutineEndCondition)
fun onSetDefaultStartTime(time: LocalTime)
fun onSetCustomDayTime(date: LocalDate, time: LocalTime)
fun onToggleCustomRate(enabled: Boolean)
fun onCustomRateChanged(rateInput: String)
fun onConfirmScheduleClicked()
fun onDismissPastDateWarning()
fun onConfirmPastDateWarning()
```

---

## 3. Undo Snackbar Coordination in `CalendarScreen`

When `CalendarScreen` observes a `BatchCreated` result:
1. It shows a Snackbar with text:
   - If `skippedCount > 0`: `pluralStringResource(R.plurals.bulk_schedule_created_with_skipped, createdCount, createdCount, skippedCount)`
   - Else: `pluralStringResource(R.plurals.bulk_schedule_created_success, createdCount, createdCount)`
2. Action label: `stringResource(R.string.bulk_schedule_undo_action)` ("Undo")
3. If user taps "Undo":
   - Calls `calendarViewModel.undoBulkLessons(lessonIds)`
   - Deletes newly created lessons via `UndoBulkLessonsUseCase`
   - Shows brief confirmation toast / snackbar: *"Batch scheduling undone."*
