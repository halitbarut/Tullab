package com.barutdev.kora.ui.screens.calendar

import androidx.compose.ui.graphics.Color
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

data class DayIndicators(
    val lessonColor: Color?,
    val homeworkColor: Color?
)

internal enum class InternalLessonStatus { RED_PAST_DUE, YELLOW, BLUE, GREEN, RED_CANCELLED }
internal enum class InternalHomeworkStatus { MAGENTA, GRAY, ORANGE, TEAL }

/**
 * Determines the indicator colors for a calendar day based on the statuses
 * of all lessons and homework items on that day.
 *
 * Priority ordering for lessons:
 * - RED_PAST_DUE: Past-due scheduled lesson
 * - YELLOW: Completed (awaiting payment) lesson
 * - BLUE: Scheduled lesson (future)
 * - GREEN: Paid lesson
 * - RED_CANCELLED: Cancelled lesson
 *
 * Priority ordering for homework:
 * - MAGENTA: Overdue homework (explicitly OVERDUE, or PENDING and past due date)
 * - GRAY: Cancelled homework
 * - ORANGE: Pending homework (future)
 * - TEAL: Completed homework
 *
 * @return DayIndicators containing the resolved lesson and homework colors.
 */
internal fun resolveDayIndicators(
    lessons: List<Lesson>,
    homework: List<Homework>,
    date: LocalDate,
    today: LocalDate
): DayIndicators {
    val lessonColor = if (lessons.isEmpty()) null else {
        val lessonStatuses = lessons.map { lesson ->
            when (lesson.status) {
                LessonStatus.PAID -> InternalLessonStatus.GREEN
                LessonStatus.COMPLETED -> InternalLessonStatus.YELLOW
                LessonStatus.SCHEDULED -> if (date.isBefore(today)) InternalLessonStatus.RED_PAST_DUE else InternalLessonStatus.BLUE
                LessonStatus.CANCELLED -> InternalLessonStatus.RED_CANCELLED
            }
        }
        when {
            lessonStatuses.any { it == InternalLessonStatus.RED_PAST_DUE } -> StatusRed
            lessonStatuses.any { it == InternalLessonStatus.YELLOW } -> StatusYellow
            lessonStatuses.any { it == InternalLessonStatus.BLUE } -> StatusBlue
            lessonStatuses.any { it == InternalLessonStatus.GREEN } -> StatusGreen
            lessonStatuses.any { it == InternalLessonStatus.RED_CANCELLED } -> StatusRed
            else -> null
        }
    }

    val homeworkColor = if (homework.isEmpty()) null else {
        val homeworkStatuses = homework.map { h ->
            when (h.status) {
                HomeworkStatus.OVERDUE -> InternalHomeworkStatus.MAGENTA
                HomeworkStatus.PENDING -> if (date.isBefore(today)) InternalHomeworkStatus.MAGENTA else InternalHomeworkStatus.ORANGE
                HomeworkStatus.CANCELLED -> InternalHomeworkStatus.GRAY
                HomeworkStatus.COMPLETED -> InternalHomeworkStatus.TEAL
            }
        }
        when {
            homeworkStatuses.any { it == InternalHomeworkStatus.MAGENTA } -> HomeworkMagenta
            homeworkStatuses.any { it == InternalHomeworkStatus.GRAY } -> HomeworkGray
            homeworkStatuses.any { it == InternalHomeworkStatus.ORANGE } -> StatusOrange
            homeworkStatuses.any { it == InternalHomeworkStatus.TEAL } -> HomeworkTeal
            else -> null
        }
    }

    return DayIndicators(lessonColor, homeworkColor)
}
