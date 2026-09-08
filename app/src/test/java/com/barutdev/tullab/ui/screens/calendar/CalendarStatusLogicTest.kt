package com.barutdev.tullab.ui.screens.calendar

import com.barutdev.tullab.domain.model.Homework
import com.barutdev.tullab.domain.model.HomeworkStatus
import com.barutdev.tullab.domain.model.Lesson
import com.barutdev.tullab.domain.model.LessonStatus
import com.barutdev.tullab.ui.theme.HomeworkGray
import com.barutdev.tullab.ui.theme.StatusBlue
import com.barutdev.tullab.ui.theme.StatusGreen
import com.barutdev.tullab.ui.theme.StatusRed
import com.barutdev.tullab.ui.theme.StatusYellow
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [resolveDayIndicators] in CalendarStatusResolver.kt.
 *
 * Verifies the unified 5-color semantic palette across both lessons and homework:
 * - Blue: Future/Scheduled lesson OR Scheduled homework
 * - Red: Past unlogged lesson OR Overdue homework (action required)
 * - Yellow: Lesson completed, pending payment
 * - Green: Lesson paid OR Homework completed
 * - Neutral Gray: Cancelled lesson OR Cancelled homework
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
        notes = null,
        pricingMode = com.barutdev.tullab.domain.model.PricingMode.PER_HOUR,
        rateOrFee = 0.0
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
    // Lesson color priorities (RED > YELLOW > BLUE > GREEN > GRAY)
    // =====================================================================

    @Test
    fun `lesson priority returns gray for cancelled lesson`() {
        val indicators = resolveDayIndicators(
            lessons = listOf(lesson(LessonStatus.CANCELLED)),
            homework = emptyList(),
            date = futureDate,
            today = today
        )
        assertEquals(HomeworkGray, indicators.lessonColor)
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
    // Homework color priorities (RED > BLUE > GREEN > GRAY)
    // =====================================================================


    @Test
    fun `homework priority returns red for past due pending homework`() {
        // Pending homework with a past due date should be treated as Overdue
        val indicators = resolveDayIndicators(
            lessons = emptyList(),
            homework = listOf(homework(HomeworkStatus.PENDING)),
            date = pastDate,
            today = today
        )
        assertEquals(StatusRed, indicators.homeworkColor)
    }

    @Test
    fun `homework priority returns blue for pending homework in future`() {
        val indicators = resolveDayIndicators(
            lessons = emptyList(),
            homework = listOf(
                homework(HomeworkStatus.PENDING),
                homework(HomeworkStatus.COMPLETED)
            ),
            date = futureDate,
            today = today
        )
        assertEquals(StatusBlue, indicators.homeworkColor)
    }

    @Test
    fun `homework priority returns green for completed homework`() {
        val indicators = resolveDayIndicators(
            lessons = emptyList(),
            homework = listOf(
                homework(HomeworkStatus.COMPLETED),
                homework(HomeworkStatus.CANCELLED)
            ),
            date = futureDate,
            today = today
        )
        assertEquals(StatusGreen, indicators.homeworkColor)
    }

    @Test
    fun `homework priority returns gray for cancelled homework`() {
        val indicators = resolveDayIndicators(
            lessons = emptyList(),
            homework = listOf(homework(HomeworkStatus.CANCELLED)),
            date = futureDate,
            today = today
        )
        assertEquals(HomeworkGray, indicators.homeworkColor)
    }

    // =====================================================================
    // Dual-dot scenarios
    // =====================================================================

    @Test
    fun `resolves day indicators independently for both lessons and homework`() {
        val indicators = resolveDayIndicators(
            lessons = listOf(lesson(LessonStatus.COMPLETED)), // Should be YELLOW
            homework = listOf(homework(HomeworkStatus.PENDING)), // Should be BLUE (future date)
            date = futureDate,
            today = today
        )
        
        assertEquals(StatusYellow, indicators.lessonColor)
        assertEquals(StatusBlue, indicators.homeworkColor)
    }

    @Test
    fun `resolves both cancelled lesson and cancelled homework to gray`() {
        val indicators = resolveDayIndicators(
            lessons = listOf(lesson(LessonStatus.CANCELLED)),
            homework = listOf(homework(HomeworkStatus.CANCELLED)),
            date = futureDate,
            today = today
        )

        assertEquals(HomeworkGray, indicators.lessonColor)
        assertEquals(HomeworkGray, indicators.homeworkColor)
    }

    @Test
    fun `resolves both past-due lesson and overdue homework to red`() {
        val indicators = resolveDayIndicators(
            lessons = listOf(lesson(LessonStatus.SCHEDULED)), // pastDate -> RED
            homework = listOf(homework(HomeworkStatus.PENDING)), // -> RED
            date = pastDate,
            today = today
        )

        assertEquals(StatusRed, indicators.lessonColor)
        assertEquals(StatusRed, indicators.homeworkColor)
    }

    @Test
    fun `homework pending evaluates overdue based on homeworkToday UTC boundary`() {
        val date = LocalDate.of(2026, 3, 5)
        val localToday = LocalDate.of(2026, 3, 6)
        val utcToday = LocalDate.of(2026, 3, 5)

        // When evaluated with local today (Mar 6), date (Mar 5) would appear overdue
        val indicatorsLocal = resolveDayIndicators(
            lessons = emptyList(),
            homework = listOf(homework(HomeworkStatus.PENDING)),
            date = date,
            today = localToday,
            homeworkToday = localToday
        )
        assertEquals(StatusRed, indicatorsLocal.homeworkColor)

        // When evaluated with UTC today (Mar 5), date (Mar 5) is NOT before utcToday -> BLUE_SCHEDULED
        val indicatorsUtc = resolveDayIndicators(
            lessons = emptyList(),
            homework = listOf(homework(HomeworkStatus.PENDING)),
            date = date,
            today = localToday,
            homeworkToday = utcToday
        )
        assertEquals(StatusBlue, indicatorsUtc.homeworkColor)
    }
}

