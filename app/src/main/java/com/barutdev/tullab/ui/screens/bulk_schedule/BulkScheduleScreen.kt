package com.barutdev.tullab.ui.screens.bulk_schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.barutdev.tullab.R
import com.barutdev.tullab.domain.model.BulkScheduleMode
import com.barutdev.tullab.ui.screens.bulk_schedule.components.CalendarGridSelector
import com.barutdev.tullab.ui.screens.bulk_schedule.components.DayTimeChipList
import com.barutdev.tullab.ui.screens.bulk_schedule.components.PastDateWarningDialog
import com.barutdev.tullab.util.tullabStringResource
import com.barutdev.tullab.util.tullabPluralResource
import androidx.compose.foundation.layout.width
import androidx.compose.ui.platform.LocalContext
import java.time.LocalTime
import java.time.YearMonth
import com.barutdev.tullab.ui.navigation.LocalTullabScaffoldController
import com.barutdev.tullab.ui.navigation.TopBarConfig
import com.barutdev.tullab.ui.navigation.TopBarAction
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import com.barutdev.tullab.ui.theme.LocalLocale
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkScheduleScreen(
    onNavigateBack: () -> Unit,
    onNavigateBackWithResult: (createdCount: Int, skippedCount: Int, lessonIds: IntArray) -> Unit = { _, _, _ -> onNavigateBack() },
    modifier: Modifier = Modifier,
    viewModel: BulkScheduleViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = com.barutdev.tullab.ui.navigation.LocalTullabScaffoldController.current.snackbarHostState

    val zoneId = remember { ZoneId.systemDefault() }
    val lessonsByDate = remember(state.existingLessons, zoneId) {
        state.existingLessons.groupBy { lesson ->
            Instant.ofEpochMilli(lesson.date)
                .atZone(zoneId)
                .toLocalDate()
        }
    }
    val homeworkByDate = remember(state.existingHomework) {
        state.existingHomework.groupBy { h ->
            Instant.ofEpochMilli(h.dueDate)
                .atZone(java.time.ZoneOffset.UTC)
                .toLocalDate()
        }
    }

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    val title = tullabStringResource(R.string.bulk_schedule_title)
    val topBarConfig = remember(title, onNavigateBack) {
        TopBarConfig(
            title = title,
            navigationIcon = TopBarAction(
                icon = androidx.compose.material.icons.Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = null,
                onClick = onNavigateBack
            )
        )
    }
    com.barutdev.tullab.ui.navigation.ScreenScaffoldConfig(topBarConfig = topBarConfig)
    
    val undoActionLabel = tullabStringResource(R.string.bulk_schedule_undo_action)

    val currentMessage = state.snackbarMessage
    
    val successMessageText = if (currentMessage is SnackbarState.Success) {
        val createdStr = tullabPluralResource(id = R.plurals.bulk_schedule_success_created, quantity = currentMessage.createdCount, currentMessage.createdCount)
        val skippedStr = if (currentMessage.skippedCount > 0) " " + tullabPluralResource(id = R.plurals.bulk_schedule_success_skipped, quantity = currentMessage.skippedCount, currentMessage.skippedCount) else ""
        createdStr + skippedStr
    } else ""
    
    val errorMessageText = if (currentMessage is SnackbarState.Error) tullabStringResource(currentMessage.messageResId) else ""
    val undoMessageText = if (currentMessage is SnackbarState.UndoSuccess) tullabStringResource(R.string.bulk_schedule_undo_success_toast) else ""

    LaunchedEffect(currentMessage) {
        currentMessage?.let { message ->
            when (message) {
                is SnackbarState.Success -> {
                    onNavigateBackWithResult(
                        message.createdCount,
                        message.skippedCount,
                        message.lessonIds.toIntArray()
                    )
                }
                is SnackbarState.Error -> {
                    snackbarHostState.showSnackbar(message = errorMessageText)
                    viewModel.onEvent(BulkScheduleEvent.SnackbarDismissed)
                }
                is SnackbarState.UndoSuccess -> {
                    snackbarHostState.showSnackbar(message = undoMessageText)
                    viewModel.onEvent(BulkScheduleEvent.SnackbarDismissed)
                }
            }
        }
    }

    PastDateWarningDialog(
        showDialog = state.showPastDateWarning,
        onDismiss = { viewModel.onEvent(BulkScheduleEvent.DismissWarningDialog) },
        onConfirm = { viewModel.onEvent(BulkScheduleEvent.ProceedWithPastDates) }
    )

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            TabRow(selectedTabIndex = if (state.draft.mode == BulkScheduleMode.CALENDAR_GRID) 0 else 1) {
                Tab(
                    selected = state.draft.mode == BulkScheduleMode.CALENDAR_GRID,
                    onClick = { viewModel.onEvent(BulkScheduleEvent.OnModeChanged(BulkScheduleMode.CALENDAR_GRID)) },
                    text = { Text(tullabStringResource(R.string.bulk_schedule_tab_calendar_grid)) }
                )
                Tab(
                    selected = state.draft.mode == BulkScheduleMode.WEEKLY_ROUTINE,
                    onClick = { viewModel.onEvent(BulkScheduleEvent.OnModeChanged(BulkScheduleMode.WEEKLY_ROUTINE)) },
                    text = { Text(tullabStringResource(R.string.bulk_schedule_tab_weekly_routine)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.draft.mode == BulkScheduleMode.CALENDAR_GRID) {
                com.barutdev.tullab.ui.screens.bulk_schedule.components.CalendarGridSelector(
                    currentMonth = currentMonth,
                    selectedDates = state.draft.selectedDates,
                    onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                    onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
                    onDateToggled = { viewModel.onEvent(BulkScheduleEvent.OnDateToggled(it)) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                    lessonsByDate = lessonsByDate,
                    homeworkByDate = homeworkByDate
                )

                Spacer(modifier = Modifier.height(24.dp))
                DefaultStartTimePickerBlock(
                    defaultStartTime = state.draft.defaultStartTime,
                    onTimeSelected = { viewModel.onEvent(BulkScheduleEvent.OnDefaultStartTimeChanged(it)) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                if (state.draft.selectedDates.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = tullabStringResource(R.string.bulk_schedule_customize_day_times),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DayTimeChipList(
                        selectedDates = state.draft.selectedDates,
                        defaultStartTime = state.draft.defaultStartTime,
                        customDayTimes = state.draft.customDayTimes,
                        onCustomTimeSelected = { date, time ->
                            viewModel.onEvent(BulkScheduleEvent.OnCustomDayTimeChanged(date, time))
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                com.barutdev.tullab.ui.screens.bulk_schedule.components.WeeklyRoutineSelector(
                    selectedDaysOfWeek = state.draft.selectedDaysOfWeek,
                    onDayOfWeekToggled = { viewModel.onEvent(BulkScheduleEvent.OnDayOfWeekToggled(it)) },
                    routineStartDate = state.draft.routineStartDate,
                    onStartDateChanged = { viewModel.onEvent(BulkScheduleEvent.OnRoutineStartDateChanged(it)) },
                    endCondition = state.draft.endCondition,
                    onEndConditionTypeChanged = { viewModel.onEvent(BulkScheduleEvent.OnRoutineEndConditionChanged(it)) },
                    onEndDateChanged = { viewModel.onEvent(BulkScheduleEvent.OnRoutineEndDateChanged(it)) },
                    targetCountInput = state.targetCountInput,
                    onTargetCountChanged = { viewModel.onEvent(BulkScheduleEvent.OnRoutineTargetCountChanged(it)) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                    timePickerSlot = {
                        DefaultStartTimePickerBlock(
                            defaultStartTime = state.draft.defaultStartTime,
                            onTimeSelected = { viewModel.onEvent(BulkScheduleEvent.OnDefaultStartTimeChanged(it)) }
                        )
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            if (state.isCapReached) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = tullabStringResource(R.string.bulk_schedule_max_limit_error),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            com.barutdev.tullab.ui.screens.bulk_schedule.components.BulkSchedulePricingSection(
                useCustomRate = state.draft.useCustomRate,
                onUseCustomRateToggled = { viewModel.onEvent(BulkScheduleEvent.OnUseCustomRateToggled(it)) },
                customRateInput = state.customRateInput,
                onCustomRateChanged = { viewModel.onEvent(BulkScheduleEvent.OnCustomRateChanged(it)) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bottom Bar (Confirm Button)
        Column(modifier = Modifier.padding(16.dp)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                val candidateCount = state.previewCandidates?.size ?: 0
                val skippedCount = state.previewSkippedCount
                val validCount = candidateCount - skippedCount
                val allConflicts = candidateCount > 0 && validCount == 0
                
                if (candidateCount > 0) {
                    val formatter = remember { java.text.NumberFormat.getCurrencyInstance() }
                    val hourlyRateStr = state.draft.customRate?.let { formatter.format(it) } ?: "Default"
                    
                    val previewText = if (skippedCount > 0) {
                        tullabStringResource(R.string.bulk_schedule_preview_count_with_skipped, candidateCount, skippedCount, hourlyRateStr)
                    } else {
                        tullabStringResource(R.string.bulk_schedule_preview_count_valid, candidateCount, hourlyRateStr)
                    }
                    Text(
                        text = previewText, 
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (state.isCapReached) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                Button(
                    onClick = { viewModel.onEvent(BulkScheduleEvent.ConfirmSchedule) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = candidateCount > 0 && !state.isCapReached && !allConflicts
                ) {
                    val buttonText = if (candidateCount > 0) {
                        tullabStringResource(R.string.bulk_schedule_confirm_button, candidateCount)
                    } else {
                        tullabStringResource(R.string.bulk_schedule_confirm_button_default)
                    }
                    Text(buttonText)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultStartTimePickerBlock(
    defaultStartTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDefaultTimePicker by remember { mutableStateOf(false) }
    if (showDefaultTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = defaultStartTime.hour,
            initialMinute = defaultStartTime.minute,
            is24Hour = android.text.format.DateFormat.is24HourFormat(androidx.compose.ui.platform.LocalContext.current)
        )
        
        AlertDialog(
            onDismissRequest = { showDefaultTimePicker = false },
            title = { Text(tullabStringResource(R.string.bulk_schedule_time_picker_title)) },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    val time = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    onTimeSelected(time)
                    showDefaultTimePicker = false
                }) {
                    Text(tullabStringResource(R.string.dialog_action_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDefaultTimePicker = false }) {
                    Text(tullabStringResource(R.string.dialog_action_cancel))
                }
            }
        )
    }
    
    Column(modifier = modifier) {
        Text(
            text = tullabStringResource(R.string.bulk_schedule_default_start_time),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        val locale = LocalLocale.current
        val timeFormatter = remember(locale) {
            DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)
        }
        
        OutlinedButton(onClick = { showDefaultTimePicker = true }) {
            Text(defaultStartTime.format(timeFormatter))
        }
    }
}

