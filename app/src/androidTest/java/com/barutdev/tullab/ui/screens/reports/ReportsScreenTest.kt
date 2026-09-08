package com.barutdev.tullab.ui.screens.reports

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.barutdev.tullab.R
import com.barutdev.tullab.domain.model.reports.MonthlyEarningsPoint
import com.barutdev.tullab.domain.model.reports.ReportRange
import com.barutdev.tullab.domain.model.reports.ReportSummary
import com.barutdev.tullab.domain.model.reports.TopStudentEntry
import com.barutdev.tullab.ui.theme.TullabTheme
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.Duration
import java.time.YearMonth
import java.util.Locale
import java.util.UUID
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReportsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val locale = Locale.US
    private val currencyFormatter = NumberFormat.getCurrencyInstance(locale)

    @Test
    fun reportsScreen_displaysSummaryAndEarningsChart() {
        val summary = ReportSummary(
            totalEarnings = BigDecimal("900.00"),
            totalHours = Duration.ofHours(18),
            activeStudents = 3
        )
        val monthly = listOf(
            MonthlyEarningsPoint(YearMonth.of(2025, 2), BigDecimal("300.00")),
            MonthlyEarningsPoint(YearMonth.of(2025, 3), BigDecimal("600.00"))
        )

        composeRule.setContent {
            TullabTheme {
                ReportsScreenContent(
                    uiState = ReportsUiState(
                        availableRanges = listOf(ReportRange.ThisMonth),
                        selectedRange = ReportRange.ThisMonth,
                        summary = summary,
                        monthlyEarnings = monthly,
                        topStudents = emptyList(),
                        isLoading = false,
                        errorMessage = null
                    ),
                    locale = locale,
                    currencyFormatter = currencyFormatter,
                    onRangeSelected = {},
                    onRetry = {}
                )
            }
        }

        composeRule.onNodeWithText(getString(R.string.reports_filters_title))
            .assertIsDisplayed()
        composeRule.onNodeWithText(getString(R.string.reports_summary_total_earnings))
            .assertIsDisplayed()
        composeRule.onNodeWithText(currencyFormatter.format(900.00))
            .assertIsDisplayed()
        composeRule.onNodeWithText(getString(R.string.reports_top_students_subtitle))
            .assertIsDisplayed()
        composeRule.onNodeWithText(getString(R.string.reports_top_students_empty))
            .assertIsDisplayed()

        val barDescription = getString(
            R.string.reports_chart_accessibility_bar,
            "Feb",
            currencyFormatter.format(300.00)
        )
        composeRule.onNodeWithContentDescription(barDescription).assertIsDisplayed()
    }

    @Test
    fun selectingRangeChipUpdatesContent() {
        val initialState = ReportsUiState(
            availableRanges = listOf(ReportRange.ThisMonth, ReportRange.LastThirtyDays),
            selectedRange = ReportRange.ThisMonth,
            summary = ReportSummary(
                totalEarnings = BigDecimal("300.00"),
                totalHours = Duration.ofHours(8),
                activeStudents = 2
            ),
            monthlyEarnings = listOf(
                MonthlyEarningsPoint(YearMonth.of(2025, 2), BigDecimal("150.00")),
                MonthlyEarningsPoint(YearMonth.of(2025, 3), BigDecimal("150.00"))
            ),
            topStudents = emptyList(),
            isLoading = false,
            errorMessage = null
        )

        val updatedState = initialState.copy(
            selectedRange = ReportRange.LastThirtyDays,
            summary = ReportSummary(
                totalEarnings = BigDecimal("450.00"),
                totalHours = Duration.ofHours(12),
                activeStudents = 3
            ),
            topStudents = listOf(
                TopStudentEntry(
                    studentId = UUID.randomUUID(),
                    studentName = "Jordan Lee",
                    hoursTaught = Duration.ofHours(5)
                ),
                TopStudentEntry(
                    studentId = UUID.randomUUID(),
                    studentName = "Maya Patel",
                    hoursTaught = Duration.ofHours(3)
                )
            )
        )

        composeRule.setContent {
            TullabTheme {
                var uiState by remember { mutableStateOf(initialState) }
                ReportsScreenContent(
                    uiState = uiState,
                    locale = locale,
                    currencyFormatter = currencyFormatter,
                    onRangeSelected = { range ->
                        if (range == ReportRange.LastThirtyDays) {
                            uiState = updatedState
                        }
                    },
                    onRetry = {}
                )
            }
        }

        composeRule.onNodeWithText(getString(R.string.reports_top_students_empty))
            .assertIsDisplayed()

        composeRule.onNodeWithText(getString(R.string.reports_range_last_30_days))
            .performClick()

        composeRule.onNodeWithText(currencyFormatter.format(450.00))
            .assertIsDisplayed()
        composeRule.onNodeWithText("Jordan Lee")
            .assertIsDisplayed()
        composeRule.onNodeWithText(getString(R.string.reports_top_students_empty))
            .assertDoesNotExist()
        composeRule.onNodeWithTag("reports-range-${R.string.reports_range_last_30_days}")
            .assertIsSelected()
    }

    private fun getString(id: Int, vararg args: Any): String =
        androidx.test.platform.app.InstrumentationRegistry
            .getInstrumentation()
            .targetContext
            .getString(id, *args)
}
