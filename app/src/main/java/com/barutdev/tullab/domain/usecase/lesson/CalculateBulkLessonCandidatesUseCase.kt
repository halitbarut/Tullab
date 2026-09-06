package com.barutdev.tullab.domain.usecase.lesson

import com.barutdev.tullab.domain.model.BulkLessonCandidate
import com.barutdev.tullab.domain.model.BulkScheduleDraft
import com.barutdev.tullab.domain.model.BulkScheduleMode
import com.barutdev.tullab.domain.model.WeeklyRoutineEndCondition
import com.barutdev.tullab.domain.repository.LessonRepository
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

class CalculateBulkLessonCandidatesUseCase @Inject constructor(
    private val lessonRepository: LessonRepository
) {
    suspend operator fun invoke(
        draft: BulkScheduleDraft,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<BulkLessonCandidate> {
        val existingTimestamps = lessonRepository.getLessonDatesForStudent(draft.studentId).toSet()
        val today = LocalDate.now(zoneId)
        val candidates = mutableListOf<BulkLessonCandidate>()

        when (draft.mode) {
            BulkScheduleMode.CALENDAR_GRID -> {
                for (date in draft.selectedDates.sorted()) {
                    if (candidates.size >= 30) break
                    val time = draft.customDayTimes[date] ?: draft.defaultStartTime
                    candidates.add(createCandidate(date, time, zoneId, existingTimestamps, today))
                }
            }
            BulkScheduleMode.WEEKLY_ROUTINE -> {
                if (draft.selectedDaysOfWeek.isEmpty()) return emptyList()

                var currentDate = draft.routineStartDate
                val maxLimit = 365
                
                val limitCount = when (val condition = draft.endCondition) {
                    is WeeklyRoutineEndCondition.ByTargetCount -> minOf(condition.targetCount, maxLimit)
                    else -> maxLimit
                }

                while (candidates.size < limitCount) {
                    if (draft.endCondition is WeeklyRoutineEndCondition.ByEndDate && 
                        currentDate.isAfter(draft.endCondition.endDate)) {
                        break
                    }

                    if (currentDate.dayOfWeek in draft.selectedDaysOfWeek) {
                        val time = draft.customDayTimes[currentDate] ?: draft.defaultStartTime
                        candidates.add(createCandidate(currentDate, time, zoneId, existingTimestamps, today))
                    }
                    currentDate = currentDate.plusDays(1)
                }
            }
        }
        
        return candidates
    }

    private fun createCandidate(
        date: LocalDate, 
        time: LocalTime, 
        zoneId: ZoneId, 
        existingTimestamps: Set<Long>, 
        today: LocalDate
    ): BulkLessonCandidate {
        val epochMillis = ZonedDateTime.of(date, time, zoneId).toInstant().toEpochMilli()
        return BulkLessonCandidate(
            date = date,
            time = time,
            epochMillis = epochMillis,
            isConflict = existingTimestamps.contains(epochMillis),
            isPast = date.isBefore(today)
        )
    }
}
