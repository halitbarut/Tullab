package com.barutdev.tullab.ui.screens.calendar

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.barutdev.tullab.util.tullabStringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import com.barutdev.tullab.R
import com.barutdev.tullab.domain.model.Lesson
import com.barutdev.tullab.domain.model.PricingMode
import com.barutdev.tullab.util.formatCurrency
import com.barutdev.tullab.domain.model.LessonStatus
import com.barutdev.tullab.util.formatCurrency
import com.barutdev.tullab.ui.navigation.FabConfig
import com.barutdev.tullab.ui.navigation.LocalTullabScaffoldController
import com.barutdev.tullab.ui.navigation.ScreenScaffoldConfig
import com.barutdev.tullab.ui.navigation.TopBarAction
import com.barutdev.tullab.ui.navigation.TopBarConfig
import com.barutdev.tullab.ui.screens.dashboard.components.LogLessonDialog
import com.barutdev.tullab.util.formatDurationHours
import com.barutdev.tullab.ui.screens.common.StudentNameUiStatus
import com.barutdev.tullab.ui.screens.common.deriveStudentNameUiStatus
import com.barutdev.tullab.ui.theme.TullabTheme
import com.barutdev.tullab.ui.theme.LocalLocale
import com.barutdev.tullab.ui.theme.StatusBlue
import com.barutdev.tullab.ui.theme.StatusGreen
import com.barutdev.tullab.ui.theme.StatusRed
import com.barutdev.tullab.ui.theme.StatusYellow
import com.barutdev.tullab.ui.theme.StatusOrange
import com.barutdev.tullab.ui.theme.StatusOrangeContainer
import com.barutdev.tullab.ui.theme.StatusRedContainer
import com.barutdev.tullab.ui.theme.HomeworkTeal
import com.barutdev.tullab.ui.theme.HomeworkTealContainer
import com.barutdev.tullab.ui.theme.HomeworkMagenta
import com.barutdev.tullab.ui.theme.HomeworkMagentaContainer
import com.barutdev.tullab.ui.theme.HomeworkGray
import com.barutdev.tullab.ui.theme.HomeworkGrayContainer
import com.barutdev.tullab.ui.theme.TullabAnimationSpecs
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.material3.Surface
import com.barutdev.tullab.ui.components.AnimatedListItem
import com.barutdev.tullab.ui.components.CalendarSpeedDialFab
import com.barutdev.tullab.ui.screens.calendar.components.CalendarLegendBottomSheet
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale
import com.barutdev.tullab.domain.model.Homework
import com.barutdev.tullab.domain.model.HomeworkStatus
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.PlaylistAdd


