package com.barutdev.kora.ui.screens.calendar

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.barutdev.kora.util.koraStringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import com.barutdev.kora.R
import com.barutdev.kora.domain.model.Lesson
import com.barutdev.kora.domain.model.LessonStatus
import com.barutdev.kora.ui.navigation.FabConfig
import com.barutdev.kora.ui.navigation.LocalKoraScaffoldController
import com.barutdev.kora.ui.navigation.ScreenScaffoldConfig
import com.barutdev.kora.ui.navigation.TopBarAction
import com.barutdev.kora.ui.navigation.TopBarConfig
import com.barutdev.kora.ui.screens.dashboard.components.LogLessonDialog
import com.barutdev.kora.ui.screens.common.StudentNameUiStatus
import com.barutdev.kora.ui.screens.common.deriveStudentNameUiStatus
import com.barutdev.kora.ui.theme.KoraTheme
import com.barutdev.kora.ui.theme.LocalLocale
import com.barutdev.kora.ui.theme.StatusBlue
import com.barutdev.kora.ui.theme.StatusGreen
import com.barutdev.kora.ui.theme.StatusRed
import com.barutdev.kora.ui.theme.StatusYellow
import com.barutdev.kora.ui.theme.StatusOrange
import com.barutdev.kora.ui.theme.StatusOrangeContainer
import com.barutdev.kora.ui.theme.HomeworkTeal
import com.barutdev.kora.ui.theme.HomeworkTealContainer
import com.barutdev.kora.ui.theme.HomeworkMagenta
import com.barutdev.kora.ui.theme.HomeworkMagentaContainer
import com.barutdev.kora.ui.theme.HomeworkGray
import com.barutdev.kora.ui.theme.HomeworkGrayContainer
import com.barutdev.kora.ui.theme.KoraAnimationSpecs
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.material3.Surface
import com.barutdev.kora.ui.components.AnimatedListItem
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
import com.barutdev.kora.domain.model.Homework
import com.barutdev.kora.domain.model.HomeworkStatus
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import androidx.compose.material.icons.outlined.Assignment


