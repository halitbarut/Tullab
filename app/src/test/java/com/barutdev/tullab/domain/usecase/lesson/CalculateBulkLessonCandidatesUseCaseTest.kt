package com.barutdev.tullab.domain.usecase.lesson

import com.barutdev.tullab.domain.model.BulkScheduleDraft
import com.barutdev.tullab.domain.model.BulkScheduleMode
import com.barutdev.tullab.domain.model.WeeklyRoutineEndCondition
import com.barutdev.tullab.domain.repository.LessonRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class CalculateBulkLessonCandidatesUseCaseTest {

    private lateinit var lessonRepository: LessonRepository
    private lateinit var useCase: CalculateBulkLessonCandidatesUseCase
    private val zoneId = ZoneId.systemDefault()

    @Before
    fun setup() {
        lessonRepository = mockk()
        useCase = CalculateBulkLessonCandidatesUseCase(lessonRepository)
    }

    @Test
    fun `calendar grid mode calculates candidates correctly`() = runTest {
        val today = LocalDate.now(zoneId)
        val testDates = setOf(today, today.plusDays(1))
        
        coEvery { lessonRepository.getLessonDatesForStudent(1) } returns emptyList()

        val draft = BulkScheduleDraft(
            studentId = 1,
            mode = BulkScheduleMode.CALENDAR_GRID,
            selectedDates = testDates,
            defaultStartTime = LocalTime.of(15, 0)
        )

        val candidates = useCase(draft, zoneId)

        assertEquals(2, candidates.size)
        assertEquals(today, candidates[0].date)
        assertEquals(LocalTime.of(15, 0), candidates[0].time)
        assertEquals(today.plusDays(1), candidates[1].date)
        assertFalse(candidates[0].isConflict)
        assertFalse(candidates[0].isPast)
    }

    @Test
    fun `weekly routine by target count calculates correct number of candidates`() = runTest {
        val today = LocalDate.now(zoneId)
        
        coEvery { lessonRepository.getLessonDatesForStudent(1) } returns emptyList()

        val draft = BulkScheduleDraft(
            studentId = 1,
            mode = BulkScheduleMode.WEEKLY_ROUTINE,
            selectedDaysOfWeek = setOf(today.dayOfWeek),
            routineStartDate = today,
            endCondition = WeeklyRoutineEndCondition.ByTargetCount(4),
            defaultStartTime = LocalTime.of(15, 0)
        )

        val candidates = useCase(draft, zoneId)

        assertEquals(4, candidates.size)
        assertEquals(today, candidates[0].date)
        assertEquals(today.plusDays(7), candidates[1].date)
    }

    @Test
    fun `weekly routine by end date calculates correct candidates`() = runTest {
        val today = LocalDate.now(zoneId)
        val endDate = today.plusDays(15)
        
        coEvery { lessonRepository.getLessonDatesForStudent(1) } returns emptyList()

        val draft = BulkScheduleDraft(
            studentId = 1,
            mode = BulkScheduleMode.WEEKLY_ROUTINE,
            selectedDaysOfWeek = setOf(today.dayOfWeek),
            routineStartDate = today,
            endCondition = WeeklyRoutineEndCondition.ByEndDate(endDate),
            defaultStartTime = LocalTime.of(15, 0)
        )

        val candidates = useCase(draft, zoneId)

        assertEquals(3, candidates.size)
        assertEquals(today, candidates[0].date)
        assertEquals(today.plusDays(7), candidates[1].date)
        assertEquals(today.plusDays(14), candidates[2].date)
    }

    @Test
    fun `identifies conflict correctly`() = runTest {
        val today = LocalDate.now(zoneId)
        val time = LocalTime.of(15, 0)
        val epochMillis = ZonedDateTime.of(today, time, zoneId).toInstant().toEpochMilli()
        
        coEvery { lessonRepository.getLessonDatesForStudent(1) } returns listOf(epochMillis)

        val draft = BulkScheduleDraft(
            studentId = 1,
            mode = BulkScheduleMode.CALENDAR_GRID,
            selectedDates = setOf(today),
            defaultStartTime = time
        )

        val candidates = useCase(draft, zoneId)

        assertEquals(1, candidates.size)
        assertTrue(candidates[0].isConflict)
    }

    @Test
    fun `enforces hard stop at 100 generated candidates in calendar mode`() = runTest {
        val today = LocalDate.now(zoneId)
        val testDates = (1..105).map { today.plusDays(it.toLong()) }.toSet()
        
        coEvery { lessonRepository.getLessonDatesForStudent(1) } returns emptyList()

        val draft = BulkScheduleDraft(
            studentId = 1,
            mode = BulkScheduleMode.CALENDAR_GRID,
            selectedDates = testDates,
            defaultStartTime = LocalTime.of(15, 0)
        )

        val candidates = useCase(draft, zoneId)

        assertEquals(100, candidates.size)
    }
}
