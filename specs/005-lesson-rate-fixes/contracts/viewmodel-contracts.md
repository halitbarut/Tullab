# ViewModel Contracts: Profile & Calendar

## 1. EditStudentProfileViewModel

### Package
`com.barutdev.kora.ui.screens.student_profile`

### UiState Additions
```kotlin
data class StudentProfileUiState(
    // existing fields...
    val isScheduledLessonsPromptVisible: Boolean = false,
    val scheduledLessonsCount: Int = 0,
    val scheduledLessonsScope: ScheduledLessonsScope = ScheduledLessonsScope.FUTURE_ONLY,
    val pendingProfileUpdate: StudentProfileUpdate? = null
)
```

### Flow / Behavior
- When `onSave()` detects hourly rate change:
  - Fetches student's current lessons:
    ```kotlin
    val studentLessons = lessonRepository.getLessonsForStudent(studentId).first()
    val scheduledLessons = studentLessons.filter { it.status == LessonStatus.SCHEDULED }
    ```
  - If `scheduledLessons.isEmpty()`: saves immediately via `executeSaveProfile(update, updateScheduledLessons = false)`.
  - If `scheduledLessons.isNotEmpty()`:
    - Analyzes dates against `LocalDate.now(ZoneId.systemDefault())`.
    - Determines `ScheduledLessonsScope` (`PAST_ONLY`, `FUTURE_ONLY`, or `MIXED`).
    - Emits UI state with `isScheduledLessonsPromptVisible = true`, `scheduledLessonsScope`, and `scheduledLessonsCount`.
- When tutor responds:
  - `onConfirmScheduledLessonsRateUpdate(updateScheduled = true)`: Saves profile update AND updates scheduled lessons.
  - `onConfirmScheduledLessonsRateUpdate(updateScheduled = false)`: Saves profile update ONLY, leaving scheduled lessons untouched.

---

## 2. CalendarViewModel

### Package
`com.barutdev.kora.ui.screens.calendar`

### Method Additions
```kotlin
fun onSaveScheduledLesson(
    lessonId: Int,
    duration: String,
    notes: String,
    pricingMode: PricingMode,
    rateOrFeeInput: String
)
```

### Flow / Behavior
- Parses input duration and rate/fee.
- Finds target lesson by `lessonId`.
- Updates the lesson via `lessonRepository.updateLesson`:
  - `status` MUST remain `LessonStatus.SCHEDULED`.
  - `pricingMode` updated to selected mode.
  - `rateOrFee` updated to parsed rate/fee.
  - `notes` updated to parsed notes.
  - `durationInHours` updated for `PER_HOUR` or null for `FLAT_FEE`.
- Dismisses dialog and clears `selectedLessonForLogging`.