@Composable
fun CalendarScreen(
    onNavigateToStudentList: () -> Unit,
    onNavigateToHomework: (Int, Int) -> Unit,
    onNavigateToBulkSchedule: (Int) -> Unit,
    expectedStudentId: Int? = null,
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel(
        key = expectedStudentId?.let { "calendar-$it" } ?: "calendar-default"
    )
) {
    val scaffoldController = LocalTullabScaffoldController.current
    val studentName by viewModel.studentName.collectAsStateWithLifecycle()
    val lessons by viewModel.lessons.collectAsStateWithLifecycle()
    val homework by viewModel.homework.collectAsStateWithLifecycle()
    val currentMonth by viewModel.currentMonth.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val isLogLessonDialogVisible by viewModel.isLogLessonDialogVisible.collectAsStateWithLifecycle()
    val lessonToLog by viewModel.lessonToLog.collectAsStateWithLifecycle()
    val pendingLessonForPayment by viewModel.pendingLessonForPayment.collectAsStateWithLifecycle()
    val requiresFeePrompt by viewModel.requiresFeePrompt.collectAsStateWithLifecycle()
    val lessonToRevert by viewModel.lessonToRevert.collectAsStateWithLifecycle()
    val currencyCode by viewModel.currencyCode.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val zoneId = remember { ZoneId.systemDefault() }
    val snackbarHostState = scaffoldController.snackbarHostState

    LaunchedEffect(expectedStudentId) {
        Log.d("CalendarScreen", "Composing for expectedStudentId=$expectedStudentId")
    }
    LaunchedEffect(viewModel.studentId) {
        viewModel.studentId?.let { id ->
            Log.d("CalendarScreen", "Rendering calendar for studentId=$id")
        }
    }

    val studentNameStatus = deriveStudentNameUiStatus(
        studentName = studentName,
        hasStudentReference = viewModel.hasStudentReference
    )
    val calendarLabel = tullabStringResource(id = R.string.calendar_title)
    val resolvedStudentName = studentName.takeIf {
        studentNameStatus == StudentNameUiStatus.Ready && it.isNotBlank()
    } ?: calendarLabel
    val topBarTitle = if (studentNameStatus == StudentNameUiStatus.Ready && studentName.isNotBlank()) {
        tullabStringResource(
            id = R.string.calendar_top_bar_title,
            resolvedStudentName,
            calendarLabel
        )
    } else {
        calendarLabel
    }
    val navigateToListDescription = tullabStringResource(
        id = R.string.top_bar_navigate_to_student_list_content_description
    )
    val addLessonDescription = tullabStringResource(
        id = R.string.calendar_add_lesson_fab_content_description
    )
    val scheduledMessage = tullabStringResource(id = R.string.calendar_lesson_scheduled_message)
    val containerColor = MaterialTheme.colorScheme.primary
    val contentColor = MaterialTheme.colorScheme.onPrimary

    val today = LocalDate.now(ZoneId.systemDefault())
    val currentLessonToLog = lessonToLog
    val isScheduled = currentLessonToLog != null && currentLessonToLog.status == LessonStatus.SCHEDULED
    val isPastScheduled = isScheduled && Instant.ofEpochMilli(currentLessonToLog!!.date).atZone(ZoneId.systemDefault()).toLocalDate().isBefore(today)
    val isFutureScheduled = isScheduled && !isPastScheduled

    LogLessonDialog(
        showDialog = isLogLessonDialogVisible,
        lesson = lessonToLog,
        onDismiss = viewModel::dismissLogLessonDialog,
        onSave = { duration, notes, pricingMode, rateOrFeeInput, isCompleted ->
            currentLessonToLog?.let {
                viewModel.onSaveLessonDetails(it, duration, notes, pricingMode, rateOrFeeInput, isCompleted)
            }
        },
        onComplete = { duration, notes, pricingMode, rateOrFeeInput ->
            viewModel.onLogLessonComplete(duration, notes, pricingMode, rateOrFeeInput)
        },
        onMarkNotDone = { notes ->
            viewModel.onLogLessonMarkNotDone(notes)
        },
        currencyCode = currencyCode
    )

    LogLessonDialog(
        showDialog = pendingLessonForPayment != null,
        lesson = pendingLessonForPayment,
        onDismiss = viewModel::dismissMarkLessonAsPaidDialog,
        onComplete = { _, _, _, _ -> },
        onMarkNotDone = { _ -> },
        isMarkAsPaidMode = true,
        requiresFeePrompt = requiresFeePrompt,
        onMarkAsPaid = { durationStr, feeStr ->
            val duration = durationStr.trim().replace(',', '.').toDoubleOrNull()
            val fee = feeStr.trim().replace(',', '.').toDoubleOrNull()
            viewModel.onConfirmMarkLessonAsPaid(duration, fee)
        },
        currencyCode = currencyCode
    )

    if (lessonToRevert != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = viewModel::dismissRevertDialog,
            title = { Text(text = tullabStringResource(id = R.string.calendar_dialog_revert_payment_title)) },
            text = { Text(text = tullabStringResource(id = R.string.calendar_dialog_revert_payment_message)) },
            confirmButton = {
                Button(onClick = viewModel::onConfirmRevertPayment) {
                    Text(text = tullabStringResource(id = R.string.calendar_dialog_revert_payment_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissRevertDialog) {
                    Text(text = tullabStringResource(id = R.string.dialog_action_cancel))
                }
            }
        )
    }

    val topBarConfig = remember(
        topBarTitle,
        navigateToListDescription,
        onNavigateToStudentList
    ) {
        TopBarConfig(
            title = topBarTitle,
            navigationIcon = TopBarAction(
                icon = Icons.Outlined.Groups,
                contentDescription = navigateToListDescription,
                onClick = onNavigateToStudentList
            ),
            actions = emptyList()
        )
    }
    ScreenScaffoldConfig(
        topBarConfig = topBarConfig,
        fabConfig = null
    )

    Box(modifier = modifier.fillMaxSize()) {
        CalendarScreenContent(
            modifier = Modifier.fillMaxSize(),
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            lessons = lessons,
            homework = homework,
            currencyCode = currencyCode,
            onPreviousMonth = viewModel::onPreviousMonth,
            onNextMonth = viewModel::onNextMonth,
            onSelectDate = viewModel::onSelectDate,
            onLogLessonClick = viewModel::onLogLessonClicked,
            onLessonMarkAsPaidClick = viewModel::onMarkLessonAsPaidClicked,
            onRevertPaymentClick = viewModel::onRevertLessonPaymentClicked,
            onToggleHomeworkStatus = viewModel::toggleHomeworkStatus,
            onHomeworkDetailsClick = { homework ->
                viewModel.studentId?.let { studentId ->
                    onNavigateToHomework(studentId, homework.id)
                }
            }
        )

        val selectedDateLesson = remember(selectedDate, lessons, zoneId) {
            lessons.firstOrNull { lesson ->
                Instant.ofEpochMilli(lesson.date)
                    .atZone(zoneId)
                    .toLocalDate() == selectedDate
            }
        }
        val hasLessonOnSelectedDate = selectedDateLesson != null
        val editLessonLabel = tullabStringResource(id = R.string.calendar_speed_dial_edit_lesson)
        val scheduleForDateLabel = tullabStringResource(id = R.string.calendar_speed_dial_schedule_for_date)
        val dateActionLabel = if (hasLessonOnSelectedDate) editLessonLabel else scheduleForDateLabel
        val dateActionIcon = if (hasLessonOnSelectedDate) Icons.Outlined.Edit else Icons.Outlined.Event
        val isSnackbarVisible = snackbarHostState.currentSnackbarData != null

        CalendarSpeedDialFab(
            onScheduleForDateClick = {
                if (hasLessonOnSelectedDate && selectedDateLesson != null) {
                    viewModel.onLogLessonClicked(selectedDateLesson)
                } else {
                    val epochMillis = selectedDate
                        .atStartOfDay(zoneId)
                        .toInstant()
                        .toEpochMilli()
                    coroutineScope.launch {
                        viewModel.saveLesson(epochMillis)
                        snackbarHostState.showSnackbar(message = scheduledMessage)
                    }
                }
            },
            onBulkScheduleClick = {
                viewModel.studentId?.let { studentId ->
                    onNavigateToBulkSchedule(studentId)
                }
            },
            dateActionLabel = dateActionLabel,
            dateActionIcon = dateActionIcon,
            snackbarVisible = isSnackbarVisible,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun CalendarScreenContent(
    modifier: Modifier = Modifier,
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    lessons: List<Lesson>,
    homework: List<Homework>,
    currencyCode: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onLogLessonClick: (Lesson) -> Unit,
    onLessonMarkAsPaidClick: (Lesson) -> Unit,
    onRevertPaymentClick: (Lesson) -> Unit,
    onToggleHomeworkStatus: (Homework) -> Unit,
    onHomeworkDetailsClick: (Homework) -> Unit,
    onNavigateToBulkSchedule: (() -> Unit)? = null
) {
    val currentLocale = LocalLocale.current
    val zoneId = remember { ZoneId.systemDefault() }
    val today = remember { LocalDate.now(zoneId) }

    val lessonsByDate = remember(lessons, zoneId) {
        lessons.groupBy { lesson ->
            Instant.ofEpochMilli(lesson.date)
                .atZone(zoneId)
                .toLocalDate()
        }
    }
    val homeworkByDate = remember(homework, zoneId) {
        homework.groupBy { h ->
            Instant.ofEpochMilli(h.dueDate)
                .atZone(zoneId)
                .toLocalDate()
        }
    }
    val selectedDateLessons = remember(selectedDate, lessonsByDate) {
        lessonsByDate[selectedDate].orEmpty()
    }
    val selectedDateHomework = remember(selectedDate, homeworkByDate) {
        homeworkByDate[selectedDate].orEmpty()
    }

    var showLegendBottomSheet by rememberSaveable { mutableStateOf(false) }

    if (showLegendBottomSheet) {
        CalendarLegendBottomSheet(
            onDismiss = { showLegendBottomSheet = false }
        )
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        AnimatedListItem(index = 0) {
            MonthlyCalendarView(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                today = today,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onDaySelected = onSelectDate,
                onOpenLegend = { showLegendBottomSheet = true },
                lessonsByDate = lessonsByDate,
                homeworkByDate = homeworkByDate,
                locale = currentLocale
            )
        }
        AnimatedListItem(index = 1) {
            DayDetailsSection(
                selectedDate = selectedDate,
                lessons = selectedDateLessons,
                homework = selectedDateHomework,
                today = today,
                locale = currentLocale,
                currencyCode = currencyCode,
                onLessonActionClick = onLogLessonClick,
                onLessonMarkAsPaidClick = onLessonMarkAsPaidClick,
                onRevertPaymentClick = onRevertPaymentClick,
                onToggleHomeworkStatus = onToggleHomeworkStatus,
                onHomeworkDetailsClick = onHomeworkDetailsClick
            )
        }
    }
}


@Composable
private fun MonthlyCalendarView(
    modifier: Modifier = Modifier,
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDaySelected: (LocalDate) -> Unit,
    onOpenLegend: () -> Unit,
    lessonsByDate: Map<LocalDate, List<Lesson>>,
    homeworkByDate: Map<LocalDate, List<Homework>>,
    locale: Locale
) {
    val monthName = remember(currentMonth, locale) {
        currentMonth.month.getDisplayName(TextStyle.FULL, locale)
    }
    val formattedMonthName = monthName.replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase(locale) else char.toString()
    }
    val monthTitle = tullabStringResource(
        id = R.string.calendar_month_year_title,
        formattedMonthName,
        currentMonth.year
    )

    val daysOfWeek = remember(locale) { daysOfWeekFromLocale(locale) }
    val weekFields = remember(locale) { WeekFields.of(locale) }
    val daysInWeek = DayOfWeek.values().size
    val firstOfMonth = remember(currentMonth) { currentMonth.atDay(1) }
    val leadingDays = ((firstOfMonth.dayOfWeek.value - weekFields.firstDayOfWeek.value) + daysInWeek) % daysInWeek
    val totalDays = currentMonth.lengthOfMonth()
    val trailingDays = (daysInWeek - (leadingDays + totalDays) % daysInWeek) % daysInWeek
    val calendarDays = remember(currentMonth, leadingDays, trailingDays) {
        buildList {
            repeat(leadingDays) { add(null) }
            repeat(totalDays) { dayIndex -> add(currentMonth.atDay(dayIndex + 1)) }
            repeat(trailingDays) { add(null) }
        }
    }
    val calendarRows = remember(calendarDays) {
        calendarDays.chunked(daysInWeek)
    }

    Column(modifier = modifier) {
        CalendarHeader(
            monthTitle = monthTitle,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth,
            onOpenLegend = onOpenLegend
        )
        Spacer(modifier = Modifier.height(16.dp))
        DaysOfWeekRow(daysOfWeek = daysOfWeek, locale = locale)
        Spacer(modifier = Modifier.height(12.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            calendarRows.forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    week.forEach { date ->
                        if (date == null) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            )
                        } else {
                            val lessonsForDate = lessonsByDate[date].orEmpty()
                            val homeworkForDate = homeworkByDate[date].orEmpty()
                            CalendarDayCell(
                                date = date,
                                isSelected = selectedDate == date,
                                dayIndicators = resolveDayIndicators(
                                    lessons = lessonsForDate,
                                    homework = homeworkForDate,
                                    date = date,
                                    today = today
                                ),
                                onClick = { onDaySelected(date) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarHeader(
    monthTitle: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onOpenLegend: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.Outlined.ChevronLeft,
                contentDescription = tullabStringResource(id = R.string.calendar_previous_month_content_description)
            )
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = monthTitle,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = onOpenLegend,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = tullabStringResource(id = R.string.calendar_legend_title),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = tullabStringResource(id = R.string.calendar_next_month_content_description)
            )
        }
    }
}

@Composable
private fun DaysOfWeekRow(
    daysOfWeek: List<DayOfWeek>,
    locale: Locale
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        daysOfWeek.forEach { day ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.getDisplayName(TextStyle.SHORT, locale).uppercase(locale),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    isSelected: Boolean,
    dayIndicators: DayIndicators,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = MaterialTheme.shapes.medium
    
    // Animated background color
    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        } else {
            Color.Transparent
        },
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 150),
        label = "dayCellBackground"
    )
    
    // Animated text color
    val animatedTextColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 150),
        label = "dayCellText"
    )
    
    // Scale animation for selection
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = TullabAnimationSpecs.pressSpec,
        label = "dayCellScale"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .background(animatedBackgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = animatedTextColor,
                textAlign = TextAlign.Center
            )
            if (dayIndicators.lessonColor != null || dayIndicators.homeworkColor != null) {
                Spacer(modifier = Modifier.height(6.dp))
                if (dayIndicators.lessonColor != null && dayIndicators.homeworkColor != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(dayIndicators.lessonColor)
                        )
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(dayIndicators.homeworkColor)
                        )
                    }
                } else if (dayIndicators.lessonColor != null) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(dayIndicators.lessonColor)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(dayIndicators.homeworkColor!!)
                    )
                }
            }
        }
    }
}

