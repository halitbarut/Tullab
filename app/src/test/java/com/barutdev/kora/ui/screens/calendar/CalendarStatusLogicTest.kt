package com.barutdev.kora.ui.screens.calendar

import com.barutdev.kora.domain.model.Homework
import com.barutdev.kora.domain.model.HomeworkStatus
import com.barutdev.kora.domain.model.Lesson
import com.barutdev.kora.domain.model.LessonStatus
import com.barutdev.kora.ui.theme.HomeworkGray
import com.barutdev.kora.ui.theme.HomeworkMagenta
import com.barutdev.kora.ui.theme.HomeworkTeal
import com.barutdev.kora.ui.theme.StatusBlue
import com.barutdev.kora.ui.theme.StatusGreen
import com.barutdev.kora.ui.theme.StatusOrange
import com.barutdev.kora.ui.theme.StatusRed
import com.barutdev.kora.ui.theme.StatusYellow
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [resolveDayIndicators] in CalendarStatusResolver.kt.
 *
 * These tests exercise the actual production function and verify both
 * lesson color priorities and homework color priorities.
 */
class CalendarStatusLogicTest {

    // Fixed dates for deterministic tests
    private val today = LocalDate.of(2026, 3, 5)
    private val futureDate = LocalDate.of(2026, 3, 10)
    private val pastDate = LocalDate.of(2026, 3, 1)

    // ----- Helpers to build domain objects with minimal boilerplate -----

    private fun lesson(status: LessonStatus) = Lesson(
        id = 0,
        studentId = 1,
        date = 0L,
        status = status,
        durationInHours = 1.0,
        notes = null
    )

    private fun homework(status: HomeworkStatus) = Homework(
        id = 0,
        studentId = 1,
        title = "Test",
        description = "",
        creationDate = 0L,
        dueDate = 0L,
        status = status,
        performanceNotes = null
    )

    // =====================================================================
    // Empty / null cases
    // =====================================================================

    @Test
    fun `returns DayIndicators with nulls when no lessons and no homework`() {
        val indicators = resolveDayIndicators(
            lessons = emptyList(),
            homework = emptyList(),
            date = futureDate,
            today = today
        )
        assertNull(indicators.lessonColor)
        assertNull(indicators.homeworkColor)
    }

    // =====================================================================
    // Lesson color priorities (RED > YELLOW > BLUE > GREEN)
    // =====================================================================

    @Test
    fun `lesson priority returns red for cancelled lesson`() {
        val indicators = resolveDayIndicators(
            lessons = listOf(lesson(LessonStatus.CANCELLED)),
            homework = emptyList(),
            date = futureDate,
            today = today
        )
        assertEquals(StatusRed, indicators.lessonColor)
    }

    @Test
    fun `lesson priority returns red for past due scheduled lesson`() {
        val indicators = resolveDayIndicators(
            lessons = listOf(lesson(LessonStatus.SCHEDULED)),
            homework = emptyList(),
            date = pastDate,
            today = today
        )
        assertEquals(StatusRed, indicators.lessonColor)
    }

    @Test
    fun `lesson priority returns yellow for completed lesson over green`() {
        val indicators = resolveDayIndicators(
            lessons = listOf(lesson(LessonStatus.COMPLETED), lesson(LessonStatus.PAID)),
            homework = emptyList(),
            date = futureDate,
            today = today
        )
        assertEquals(StatusYellow, indicators.lessonColor)
    }

    @Test
    fun `lesson priority returns blue for scheduled lesson in future`() {
        val indicators = resolveDayIndicators(
            lessons = listOf(lesson(LessonStatus.SCHEDULED)),
            homework = emptyList(),
            date = futureDate,
            today = today
        )
        assertEquals(StatusBlue, indicators.lessonColor)
    }

    @Test
    fun `lesson priority returns green for paid lesson`() {
        val indicators = resolveDayIndicators(
            lessons = listOf(lesson(LessonStatus.PAID)),
            homework = emptyList(),
            date = futureDate,
            today = today
        )
        assertEquals(StatusGreen, indicators.lessonColor)
    }

    // =====================================================================
    // Homework color priorities (MAGENTA > GRAY > ORANGE > TEAL)
    // =====================================================================

    @Test
    fun `homework priority returns magenta for overdue homework`() {
        val indicators = resolveDayIndicators(
            lessons = emptyList(),
            homework = listOf(homework(HomeworkStatus.OVERDUE)),
            date = futureDate,
            today = today
        )
        assertEquals(HomeworkMagenta, indicators.homeworkColor)
    }

    @Test
    fun `homework priority returns magenta for past due pending homework`() {
        // Pending homework with a past due date should be treated as Overdue
        val indicators = resolveDayIndicators(
            lessons = emptyList(),
            homework = listOf(homework(HomeworkStatus.PENDING)),
            date = pastDate,
            today = today
        )
        assertEquals(HomeworkMagenta, indicators.homeworkColor)
    }

    @Test
    fun `homework priority returns gray for cancelled homework`() {
        val indicators = resolveDayIndicators(
            lessons = emptyList(),
            homework = listOf(
                homework(HomeworkStatus.CANCELLED),
                homework(HomeworkStatus.COMPLETED)
            ),
            date = futureDate,
            today = today
        )
        assertEquals(HomeworkGray, indicators.homeworkColor)
    }

    @Test
    fun `homework priority returns orange for pending homework in future`() {
        val indicators = resolveDayIndicators(
            lessons = emptyList(),
            homework = listOf(
                homework(HomeworkStatus.PENDING),
                homework(HomeworkStatus.COMPLETED)
            ),
            date = futureDate,
            today = today
        )
        assertEquals(StatusOrange, indicators.homeworkColor)
    }

    @Test
    fun `homework priority returns teal for completed homework`() {
        val indicators = resolveDayIndicators(
            lessons = emptyList(),
            homework = listOf(homework(HomeworkStatus.COMPLETED)),
            date = futureDate,
            today = today
        )
        assertEquals(HomeworkTeal, indicators.homeworkColor)
    }

    // =====================================================================
    // Dual-dot scenarios
    // =====================================================================

    @Test
    fun `resolves day indicators independently for both lessons and homework`() {
        val indicators = resolveDayIndicators(
            lessons = listOf(lesson(LessonStatus.COMPLETED)), // Should be YELLOW
            homework = listOf(homework(HomeworkStatus.PENDING)), // Should be ORANGE (future date)
            date = futureDate,
            today = today
        )
        
        assertEquals(StatusYellow, indicators.lessonColor)
        assertEquals(StatusOrange, indicators.homeworkColor)
    }
}
