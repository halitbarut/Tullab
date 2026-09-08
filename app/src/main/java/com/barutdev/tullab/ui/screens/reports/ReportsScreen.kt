package com.barutdev.tullab.ui.screens.reports

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.barutdev.tullab.util.formatCompactCurrency
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.barutdev.tullab.R
import com.barutdev.tullab.domain.model.UserPreferences
import com.barutdev.tullab.domain.model.reports.MonthlyEarningsPoint
import com.barutdev.tullab.domain.model.reports.ReportRange
import com.barutdev.tullab.domain.model.reports.ReportSummary
import com.barutdev.tullab.domain.model.reports.TopStudentEntry
import com.barutdev.tullab.ui.navigation.ScreenScaffoldConfig
import com.barutdev.tullab.ui.navigation.TopBarAction
import com.barutdev.tullab.ui.navigation.TopBarConfig
import com.barutdev.tullab.ui.preferences.LocalUserPreferences
import com.barutdev.tullab.ui.theme.TullabTheme
import com.barutdev.tullab.ui.theme.LocalLocale
import com.barutdev.tullab.util.formatDurationHours
import com.barutdev.tullab.util.tullabStringResource
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.Duration
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

@Composable
fun ReportsScreen(
    expectedStudentId: Int? = null,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    viewModel: ReportsViewModel = hiltViewModel(
        key = expectedStudentId?.let { "reports-$it" } ?: "reports-default"
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val locale = LocalLocale.current
    val userPreferences = LocalUserPreferences.current
    val currencyFormatter = rememberCurrencyFormatter(
        locale = locale,
        currencyCode = userPreferences.currencyCode
    )
    val topBarTitle = tullabStringResource(id = R.string.reports_title)
    val backContentDescription = tullabStringResource(id = R.string.reports_back_content_description)
    val topBarConfig = remember(topBarTitle, backContentDescription, onBackClick) {
        TopBarConfig(
            title = topBarTitle,
            navigationIcon = TopBarAction(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = backContentDescription,
                onClick = onBackClick
            )
        )
    }

    ScreenScaffoldConfig(topBarConfig = topBarConfig)

    ReportsScreenContent(
        uiState = uiState,
        locale = locale,
        currencyFormatter = currencyFormatter,
        onRangeSelected = viewModel::onRangeSelected,
        onRetry = viewModel::refresh,
        modifier = modifier.fillMaxSize()
    )
}

@VisibleForTesting
@Composable
internal fun ReportsScreenContent(
    uiState: ReportsUiState,
    locale: Locale,
    currencyFormatter: NumberFormat,
    onRangeSelected: (ReportRange) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.errorMessage != null -> {
            ReportsError(
                messageResId = uiState.errorMessage.messageResId,
                onRetry = onRetry,
                modifier = modifier
            )
        }

        else -> {
            ReportsContent(
                availableRanges = uiState.availableRanges,
                selectedRange = uiState.selectedRange,
                onRangeSelected = onRangeSelected,
                summary = uiState.summary,
                monthlyEarnings = uiState.monthlyEarnings,
                topStudents = uiState.topStudents,
                locale = locale,
                currencyFormatter = currencyFormatter,
                isEmptyPeriod = uiState.monthlyEarnings.all { it.earnings.compareTo(BigDecimal.ZERO) == 0 },
                modifier = modifier
            )
        }
    }
}

@Composable
private fun ReportsContent(
    availableRanges: List<ReportRange>,
    selectedRange: ReportRange,
    onRangeSelected: (ReportRange) -> Unit,
    summary: ReportSummary,
    monthlyEarnings: List<MonthlyEarningsPoint>,
    topStudents: List<TopStudentEntry>,
    locale: Locale,
    currencyFormatter: NumberFormat,
    isEmptyPeriod: Boolean,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        RangeFilterSection(
            ranges = availableRanges,
            selectedRange = selectedRange,
            onRangeSelected = onRangeSelected
        )
        SummaryCards(
            summary = summary,
            locale = locale,
            currencyFormatter = currencyFormatter
        )
        val userPreferences = LocalUserPreferences.current
        EarningsChart(
            points = monthlyEarnings,
            currencyCode = userPreferences.currencyCode,
            locale = locale,
            currencyFormatter = currencyFormatter,
            isEmptyPeriod = isEmptyPeriod
        )
        TopStudentsSection(
            students = topStudents,
            locale = locale
        )
    }
}