@Composable
fun CalendarScreen(
    onNavigateToStudentList: () -> Unit,
    onNavigateToHomework: (Int, Int) -> Unit,
    expectedStudentId: Int? = null,
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel(
        key = expectedStudentId?.let { "calendar-$it" } ?: "calendar-default"
    )
) {
    val scaffoldController = LocalKoraScaffoldController.current
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
    val calendarLabel = koraStringResource(id = R.string.calendar_title)
    val resolvedStudentName = studentName.takeIf {
        studentNameStatus == StudentNameUiStatus.Ready && it.isNotBlank()
    } ?: calendarLabel
    val topBarTitle = if (studentNameStatus == StudentNameUiStatus.Ready && studentName.isNotBlank()) {
        koraStringResource(
            id = R.string.calendar_top_bar_title,
            resolvedStudentName,
            calendarLabel
        )
    } else {
        calendarLabel
    }
    val navigateToListDescription = koraStringResource(
        id = R.string.top_bar_navigate_to_student_list_content_description
    )
    val addLessonDescription = koraStringResource(
        id = R.string.calendar_add_lesson_fab_content_description
    )
    val scheduledMessage = koraStringResource(id = R.string.calendar_lesson_scheduled_message)
    val containerColor = MaterialTheme.colorScheme.primary
    val contentColor = MaterialTheme.colorScheme.onPrimary

    LogLessonDialog(
        showDialog = isLogLessonDialogVisible,
        lesson = lessonToLog,
        onDismiss = viewModel::dismissLogLessonDialog,
        onComplete = { duration, notes ->
            viewModel.onLogLessonComplete(duration, notes)
        },
        onMarkNotDone = { notes ->
            viewModel.onLogLessonMarkNotDone(notes)
        }
    )

    LogLessonDialog(
        showDialog = pendingLessonForPayment != null,
        lesson = pendingLessonForPayment,
        onDismiss = viewModel::dismissMarkLessonAsPaidDialog,
        onComplete = { _, _ -> },
        onMarkNotDone = { _ -> },
        isMarkAsPaidMode = true,
        requiresFeePrompt = requiresFeePrompt,
        onMarkAsPaid = { durationStr, feeStr ->
            val duration = durationStr.trim().replace(',', '.').toDoubleOrNull()
            val fee = feeStr.trim().replace(',', '.').toDoubleOrNull()
            viewModel.onConfirmMarkLessonAsPaid(duration, fee)
        }
    )

    if (lessonToRevert != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = viewModel::dismissRevertDialog,
            title = { Text(text = koraStringResource(id = R.string.calendar_dialog_revert_payment_title)) },
            text = { Text(text = koraStringResource(id = R.string.calendar_dialog_revert_payment_message)) },
            confirmButton = {
                Button(onClick = viewModel::onConfirmRevertPayment) {
                    Text(text = koraStringResource(id = R.string.calendar_dialog_revert_payment_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissRevertDialog) {
                    Text(text = koraStringResource(id = R.string.dialog_action_cancel))
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
            )
        )
    }
    val fabConfig = remember(
        selectedDate,
        addLessonDescription,
        containerColor,
        contentColor,
        scheduledMessage,
        coroutineScope,
        viewModel,
        zoneId,
        snackbarHostState
    ) {
        FabConfig(
            icon = Icons.Filled.Add,
            contentDescription = addLessonDescription,
            onClick = {
                val epochMillis = selectedDate
                    .atStartOfDay(zoneId)
                    .toInstant()
                    .toEpochMilli()
                coroutineScope.launch {
                    viewModel.saveLesson(epochMillis)
                    snackbarHostState.showSnackbar(message = scheduledMessage)
                }
            },
            containerColor = containerColor,
            contentColor = contentColor
        )
    }
    ScreenScaffoldConfig(
        topBarConfig = topBarConfig,
        fabConfig = fabConfig
    )

    CalendarScreenContent(
        modifier = modifier.fillMaxSize(),
        currentMonth = currentMonth,
        selectedDate = selectedDate,
        lessons = lessons,
        homework = homework,
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
}

@Composable
private fun CalendarScreenContent(
    modifier: Modifier = Modifier,
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    lessons: List<Lesson>,
    homework: List<Homework>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onLogLessonClick: (Lesson) -> Unit,
    onLessonMarkAsPaidClick: (Lesson) -> Unit,
    onRevertPaymentClick: (Lesson) -> Unit,
    onToggleHomeworkStatus: (Homework) -> Unit,
    onHomeworkDetailsClick: (Homework) -> Unit
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
    val monthTitle = koraStringResource(
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
            onNextMonth = onNextMonth
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
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.Outlined.ChevronLeft,
                contentDescription = koraStringResource(id = R.string.calendar_previous_month_content_description)
            )
        }
        Text(
            text = monthTitle,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = koraStringResource(id = R.string.calendar_next_month_content_description)
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
        animationSpec = KoraAnimationSpecs.pressSpec,
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
                                .clip(CircleShape)
                                .background(dayIndicators.homeworkColor)
                        )
                    }
                } else {
                    val singleColor = dayIndicators.lessonColor ?: dayIndicators.homeworkColor!!
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(singleColor)
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
            text = koraStringResource(
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
                        text = koraStringResource(id = R.string.calendar_no_lessons_message),
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
        HomeworkStatus.COMPLETED -> koraStringResource(id = R.string.homework_status_completed)
        HomeworkStatus.OVERDUE -> koraStringResource(id = R.string.homework_status_overdue)
        HomeworkStatus.CANCELLED -> koraStringResource(id = R.string.homework_status_cancelled)
        else -> koraStringResource(id = R.string.homework_status_pending)
    }
    
    val statusColor = when (effectiveStatus) {
        HomeworkStatus.COMPLETED -> HomeworkTeal
        HomeworkStatus.OVERDUE -> HomeworkMagenta
        HomeworkStatus.CANCELLED -> HomeworkGray
        else -> StatusOrange
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
                    text = koraStringResource(
                        id = R.string.calendar_lesson_details_status,
                        statusText
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = statusColor
                )
                if (effectiveStatus == HomeworkStatus.OVERDUE) {
                    Surface(
                        color = HomeworkMagentaContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = koraStringResource(id = R.string.homework_badge_overdue),
                            color = HomeworkMagenta,
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
                    text = koraStringResource(
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
                        Text(text = koraStringResource(id = labelRes))
                    }
                }

                OutlinedButton(
                    onClick = onDetailsClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = koraStringResource(id = R.string.calendar_homework_action_details))
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
    val durationText = remember(lesson.durationInHours, locale) {
        lesson.durationInHours?.let { duration ->
            NumberFormat.getNumberInstance(locale).apply {
                maximumFractionDigits = 2
                minimumFractionDigits = 0
            }.format(duration)
        }
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
                text = koraStringResource(
                    id = R.string.calendar_lesson_details_title,
                    formattedDate
                ),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = koraStringResource(
                    id = R.string.calendar_lesson_details_status,
                    statusDisplay.text
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = statusDisplay.color
            )
            if (durationText != null) {
                Text(
                    text = koraStringResource(
                        id = R.string.calendar_lesson_details_duration,
                        durationText
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
                    text = koraStringResource(
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
                    text = koraStringResource(
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
                Text(text = koraStringResource(id = actionTextRes))
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
                    Text(text = koraStringResource(id = R.string.calendar_lesson_action_mark_as_paid))
                }
            } else {
                OutlinedButton(
                    onClick = { onRevertPaymentClick(lesson) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = koraStringResource(id = R.string.calendar_lesson_action_revert_payment))
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
        LessonStatus.CANCELLED -> StatusRed
    }
    return LessonStatusDisplay(
        text = koraStringResource(id = statusTextRes),
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
            notes = null
        ),
        Lesson(
            id = 2,
            studentId = 1,
            date = today.atStartOfDay(zoneId).toInstant().toEpochMilli(),
            status = LessonStatus.COMPLETED,
            durationInHours = 1.5,
            notes = "Worked on algebra problems."
        ),
        Lesson(
            id = 3,
            studentId = 1,
            date = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli(),
            status = LessonStatus.PAID,
            durationInHours = 2.0,
            notes = null
        )
    )
    val selectedDate = today
    val currentMonth = YearMonth.from(selectedDate)
    KoraTheme {
        CalendarScreenContent(
            modifier = Modifier.fillMaxWidth(),
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            lessons = previewLessons,
            homework = emptyList(),
            onPreviousMonth = {},
            onNextMonth = {},
            onSelectDate = {},
            onLogLessonClick = {},
            onLessonMarkAsPaidClick = {},
            onRevertPaymentClick = {},
            onToggleHomeworkStatus = {},
            onHomeworkDetailsClick = {}
        )
    }
}
