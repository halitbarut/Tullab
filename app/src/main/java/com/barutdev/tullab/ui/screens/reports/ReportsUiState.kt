package com.barutdev.tullab.ui.screens.reports

import androidx.annotation.StringRes
import com.barutdev.tullab.R
import com.barutdev.tullab.domain.model.reports.MonthlyEarningsPoint
import com.barutdev.tullab.domain.model.reports.ReportRange
import com.barutdev.tullab.domain.model.reports.ReportSummary
import com.barutdev.tullab.domain.model.reports.TopStudentEntry
import java.math.BigDecimal
import java.time.Duration

/**
 * Immutable state consumed by the Reports Compose screen.
 */
data class ReportsUiState(
    val availableRanges: List<ReportRange>,
    val selectedRange: ReportRange,
    val summary: ReportSummary = ReportSummary(
        totalEarnings = BigDecimal.ZERO,
        totalHours = Duration.ZERO,
        activeStudents = 0
    ),
    val monthlyEarnings: List<MonthlyEarningsPoint> = emptyList(),
    val topStudents: List<TopStudentEntry> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: ReportsUiMessage? = null
) {
    val hasTopStudents: Boolean get() = topStudents.isNotEmpty()
    val hasChartData: Boolean get() = monthlyEarnings.isNotEmpty()
    companion object {
        fun initial(selectedRange: ReportRange? = ReportRange.default): ReportsUiState {
            val ranges = ReportRange.presets
            val fallback = selectedRange ?: ReportRange.default
            return ReportsUiState(
                availableRanges = ranges,
                selectedRange = fallback
            )
        }
    }
}

/**
 * Lightweight wrapper around a string resource for transient UI feedback.
 */
data class ReportsUiMessage(
    @StringRes val messageResId: Int,
    val formatArgs: List<Any> = emptyList()
) {
    companion object {
        fun emptyState(): ReportsUiMessage = ReportsUiMessage(R.string.reports_message_empty_period)
        fun loadFailed(): ReportsUiMessage = ReportsUiMessage(R.string.reports_message_load_failed)
    }
}
