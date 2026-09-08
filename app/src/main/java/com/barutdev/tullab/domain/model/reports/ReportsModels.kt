package com.barutdev.tullab.domain.model.reports

import java.math.BigDecimal
import java.time.Duration
import java.time.YearMonth
import java.util.UUID

/**
 * Aggregated metrics shown on the Reports dashboard cards.
 */
data class ReportSummary(
    val totalEarnings: BigDecimal,
    val totalHours: Duration,
    val activeStudents: Int
) {
    companion object {
        fun empty() = ReportSummary(
            totalEarnings = BigDecimal.ZERO,
            totalHours = Duration.ZERO,
            activeStudents = 0
        )
    }
}

/**
 * Single data point for the earnings bar chart covering a calendar month.
 */
data class MonthlyEarningsPoint(
    val month: YearMonth,
    val earnings: BigDecimal
)

/**
 * Representation of a student's teaching activity in the selected period.
 */
data class TopStudentEntry(
    val studentId: UUID,
    val studentName: String,
    val hoursTaught: Duration
)
