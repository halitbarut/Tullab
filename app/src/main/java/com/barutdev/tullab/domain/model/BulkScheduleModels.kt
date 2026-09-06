package com.barutdev.tullab.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

enum class BulkScheduleMode {
    CALENDAR_GRID,
    WEEKLY_ROUTINE
}

sealed interface WeeklyRoutineEndCondition {
    data class ByEndDate(val endDate: LocalDate) : WeeklyRoutineEndCondition
    data class ByTargetCount(val targetCount: Int) : WeeklyRoutineEndCondition
}

data class BulkLessonCandidate(
    val date: LocalDate,
    val time: LocalTime,
    val epochMillis: Long,
    val isConflict: Boolean = false,
    val isPast: Boolean = false
)

data class BulkScheduleDraft(
    val studentId: Int,
    val mode: BulkScheduleMode = BulkScheduleMode.CALENDAR_GRID,
    val selectedDates: Set<LocalDate> = emptySet(),
    val selectedDaysOfWeek: Set<DayOfWeek> = emptySet(),
    val routineStartDate: LocalDate = LocalDate.now(),
    val endCondition: WeeklyRoutineEndCondition = WeeklyRoutineEndCondition.ByTargetCount(4),
    val defaultStartTime: LocalTime = LocalTime.of(15, 0),
    val customDayTimes: Map<LocalDate, LocalTime> = emptyMap(),
    val useCustomRate: Boolean = false,
    val customRate: Double? = null
) {
    val effectiveRate: (studentDefaultRate: Double) -> Double = { studentDefaultRate ->
        if (useCustomRate && customRate != null && customRate >= 0.0) customRate else studentDefaultRate
    }
}

data class BulkScheduleResult(
    val studentId: Int,
    val createdLessons: List<Lesson>,
    val skippedCount: Int,
    val hasPastLessons: Boolean
) {
    val createdCount: Int get() = createdLessons.size
    val createdLessonIds: List<Int> get() = createdLessons.map { it.id }
}

data class BatchUndoSession(
    val studentId: Int,
    val lessonIds: List<Int>,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)