// Internal classes mapped in CalendarStatusResolver.kt

private data class LessonStatusDisplay(
    val text: String,
    val color: Color
)

@Composable
private fun DayDetailsSection(
    selectedDate: LocalDate,
    lessons: List<Lesson>,
    homework: List<Homework>,
    today: LocalDate,
    locale: Locale,
    currencyCode: String,
    onLessonActionClick: (Lesson) -> Unit,
    onLessonMarkAsPaidClick: (Lesson) -> Unit,
    onRevertPaymentClick: (Lesson) -> Unit,
    onToggleHomeworkStatus: (Homework) -> Unit,
    onHomeworkDetailsClick: (Homework) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember(locale) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale)
    }
    val formattedSelectedDate = remember(selectedDate, dateFormatter) {
        selectedDate.format(dateFormatter)
    }
    val lessonsSorted = remember(lessons) {
        lessons.sortedBy { lesson -> lesson.date }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = tullabStringResource(
                id = R.string.calendar_day_details_title,
                formattedSelectedDate
            ),
            style = MaterialTheme.typography.titleMedium
        )
        if (lessonsSorted.isEmpty() && homework.isEmpty()) {
            Card(
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = tullabStringResource(id = R.string.calendar_no_lessons_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                lessonsSorted.forEach { lesson ->
                    LessonDetailCard(
                        lesson = lesson,
                        dateFormatter = dateFormatter,
                        today = today,
                        locale = locale,
                        currencyCode = currencyCode,
                        onActionClick = onLessonActionClick,
                        onMarkAsPaidClick = onLessonMarkAsPaidClick,
                        onRevertPaymentClick = onRevertPaymentClick
                    )
                }
                homework.forEach { h ->
                    HomeworkDetailCard(
                        homework = h,
                        dateFormatter = dateFormatter,
                        today = today,
                        locale = locale,
                        onToggleStatus = { onToggleHomeworkStatus(h) },
                        onDetailsClick = { onHomeworkDetailsClick(h) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeworkDetailCard(
    homework: Homework,
    dateFormatter: DateTimeFormatter,
    today: LocalDate,
    locale: Locale,
    onToggleStatus: () -> Unit,
    onDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dueDate = remember(homework.dueDate) {
        Instant.ofEpochMilli(homework.dueDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
    
    val isOverdue = homework.status == HomeworkStatus.PENDING && dueDate.isBefore(today)
    val effectiveStatus = if (isOverdue) HomeworkStatus.OVERDUE else homework.status
    
    val statusText = when (effectiveStatus) {
        HomeworkStatus.COMPLETED -> tullabStringResource(id = R.string.homework_status_completed)
        HomeworkStatus.OVERDUE -> tullabStringResource(id = R.string.homework_status_overdue)
        HomeworkStatus.CANCELLED -> tullabStringResource(id = R.string.homework_status_cancelled)
        else -> tullabStringResource(id = R.string.homework_status_pending)
    }
    
    val statusColor = when (effectiveStatus) {
        HomeworkStatus.COMPLETED -> StatusGreen
        HomeworkStatus.OVERDUE -> StatusRed
        HomeworkStatus.CANCELLED -> HomeworkGray
        else -> StatusBlue
    }

    val textDecoration = if (effectiveStatus == HomeworkStatus.CANCELLED) TextDecoration.LineThrough else TextDecoration.None
    val titleColor = if (effectiveStatus == HomeworkStatus.CANCELLED) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface

    Card(
        shape = MaterialTheme.shapes.large,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Assignment,
                    contentDescription = null,
                    tint = if (effectiveStatus == HomeworkStatus.CANCELLED) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = homework.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = textDecoration,
                    color = titleColor
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = tullabStringResource(
                        id = R.string.calendar_lesson_details_status,
                        statusText
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = statusColor
                )
                if (effectiveStatus == HomeworkStatus.OVERDUE) {
                    Surface(
                        color = StatusRedContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = tullabStringResource(id = R.string.homework_badge_overdue),
                            color = StatusRed,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            if (homework.description.isNotBlank()) {
                Text(
                    text = homework.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (effectiveStatus == HomeworkStatus.CANCELLED) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = textDecoration
                )
            }
            
            val notes = homework.performanceNotes
            if (!notes.isNullOrBlank()) {
                Text(
                    text = tullabStringResource(
                        id = R.string.calendar_lesson_details_notes,
                        notes
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (effectiveStatus == HomeworkStatus.CANCELLED) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (effectiveStatus != HomeworkStatus.CANCELLED) {
                    Button(
                        onClick = onToggleStatus,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        val labelRes = if (effectiveStatus == HomeworkStatus.COMPLETED) {
                            R.string.calendar_homework_action_mark_pending
                        } else {
                            R.string.calendar_homework_action_mark_complete
                        }
                        Text(text = tullabStringResource(id = labelRes))
                    }
                }

                OutlinedButton(
                    onClick = onDetailsClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = tullabStringResource(id = R.string.calendar_homework_action_details))
                }
            }
        }
    }
}

@Composable
private fun LessonDetailCard(
    lesson: Lesson,
    dateFormatter: DateTimeFormatter,
    today: LocalDate,
    locale: Locale,
    currencyCode: String,
    onActionClick: (Lesson) -> Unit,
    onMarkAsPaidClick: (Lesson) -> Unit,
    onRevertPaymentClick: (Lesson) -> Unit,
    modifier: Modifier = Modifier
) {
    val lessonDate = remember(lesson.date) {
        Instant.ofEpochMilli(lesson.date)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
    val formattedDate = remember(lessonDate, dateFormatter) {
        lessonDate.format(dateFormatter)
    }
    val statusDisplay = lessonStatusDisplay(lesson, lessonDate, today)
    val durationText = lesson.durationInHours?.let { duration ->
        formatDurationHours(duration)
    }
    val actionTextRes = if (
        lesson.status == LessonStatus.SCHEDULED && lessonDate.isBefore(today)
    ) {
        R.string.calendar_lesson_action_log_details
    } else {
        R.string.calendar_lesson_action_edit
    }

    Card(
        shape = MaterialTheme.shapes.large,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = tullabStringResource(
                    id = R.string.calendar_lesson_details_title,
                    formattedDate
                ),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = tullabStringResource(
                    id = R.string.calendar_lesson_details_status,
                    statusDisplay.text
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = statusDisplay.color
            )
            val pricingModeRes = remember(lesson.pricingMode) {
                if (lesson.pricingMode == PricingMode.FLAT_FEE) R.string.pricing_mode_flat_fee
                else R.string.pricing_mode_per_hour
            }
            Text(
                text = tullabStringResource(
                    id = R.string.calendar_lesson_details_pricing_mode,
                    tullabStringResource(id = pricingModeRes)
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (lesson.pricingMode != PricingMode.FLAT_FEE && durationText != null) {
                Text(
                    text = tullabStringResource(
                        id = R.string.calendar_lesson_details_duration,
                        durationText
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            val totalText = remember(lesson.calculatedValue, currencyCode) {
                formatCurrency(lesson.calculatedValue, currencyCode)
            }
            Text(
                text = tullabStringResource(
                    id = R.string.calendar_lesson_details_total,
                    totalText
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val paymentTimestamp = lesson.paymentTimestamp
            if (paymentTimestamp != null) {
                val paymentDateTimeFormatter = remember(locale) {
                    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT).withLocale(locale)
                }
                val formattedPaymentDate = remember(paymentTimestamp, paymentDateTimeFormatter) {
                    Instant.ofEpochMilli(paymentTimestamp)
                        .atZone(ZoneId.systemDefault())
                        .format(paymentDateTimeFormatter)
                }
                Text(
                    text = tullabStringResource(
                        id = R.string.calendar_lesson_details_paid_on,
                        formattedPaymentDate
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = StatusGreen
                )
            }
            val lessonNotes = lesson.notes
            if (!lessonNotes.isNullOrBlank()) {
                Text(
                    text = tullabStringResource(
                        id = R.string.calendar_lesson_details_notes,
                        lessonNotes
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = { onActionClick(lesson) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(text = tullabStringResource(id = actionTextRes))
            }
            if (lesson.status != LessonStatus.PAID) {
                Button(
                    onClick = { onMarkAsPaidClick(lesson) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StatusGreen,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = tullabStringResource(id = R.string.calendar_lesson_action_mark_as_paid))
                }
            } else {
                OutlinedButton(
                    onClick = { onRevertPaymentClick(lesson) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = tullabStringResource(id = R.string.calendar_lesson_action_revert_payment))
                }
            }
        }
    }
}

@Composable
private fun lessonStatusDisplay(
    lesson: Lesson,
    lessonDate: LocalDate,
    today: LocalDate
): LessonStatusDisplay {
    val statusTextRes = when (lesson.status) {
        LessonStatus.PAID -> R.string.calendar_status_paid
        LessonStatus.COMPLETED -> R.string.calendar_status_completed
        LessonStatus.SCHEDULED -> if (lessonDate.isBefore(today)) {
            R.string.calendar_status_scheduled_needs_logging
        } else {
            R.string.calendar_status_scheduled
        }
        LessonStatus.CANCELLED -> R.string.calendar_status_cancelled
    }
    val color = when (lesson.status) {
        LessonStatus.PAID -> StatusGreen
        LessonStatus.COMPLETED -> StatusYellow
        LessonStatus.SCHEDULED -> if (lessonDate.isBefore(today)) {
            StatusRed
        } else {
            StatusBlue
        }
        LessonStatus.CANCELLED -> HomeworkGray
    }
    return LessonStatusDisplay(
        text = tullabStringResource(id = statusTextRes),
        color = color
    )
}

private fun daysOfWeekFromLocale(locale: Locale): List<DayOfWeek> {
    val firstDay = WeekFields.of(locale).firstDayOfWeek
    val days = DayOfWeek.values()
    val firstIndex = firstDay.ordinal
    return List(days.size) { index ->
        days[(firstIndex + index) % days.size]
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarScreenPreview() {
    val zoneId = ZoneId.systemDefault()
    val today = LocalDate.now(zoneId)
    val previewLessons = listOf(
        Lesson(
            id = 1,
            studentId = 1,
            date = today.minusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli(),
            status = LessonStatus.SCHEDULED,
            durationInHours = null,
            notes = null,
            pricingMode = PricingMode.PER_HOUR,
            rateOrFee = 50.0
        ),
        Lesson(
            id = 2,
            studentId = 1,
            date = today.atStartOfDay(zoneId).toInstant().toEpochMilli(),
            status = LessonStatus.COMPLETED,
            durationInHours = 1.5,
            notes = "Worked on algebra problems.",
            pricingMode = PricingMode.PER_HOUR,
            rateOrFee = 50.0
        ),
        Lesson(
            id = 3,
            studentId = 1,
            date = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli(),
            status = LessonStatus.PAID,
            durationInHours = 2.0,
            notes = null,
            pricingMode = PricingMode.FLAT_FEE,
            rateOrFee = 80.0
        )
    )
    val selectedDate = today
    val currentMonth = YearMonth.from(selectedDate)
    TullabTheme {
        CalendarScreenContent(
            modifier = Modifier.fillMaxWidth(),
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            lessons = previewLessons,
            homework = emptyList(),
            currencyCode = "USD",
            onPreviousMonth = {},
            onNextMonth = {},
            onSelectDate = {},
            onLogLessonClick = {},
            onLessonMarkAsPaidClick = {},
            onRevertPaymentClick = {},
            onToggleHomeworkStatus = {},
            onHomeworkDetailsClick = {},
            onNavigateToBulkSchedule = {}
        )
    }
}
