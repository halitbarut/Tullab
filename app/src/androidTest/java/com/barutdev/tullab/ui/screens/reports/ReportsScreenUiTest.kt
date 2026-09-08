package com.barutdev.tullab.ui.screens.reports

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.barutdev.tullab.domain.model.UserPreferences
import com.barutdev.tullab.domain.model.reports.MonthlyEarningsPoint
import com.barutdev.tullab.domain.model.reports.ReportRange
import com.barutdev.tullab.domain.model.reports.ReportSummary
import com.barutdev.tullab.domain.model.reports.TopStudentEntry
import com.barutdev.tullab.ui.preferences.LocalUserPreferences
import com.barutdev.tullab.ui.theme.TullabTheme
import com.barutdev.tullab.ui.theme.LocalLocale
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.Duration
import java.time.YearMonth
import java.util.Currency
import java.util.Locale
import java.util.UUID

@RunWith(AndroidJUnit4::class)
@LargeTest
class ReportsScreenUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun reportsScreen_englishLocaleDisplaysUsdCurrencyAndHidesBottomNavigation() {
        val preferences = userPreferences(
            languageCode = "en-US",
            currencyCode = "USD"
        )
        setReportsContent(preferences = preferences)

        assertNoBottomNavigation()
        assertCurrencySymbolDisplayed("$")
        assertMonthLabels(listOf("JAN", "FEB"))
    }

    @Test
    fun reportsScreen_germanLocaleDisplaysEuroAndLocalizedLabels() {
        val preferences = userPreferences(
            languageCode = "de-DE",
            currencyCode = "EUR"
        )
        setReportsContent(preferences = preferences)

        assertNoBottomNavigation()
        assertCurrencySymbolDisplayed("€")
        assertMonthLabels(listOf("JAN", "M\u00C4R"))
    }

    private fun setReportsContent(
        preferences: UserPreferences,
        state: ReportsUiState = sampleReportsUiState()
    ) {
        val locale = Locale.forLanguageTag(preferences.languageCode).takeIf { it.language.isNotEmpty() }
            ?: Locale(preferences.languageCode)
        composeRule.setContent {
            TullabTheme {
                CompositionLocalProvider(
                    LocalLocale provides locale,
                    LocalUserPreferences provides preferences
                ) {
                    val currencyFormatter = remember(locale, preferences.currencyCode) {
                        NumberFormat.getCurrencyInstance(locale).apply {
                            runCatching { Currency.getInstance(preferences.currencyCode) }
                                .getOrNull()
                                ?.let { desiredCurrency ->
                                    currency = desiredCurrency
                                }
                        }
                    }
                    ReportsScreenContent(
                        uiState = state,
                        locale = locale,
                        currencyFormatter = currencyFormatter,
                        onRangeSelected = {},
                        onRetry = {},
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    private fun assertNoBottomNavigation() {
        val tabListMatcher = SemanticsMatcher.expectValue(
            SemanticsProperties.Role,
            Role.TabList
        )
        composeRule.onAllNodes(tabListMatcher, useUnmergedTree = true)
            .assertCountEquals(0)
    }

    private fun assertCurrencySymbolDisplayed(symbol: String) {
        composeRule.onNode(
            hasText(symbol, substring = true),
            useUnmergedTree = true
        ).assertExists()
    }

    private fun assertMonthLabels(expected: List<String>) {
        expected.forEach { label ->
            composeRule.onNodeWithText(label, useUnmergedTree = true)
                .assertExists()
        }
    }

    private fun userPreferences(
        languageCode: String,
        currencyCode: String
    ): UserPreferences {
        return UserPreferences(
            isDarkMode = false,
            languageCode = languageCode,
            currencyCode = currencyCode,
            defaultHourlyRate = 0.0,
            lessonRemindersEnabled = false,
            logReminderEnabled = false,
            lessonReminderHour = 9,
            lessonReminderMinute = 0,
            logReminderHour = 20,
            logReminderMinute = 0
        )
    }

    private fun sampleReportsUiState(): ReportsUiState {
        val summary = ReportSummary(
            totalEarnings = BigDecimal("1750.25"),
            totalHours = Duration.ofHours(58),
            activeStudents = 6
        )
        val monthlyPoints = (1..6).map { index ->
            MonthlyEarningsPoint(
                month = YearMonth.of(2024, index),
                earnings = BigDecimal.valueOf(600 + index * 75L)
            )
        }
        val topStudents = listOf(
            TopStudentEntry(
                studentId = UUID.randomUUID(),
                studentName = "Eva Schneider",
                hoursTaught = Duration.ofHours(12)
            ),
            TopStudentEntry(
                studentId = UUID.randomUUID(),
                studentName = "Marco Rossi",
                hoursTaught = Duration.ofHours(9)
            )
        )
        return ReportsUiState(
            availableRanges = ReportRange.presets,
            selectedRange = ReportRange.default,
            summary = summary,
            monthlyEarnings = monthlyPoints,
            topStudents = topStudents,
            isLoading = false,
            errorMessage = null
        )
    }
}
