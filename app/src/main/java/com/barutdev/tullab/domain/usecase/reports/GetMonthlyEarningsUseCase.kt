package com.barutdev.tullab.domain.usecase.reports

import com.barutdev.tullab.domain.model.LessonStatus
import com.barutdev.tullab.domain.model.reports.MonthlyEarningsPoint
import com.barutdev.tullab.domain.repository.LessonRepository
import com.barutdev.tullab.domain.repository.StudentRepository
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class GetMonthlyEarningsUseCase @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val studentRepository: StudentRepository,
    private val clock: Clock,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) {

    suspend operator fun invoke(): List<MonthlyEarningsPoint> {
        val today = LocalDate.now(clock)
        val currentMonth = YearMonth.from(today)
        val months = (5 downTo 0).map { offset -> currentMonth.minusMonths(offset.toLong()) }
        val lessons = lessonRepository.getAllLessons().first()

        val monthlyTotals = months.associateWith { BigDecimal.ZERO.setScale(2) }.toMutableMap()

        // Group PAID lessons by lesson date (not paymentTimestamp) so the
        // chart aligns with the summary's timeframe logic and FLAT_FEE lessons
        // (which have null duration) are included via calculatedValue.
        lessons.filter { it.status == LessonStatus.PAID }
            .forEach { lesson ->
                val lessonMonth = lesson.date.toYearMonth()
                if (!monthlyTotals.containsKey(lessonMonth)) return@forEach
                val amountValue = lesson.calculatedValue
                if (amountValue <= 0.0 || amountValue.isNaN()) return@forEach
                val amount = BigDecimal.valueOf(amountValue)
                    .setScale(2, RoundingMode.HALF_UP)
                monthlyTotals[lessonMonth] = monthlyTotals.getValue(lessonMonth).add(amount)
            }

        return months.map { month ->
            MonthlyEarningsPoint(
                month = month,
                earnings = monthlyTotals.getValue(month).setScale(2, RoundingMode.HALF_UP)
            )
        }
    }

    private fun Long.toYearMonth(): YearMonth =
        YearMonth.from(Instant.ofEpochMilli(this).atZone(zoneId))
}