@Composable
private fun RangeFilterSection(
    ranges: List<ReportRange>,
    selectedRange: ReportRange,
    onRangeSelected: (ReportRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = tullabStringResource(id = R.string.reports_filters_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
        ) {
            items(
                items = ranges,
                key = { it.labelResId }
            ) { range ->
                val label = tullabStringResource(id = range.labelResId)
                FilterChip(
                    selected = range == selectedRange,
                    onClick = { if (range != selectedRange) onRangeSelected(range) },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    modifier = Modifier.testTag("reports-range-${range.labelResId}"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
private fun SummaryCards(
    summary: ReportSummary,
    locale: Locale,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    val earningsValue = remember(summary.totalEarnings, currencyFormatter) {
        currencyFormatter.format(summary.totalEarnings)
    }
    val hoursValue = formatDurationHours(summary.totalHours.toMinutes() / 60.0)
    val activeStudentsValue = remember(summary.activeStudents, locale) {
        NumberFormat.getIntegerInstance(locale).format(summary.activeStudents)
    }

    // Horizontally scrollable KPI row: guarantees no clipping for long
    // localized titles (en/de/tr) on narrow screens while keeping balanced
    // card sizing via fixed width + min height.
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
    ) {
            item(key = "total_earnings") {
                SummaryCard(
                    title = tullabStringResource(id = R.string.reports_summary_total_earnings),
                    value = earningsValue,
                    modifier = Modifier.width(172.dp)
                )
            }
            item(key = "total_hours") {
                SummaryCard(
                    title = tullabStringResource(id = R.string.reports_summary_total_hours),
                    value = hoursValue,
                    modifier = Modifier.width(172.dp)
                )
            }
            item(key = "active_students") {
                SummaryCard(
                    title = tullabStringResource(id = R.string.reports_summary_active_students),
                    value = activeStudentsValue,
                    modifier = Modifier.width(172.dp)
                )
            }
        }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 112.dp)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // labelMedium + 2-3 line bounds prevents single-character orphan
            // wraps (e.g. "Gesamteinnahme\nn") and keeps titles legible in
            // German, English and Turkish without clipping.
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                minLines = 2,
                maxLines = 3,
                softWrap = true,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EarningsChart(
    points: List<MonthlyEarningsPoint>,
    currencyCode: String,
    locale: Locale,
    currencyFormatter: NumberFormat,
    isEmptyPeriod: Boolean,
    modifier: Modifier = Modifier
) {
    val title = tullabStringResource(id = R.string.reports_chart_title)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (points.isEmpty() || isEmptyPeriod) {
            EmptyState(
                icon = Icons.Outlined.BarChart,
                message = tullabStringResource(id = R.string.reports_message_empty_period)
            )
        } else {
            val monthFormatter = rememberMonthLabelFormatter(locale)
            val fullPeriodFormatter = remember(locale) {
                DateTimeFormatter.ofPattern("LLLL yyyy", locale)
            }
            val bars = remember(points, locale, currencyFormatter, currencyCode) {
                points.map { point ->
                    val label = monthFormatter.format(point.month)
                    val rawPeriod = fullPeriodFormatter.format(point.month)
                    val fullPeriodLabel = rawPeriod.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(locale) else it.toString()
                    }
                    val amount = point.earnings
                    val amountDouble = amount.toDouble()
                    ChartBar(
                        label = label,
                        fullPeriodLabel = fullPeriodLabel,
                        value = amountDouble,
                        compactValue = formatCompactCurrency(amountDouble, currencyCode, locale),
                        formattedValue = currencyFormatter.format(amount)
                    )
                }
            }
            BarChart(bars = bars, locale = locale)
        }
    }
}

private data class ChartBar(
    val label: String,
    val fullPeriodLabel: String,
    val value: Double,
    val compactValue: String,
    val formattedValue: String
)

private const val MONTH_LABEL_PATTERN = "LLL"
private val ReportsChartHeight = 160.dp
private val ReportsChartBarWidth = 28.dp
private val ReportsChartBarSpacing = 16.dp
private val ReportsChartLabelSpacing = 8.dp
private val ReportsChartMinBarHeight = 8.dp
private val ReportsChartBarCornerRadius = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
private val ReportsChartMinLabelPadding = 4.dp

@Composable
private fun rememberCurrencyFormatter(
    locale: Locale,
    currencyCode: String
): NumberFormat {
    return remember(locale, currencyCode) {
        NumberFormat.getCurrencyInstance(locale).apply {
            runCatching { Currency.getInstance(currencyCode) }
                .getOrNull()
                ?.let { desiredCurrency ->
                    currency = desiredCurrency
                }
            if (currencyCode == "TRY" && this is java.text.DecimalFormat) {
                val symbols = decimalFormatSymbols
                symbols.currencySymbol = "₺"
                decimalFormatSymbols = symbols
            }
        }
    }
}

@Composable
private fun rememberMonthLabelFormatter(locale: Locale): DateTimeFormatter {
    return remember(locale) {
        DateTimeFormatter.ofPattern(MONTH_LABEL_PATTERN)
            .withLocale(locale)
    }
}

@Composable
private fun BarChart(
    bars: List<ChartBar>,
    locale: Locale,
    modifier: Modifier = Modifier,
    chartHeight: Dp = ReportsChartHeight
) {
    var selectedBarIndex by remember { mutableStateOf<Int?>(null) }

    // Measure label height to reserve space at bottom of chart
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.bodySmall
    val density = LocalDensity.current
    val labelHeight = remember(labelStyle, density) {
        val textLayoutResult = textMeasurer.measure(
            text = "MMM",
            style = labelStyle
        )
        with(density) { textLayoutResult.size.height.toDp() }
    }

    val valueLabelHeight = 18.dp
    // Calculate available height for bars by reserving space for value label, bar, and month label
    val availableBarHeight = chartHeight - labelHeight - valueLabelHeight - ReportsChartMinLabelPadding - ReportsChartLabelSpacing

    val maxValue = bars.maxOfOrNull { it.value }.takeUnless { it == null || it == 0.0 } ?: 1.0

    Column(modifier = modifier.fillMaxWidth()) {
        // Interactive tap tooltip
        AnimatedVisibility(
            visible = selectedBarIndex != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            selectedBarIndex?.let { index ->
                val selectedBar = bars.getOrNull(index)
                if (selectedBar != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.inverseSurface,
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clickable { selectedBarIndex = null }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedBar.fullPeriodLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = selectedBar.formattedValue,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(ReportsChartBarSpacing)
        ) {
            bars.forEachIndexed { index, bar ->
                val isSelected = selectedBarIndex == index
                val heightFraction = (bar.value / maxValue).coerceIn(0.0, 1.0).toFloat()
                val barDescription = tullabStringResource(
                    id = R.string.reports_chart_accessibility_bar,
                    bar.label,
                    bar.formattedValue
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            selectedBarIndex = if (isSelected) null else index
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = bar.compactValue,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val targetHeight = (availableBarHeight.value * heightFraction).dp
                    val resolvedHeight = if (targetHeight < ReportsChartMinBarHeight) {
                        ReportsChartMinBarHeight
                    } else {
                        targetHeight
                    }
                    Box(
                        modifier = Modifier
                            .width(ReportsChartBarWidth)
                            .height(resolvedHeight)
                            .clip(ReportsChartBarCornerRadius)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                            )
                            .semantics {
                                contentDescription = barDescription
                            }
                    )
                    Spacer(modifier = Modifier.height(ReportsChartLabelSpacing))
                    Text(
                        text = bar.label.uppercase(locale = locale),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun TopStudentsSection(
    students: List<TopStudentEntry>,
    locale: Locale,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = tullabStringResource(id = R.string.reports_top_students_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = tullabStringResource(id = R.string.reports_top_students_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (students.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.Group,
                message = tullabStringResource(id = R.string.reports_top_students_empty)
            )
        } else {
            students.forEachIndexed { index, entry ->
                TopStudentRow(
                    rank = index + 1,
                    entry = entry,
                    locale = locale
                )
            }
        }
    }
}

@Composable
private fun TopStudentRow(
    rank: Int,
    entry: TopStudentEntry,
    locale: Locale,
    modifier: Modifier = Modifier
) {
    val hours = remember(entry.hoursTaught, locale) {
        val totalHours = entry.hoursTaught.toMinutes() / 60.0
        NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 1
        }.format(totalHours)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = rank.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = entry.studentName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = tullabStringResource(id = R.string.reports_summary_hours_unit, hours),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReportsError(
    messageResId: Int,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.BarChart,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = tullabStringResource(id = messageResId),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(text = tullabStringResource(id = R.string.reports_action_retry))
        }
    }
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(60.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(locale = "en", showBackground = true)
@Composable
private fun ReportsScreenPreview() {
    val preferences = UserPreferences(
        isDarkMode = false,
        languageCode = "en",
        currencyCode = "USD",
        defaultHourlyRate = 0.0,
        lessonRemindersEnabled = false,
        logReminderEnabled = false,
        lessonReminderHour = 9,
        lessonReminderMinute = 0,
        logReminderHour = 20,
        logReminderMinute = 0
    )
    PreviewReportsScreen(locale = Locale.US, preferences = preferences)
}

@Preview(locale = "de", showBackground = true)
@Composable
private fun ReportsScreenGermanPreview() {
    val preferences = UserPreferences(
        isDarkMode = false,
        languageCode = "de",
        currencyCode = "EUR",
        defaultHourlyRate = 0.0,
        lessonRemindersEnabled = false,
        logReminderEnabled = false,
        lessonReminderHour = 9,
        lessonReminderMinute = 0,
        logReminderHour = 20,
        logReminderMinute = 0
    )
    PreviewReportsScreen(locale = Locale.GERMANY, preferences = preferences)
}

@Composable
private fun PreviewReportsScreen(
    locale: Locale,
    preferences: UserPreferences
) {
    TullabTheme {
        CompositionLocalProvider(
            LocalLocale provides locale,
            LocalUserPreferences provides preferences
        ) {
            val currencyFormatter = rememberCurrencyFormatter(locale, preferences.currencyCode)
            val sampleState = remember { previewReportsUiState() }
            ReportsScreenContent(
                uiState = sampleState,
                locale = locale,
                currencyFormatter = currencyFormatter,
                onRangeSelected = {},
                onRetry = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private fun previewReportsUiState(): ReportsUiState {
    val sampleSummary = ReportSummary(
        totalEarnings = BigDecimal("1234.50"),
        totalHours = Duration.ofHours(42),
        activeStudents = 8
    )
    val sampleMonthly = (0..5).map { offset ->
        val month = YearMonth.now().minusMonths((5 - offset).toLong())
        MonthlyEarningsPoint(
            month = month,
            earnings = BigDecimal.valueOf(800 + offset * 120L)
        )
    }
    val sampleStudents = listOf(
        TopStudentEntry(
            studentId = java.util.UUID.randomUUID(),
            studentName = "Alice Johnson",
            hoursTaught = Duration.ofHours(10)
        ),
        TopStudentEntry(
            studentId = java.util.UUID.randomUUID(),
            studentName = "Bilal Demir",
            hoursTaught = Duration.ofHours(8)
        ),
        TopStudentEntry(
            studentId = java.util.UUID.randomUUID(),
            studentName = "Chloe Martin",
            hoursTaught = Duration.ofHours(6)
        )
    )
    return ReportsUiState(
        availableRanges = ReportRange.presets,
        selectedRange = ReportRange.default,
        summary = sampleSummary,
        monthlyEarnings = sampleMonthly,
        topStudents = sampleStudents,
        isLoading = false,
        errorMessage = null
    )
}
