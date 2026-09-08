package com.barutdev.tullab.domain.usecase.reports

import com.barutdev.tullab.domain.model.Lesson
import com.barutdev.tullab.domain.model.LessonStatus
import com.barutdev.tullab.domain.model.reports.ReportRange
import com.barutdev.tullab.domain.model.reports.ReportSummary
import com.barutdev.tullab.domain.repository.LessonRepository
import com.barutdev.tullab.domain.repository.StudentRepository
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.roundToLong
import kotlinx.coroutines.flow.first

class GetReportSummaryUseCase @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val studentRepository: StudentRepository,
    private val clock: Clock,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) {

    suspend operator fun invoke(range: ReportRange?): ReportSummary {
        val targetRange = range ?: ReportRange.default
        val today = LocalDate.now(clock)
        val bounds = targetRange.bounds(today = today, zoneId = zoneId)

        val lessons = lessonRepository.getAllLessons().first()

        // Both hours and earnings are filtered strictly by lesson date so the
        // active timeframe applies consistently (paymentTimestamp is ignored
        // for range filtering to avoid hours/earnings misalignment).
        val paidLessons = lessons.filter { it.isPaidWithin(bounds) }
        val completedLessons = lessons.filter { it.isCompletedWithin(bounds) }

        // Aggregate via Lesson.calculatedValue so PER_HOUR (duration * rateOrFee)
        // and FLAT_FEE (rateOrFee) both contribute using the lesson's own
        // pricing snapshot instead of the student's current hourlyRate.
        val totalEarnings = paidLessons.fold(BigDecimal.ZERO) { acc, lesson ->
            val value = lesson.calculatedValue
            if (value <= 0.0 || value.isNaN()) return@fold acc
            acc + BigDecimal.valueOf(value)
        }.setScale(2, RoundingMode.HALF_UP)

        val totalMinutes = completedLessons.sumOf { lesson ->
            val hours = lesson.durationInHours ?: 0.0
            max(0L, (hours * 60.0).roundToLong())
        }
        val totalHours = Duration.ofMinutes(totalMinutes)

        val activeStudents = completedLessons
            .map { it.studentId }
            .distinct()
            .count()

        return ReportSummary(
            totalEarnings = totalEarnings,
            totalHours = totalHours,
            activeStudents = activeStudents
        )
    }

    private fun Lesson.isPaidWithin(bounds: ClosedRange<LocalDate>): Boolean {
        if (status != LessonStatus.PAID) return false
        // Consistent with hours: filter by lesson date, not paymentTimestamp,
        // so earnings align strictly with the active timeframe filter.
        val lessonDate = date.toLocalDate()
        return lessonDate in bounds
    }

    private fun Lesson.isCompletedWithin(bounds: ClosedRange<LocalDate>): Boolean {
        val relevantStatus = status == LessonStatus.COMPLETED || status == LessonStatus.PAID
        if (!relevantStatus) return false
        val lessonDate = date.toLocalDate()
        return lessonDate in bounds
    }

    private fun Long.toLocalDate(): LocalDate =
        Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()
}
