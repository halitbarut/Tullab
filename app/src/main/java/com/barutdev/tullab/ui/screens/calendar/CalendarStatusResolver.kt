package com.barutdev.tullab.ui.screens.calendar

import androidx.compose.ui.graphics.Color
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

data class DayIndicators(
    val lessonColor: Color?,
    val homeworkColor: Color?
)

internal enum class InternalLessonStatus { RED_PAST_DUE, YELLOW, BLUE, GREEN, GRAY_CANCELLED }
internal enum class InternalHomeworkStatus { RED_OVERDUE, BLUE_SCHEDULED, GREEN_COMPLETED, GRAY_CANCELLED }

/**
 * Determines the indicator colors for a calendar day based on the statuses
 * of all lessons and homework items on that day using a unified 5-color semantic palette.
 *
 * Semantic color palette:
 * - Blue (Primary): Future/Scheduled lesson OR Scheduled homework.
 * - Red (Error): Past unlogged lesson OR Overdue homework (action required).
 * - Orange/Yellow (Warning): Lesson completed, pending payment.
 * - Green (Success): Lesson paid OR Homework completed.
 * - Neutral Gray: Cancelled lesson OR Cancelled homework.
 *
 * Priority ordering for lessons:
 * 1. RED_PAST_DUE: Past-due scheduled lesson (requires action) -> StatusRed
 * 2. YELLOW: Completed (awaiting payment) lesson -> StatusYellow
 * 3. BLUE: Scheduled lesson (future/today) -> StatusBlue
 * 4. GREEN: Paid lesson -> StatusGreen
 * 5. GRAY_CANCELLED: Cancelled lesson -> HomeworkGray
 *
 * Priority ordering for homework:
 * 1. RED_OVERDUE: Overdue homework (OVERDUE or past-due PENDING) -> StatusRed
 * 2. BLUE_SCHEDULED: Scheduled/pending homework -> StatusBlue
 * 3. GREEN_COMPLETED: Completed homework -> StatusGreen
 * 4. GRAY_CANCELLED: Cancelled homework -> HomeworkGray
 *
 * @return DayIndicators containing the resolved lesson and homework colors.
 */
internal fun resolveDayIndicators(
    lessons: List<Lesson>,
    homework: List<Homework>,
    date: LocalDate,
    today: LocalDate,
    homeworkToday: LocalDate = today
): DayIndicators {
    val lessonColor = if (lessons.isEmpty()) null else {
        val lessonStatuses = lessons.map { lesson ->
            when (lesson.status) {
                LessonStatus.PAID -> InternalLessonStatus.GREEN
                LessonStatus.COMPLETED -> InternalLessonStatus.YELLOW
                LessonStatus.SCHEDULED -> if (date.isBefore(today)) InternalLessonStatus.RED_PAST_DUE else InternalLessonStatus.BLUE
                LessonStatus.CANCELLED -> InternalLessonStatus.GRAY_CANCELLED
            }
        }
        when {
            lessonStatuses.any { it == InternalLessonStatus.RED_PAST_DUE } -> StatusRed
            lessonStatuses.any { it == InternalLessonStatus.YELLOW } -> StatusYellow
            lessonStatuses.any { it == InternalLessonStatus.BLUE } -> StatusBlue
            lessonStatuses.any { it == InternalLessonStatus.GREEN } -> StatusGreen
            lessonStatuses.any { it == InternalLessonStatus.GRAY_CANCELLED } -> HomeworkGray
            else -> null
        }
    }

    val homeworkColor = if (homework.isEmpty()) null else {
        val homeworkStatuses = homework.map { h ->
            when (h.status) {
                HomeworkStatus.OVERDUE -> InternalHomeworkStatus.RED_OVERDUE
                HomeworkStatus.PENDING -> if (date.isBefore(homeworkToday)) InternalHomeworkStatus.RED_OVERDUE else InternalHomeworkStatus.BLUE_SCHEDULED
                HomeworkStatus.COMPLETED -> InternalHomeworkStatus.GREEN_COMPLETED
                HomeworkStatus.CANCELLED -> InternalHomeworkStatus.GRAY_CANCELLED
            }
        }
        when {
            homeworkStatuses.any { it == InternalHomeworkStatus.RED_OVERDUE } -> StatusRed
            homeworkStatuses.any { it == InternalHomeworkStatus.BLUE_SCHEDULED } -> StatusBlue
            homeworkStatuses.any { it == InternalHomeworkStatus.GREEN_COMPLETED } -> StatusGreen
            homeworkStatuses.any { it == InternalHomeworkStatus.GRAY_CANCELLED } -> HomeworkGray
            else -> null
        }
    }

    return DayIndicators(lessonColor, homeworkColor)
}
