# Contract: Bulk Schedule Domain Use Cases

**Feature**: `008-bulk-lesson-scheduling`  
**Package**: `com.barutdev.tullab.domain.usecase.lesson`  

## 1. `CalculateBulkLessonCandidatesUseCase`

Evaluates candidate slots, detects collisions with existing student lessons, and computes validation statuses.

```kotlin
class CalculateBulkLessonCandidatesUseCase @Inject constructor(
    private val lessonRepository: LessonRepository
) {
    /**
     * Generates a preview list of candidates with conflicts flagged.
     *
     * @param draft Current bulk schedule configuration.
     * @param zoneId Timezone used for date-time calculations.
     * @return List of BulkLessonCandidate objects sorted chronologically.
     */
    suspend operator fun invoke(
        draft: BulkScheduleDraft,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<BulkLessonCandidate>
}
```

### Generation Logic:
1. Fetch `existingTimestamps = lessonRepository.getLessonDatesForStudent(draft.studentId).toSet()`.
2. Determine raw candidate pairs `(LocalDate, LocalTime)`:
   - For `CALENDAR_GRID`: Iterate over `draft.selectedDates.sorted()`. Use `draft.customDayTimes[date] ?: draft.defaultStartTime`.
   - For `WEEKLY_ROUTINE`:
     - If `draft.selectedDaysOfWeek.isEmpty()`, return empty.
     - Start from `draft.routineStartDate`. Iterate dates step-by-step matching `date.dayOfWeek in draft.selectedDaysOfWeek`.
     - Stop condition:
       - `ByEndDate(endDate)`: While `currentDate <= endDate`.
       - `ByTargetCount(targetCount)`: While candidates collected $< \min(targetCount, 30)$.
     - Use `draft.customDayTimes[date] ?: draft.defaultStartTime`.
3. Map each raw slot to `BulkLessonCandidate`:
   - `epochMillis = ZonedDateTime.of(date, time, zoneId).toInstant().toEpochMilli()`
   - `isConflict = epochMillis in existingTimestamps`
   - `isPast = date.isBefore(LocalDate.now(zoneId))`

---

## 2. `CreateBulkLessonsUseCase`

Persists non-conflicting candidates as independent lessons and returns the execution result.

```kotlin
class CreateBulkLessonsUseCase @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val studentRepository: StudentRepository
) {
    /**
     * Persists valid candidates. Conflicting slots are skipped.
     *
     * @param draft The validated draft.
     * @param candidates Pre-evaluated candidate list.
     * @return BulkScheduleResult containing created lesson IDs and skipped count.
     */
    suspend operator fun invoke(
        draft: BulkScheduleDraft,
        candidates: List<BulkLessonCandidate>
    ): Result<BulkScheduleResult>
}
```

---

## 3. `UndoBulkLessonsUseCase`

Executes single-tap rollback by deleting the batch lessons by ID.

```kotlin
class UndoBulkLessonsUseCase @Inject constructor(
    private val lessonRepository: LessonRepository
) {
    /**
     * Deletes the specified created lessons.
     *
     * @param lessonIds The list of IDs of lessons created in the batch.
     */
    suspend operator fun invoke(lessonIds: List<Int>): Result<Unit>
}
```
