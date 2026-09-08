package com.barutdev.tullab.ui.screens.dashboard.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.barutdev.tullab.R
import com.barutdev.tullab.domain.model.Lesson
import com.barutdev.tullab.domain.model.LessonStatus
import com.barutdev.tullab.domain.model.PricingMode
import com.barutdev.tullab.ui.components.TullabHapticFeedbackType
import com.barutdev.tullab.ui.components.rememberTullabHapticFeedback
import com.barutdev.tullab.ui.theme.LocalLocale
import com.barutdev.tullab.util.formatCurrency
import com.barutdev.tullab.util.getCurrencySymbol
import com.barutdev.tullab.util.tullabStringResource
import com.barutdev.tullab.util.formatLessonStartTime
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogLessonDialog(
    showDialog: Boolean,
    lesson: Lesson?,
    existingLessons: List<Lesson> = emptyList(),
    onDismiss: () -> Unit,
    onSave: ((duration: String, notes: String, pricingMode: PricingMode, rateOrFee: String, isCompleted: Boolean, dateMillis: Long) -> Unit)? = null,
    onComplete: ((duration: String, notes: String, pricingMode: PricingMode, rateOrFee: String) -> Unit)? = null,
    onMarkNotDone: ((notes: String, dateMillis: Long) -> Unit)? = null,
    onDelete: ((lesson: Lesson) -> Unit)? = null,
    isMarkAsPaidMode: Boolean = false,
    requiresFeePrompt: Boolean = false,
    onMarkAsPaid: ((duration: String, customFee: String) -> Unit)? = null,
    onSaveScheduled: ((duration: String, notes: String, pricingMode: PricingMode, rateOrFee: String) -> Unit)? = null,
    isPastScheduled: Boolean = false,
    currencyCode: String = "USD",
    onSaveStatusAndDetails: ((
        duration: String,
        notes: String,
        pricingMode: PricingMode,
        rateOrFeeInput: String,
        statusChoice: LogLessonStatusChoice,
        dateMillis: Long
    ) -> Unit)? = null
) {
    if (!showDialog || lesson == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val locale = LocalLocale.current
    val haptics = rememberTullabHapticFeedback()

    val conflictingDates = remember(existingLessons, lesson.id, lesson.studentId) {
        getConflictingLessonDates(
            existingLessons = existingLessons,
            currentLessonId = lesson.id,
            studentId = lesson.studentId
        )
    }

    var selectedDateMillis by remember(lesson.id, lesson.date) {
        mutableStateOf(lesson.date)
    }

    val isDateConflict by remember(conflictingDates, selectedDateMillis) {
        derivedStateOf {
            isLessonDateConflicting(
                dateMillis = selectedDateMillis,
                conflictingDates = conflictingDates
            )
        }
    }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    val formatter = remember(locale) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
    }
    val formattedDate = remember(selectedDateMillis, formatter) {
        Instant.ofEpochMilli(selectedDateMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(formatter)
    }

    val today = remember { LocalDate.now(ZoneId.systemDefault()) }
    val lessonDate = remember(selectedDateMillis) {
        Instant.ofEpochMilli(selectedDateMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
    val isPastLesson = remember(lessonDate, today) {
        lessonDate.isBefore(today)
    }

    val currencySymbol = remember(currencyCode, locale) {
        getCurrencySymbol(currencyCode, locale)
    }

    val initialDurationStr = remember(lesson) {
        lesson.durationInHours?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: ""
    }
    var duration by remember(lesson) { mutableStateOf(initialDurationStr) }

    val initialNotesStr = remember(lesson) { lesson.notes ?: "" }
    var notes by remember(lesson) { mutableStateOf(initialNotesStr) }

    var customFee by rememberSaveable(lesson.id) {
        mutableStateOf("")
    }

    val initialPricingMode = remember(lesson) { lesson.pricingMode }
    var pricingMode by remember(lesson) { mutableStateOf(initialPricingMode) }

    val initialRateOrFeeStr = remember(lesson) {
        lesson.rateOrFee.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }
    }
    var rateOrFeeInput by remember(lesson) {
        mutableStateOf(initialRateOrFeeStr)
    }

    // Smart default status:
    // If existing lesson has explicit completed or cancelled, respect it.
    // If scheduled: if strictly past (date < today) default to COMPLETED ("Yapıldı"),
    // if today or future (date >= today) default to SCHEDULED ("Planlandı").
    val initialStatusChoice = remember(lesson, today) {
        resolveInitialStatusChoice(
            lessonStatus = lesson.status,
            lessonDateMillis = lesson.date,
            today = today
        )
    }
    var statusChoice by remember(lesson.id, initialStatusChoice) {
        mutableStateOf(initialStatusChoice)
    }

    val isDataChanged by remember(
        lesson,
        selectedDateMillis,
        initialDurationStr,
        initialNotesStr,
        initialPricingMode,
        initialRateOrFeeStr,
        initialStatusChoice
    ) {
        derivedStateOf {
            val initialZdt = Instant.ofEpochMilli(lesson.date).atZone(ZoneId.systemDefault())
            val currentZdt = Instant.ofEpochMilli(selectedDateMillis).atZone(ZoneId.systemDefault())
            val isDateTimeChanged = initialZdt.toLocalDate() != currentZdt.toLocalDate() ||
                initialZdt.hour != currentZdt.hour ||
                initialZdt.minute != currentZdt.minute

            duration != initialDurationStr ||
                notes != initialNotesStr ||
                pricingMode != initialPricingMode ||
                rateOrFeeInput != initialRateOrFeeStr ||
                statusChoice != initialStatusChoice ||
                selectedDateMillis != lesson.date ||
                isDateTimeChanged
        }
    }

    var showPastWarning by remember { mutableStateOf(false) }

    val isSaveEnabled by remember(
        isMarkAsPaidMode,
        requiresFeePrompt,
        selectedDateMillis,
        isDateConflict
    ) {
        derivedStateOf {
            isDialogSaveEnabled(
                durationStr = duration,
                rateOrFeeInput = rateOrFeeInput,
                pricingMode = pricingMode,
                isMarkAsPaidMode = isMarkAsPaidMode,
                requiresFeePrompt = requiresFeePrompt,
                customFeeStr = customFee,
                isDataChanged = isDataChanged,
                statusChoice = statusChoice,
                isDateConflict = isDateConflict
            )
        }
    }

    fun resetInputs() {
        duration = lesson.durationInHours?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: ""
        notes = lesson.notes.orEmpty()
        customFee = ""
        pricingMode = lesson.pricingMode
        rateOrFeeInput = lesson.rateOrFee.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }
        statusChoice = initialStatusChoice
        selectedDateMillis = lesson.date
        showTimePicker = false
        showDatePicker = false
        showDeleteConfirmationDialog = false
    }

    val onDismissAndReset = {
        showPastWarning = false
        resetInputs()
        onDismiss()
    }

    val performSave = {
        when {
            isMarkAsPaidMode -> {
                onMarkAsPaid?.invoke(duration, customFee)
            }
            onSaveStatusAndDetails != null -> {
                onSaveStatusAndDetails(duration, notes, pricingMode, rateOrFeeInput, statusChoice, selectedDateMillis)
            }
            statusChoice == LogLessonStatusChoice.CANCELLED -> {
                if (onMarkNotDone != null) {
                    onMarkNotDone(notes, selectedDateMillis)
                } else if (onSave != null) {
                    onSave(duration, notes, pricingMode, rateOrFeeInput, false, selectedDateMillis)
                } else {
                    onComplete?.invoke(duration, notes, pricingMode, rateOrFeeInput)
                }
            }
            statusChoice == LogLessonStatusChoice.COMPLETED -> {
                if (onSave != null) {
                    onSave(duration, notes, pricingMode, rateOrFeeInput, true, selectedDateMillis)
                } else {
                    onComplete?.invoke(duration, notes, pricingMode, rateOrFeeInput)
                }
            }
            else -> { // SCHEDULED
                if (onSave != null) {
                    onSave(duration, notes, pricingMode, rateOrFeeInput, false, selectedDateMillis)
                } else if (onSaveScheduled != null) {
                    onSaveScheduled(duration, notes, pricingMode, rateOrFeeInput)
                } else {
                    onComplete?.invoke(duration, notes, pricingMode, rateOrFeeInput)
                }
            }
        }
    }

    if (showDeleteConfirmationDialog && lesson != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = {
                Text(text = tullabStringResource(id = R.string.lesson_delete_confirmation_title))
            },
            text = {
                Text(text = tullabStringResource(id = R.string.lesson_delete_confirmation_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmationDialog = false
                        onDelete?.invoke(lesson)
                    }
                ) {
                    Text(
                        text = tullabStringResource(id = R.string.dialog_action_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmationDialog = false }) {
                    Text(text = tullabStringResource(id = R.string.dialog_action_cancel))
                }
            }
        )
    }

    if (showTimePicker) {
        val initialLocalTime = remember(selectedDateMillis) {
            Instant.ofEpochMilli(selectedDateMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
        }
        val context = LocalContext.current
        val is24Hour = remember { android.text.format.DateFormat.is24HourFormat(context) }
        val timePickerState = rememberTimePickerState(
            initialHour = initialLocalTime.hour,
            initialMinute = initialLocalTime.minute,
            is24Hour = is24Hour
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(text = tullabStringResource(id = R.string.lesson_edit_time_picker_title)) },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val currentZonedDateTime = Instant.ofEpochMilli(selectedDateMillis)
                            .atZone(ZoneId.systemDefault())
                        val newZonedDateTime = currentZonedDateTime
                            .withHour(timePickerState.hour)
                            .withMinute(timePickerState.minute)
                            .withSecond(0)
                            .withNano(0)
                        selectedDateMillis = newZonedDateTime.toInstant().toEpochMilli()
                        showTimePicker = false
                    }
                ) {
                    Text(tullabStringResource(id = R.string.dialog_action_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(tullabStringResource(id = R.string.dialog_action_cancel))
                }
            }
        )
    }

    if (showDatePicker) {
        val initialDatePickerMillis = remember(selectedDateMillis) {
            Instant.ofEpochMilli(selectedDateMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .atStartOfDay(ZoneId.of("UTC"))
                .toInstant()
                .toEpochMilli()
        }
        val selectableDates = remember(conflictingDates) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val date = Instant.ofEpochMilli(utcTimeMillis)
                        .atZone(ZoneId.of("UTC"))
                        .toLocalDate()
                    return !conflictingDates.contains(date)
                }

                override fun isSelectableYear(year: Int): Boolean = true
            }
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDatePickerMillis,
            selectableDates = selectableDates
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                val isPickedDateValid = datePickerState.selectedDateMillis?.let { pickedUtcMillis ->
                    val pickedLocalDate = Instant.ofEpochMilli(pickedUtcMillis)
                        .atZone(ZoneId.of("UTC"))
                        .toLocalDate()
                    !conflictingDates.contains(pickedLocalDate)
                } ?: false

                TextButton(
                    enabled = isPickedDateValid,
                    onClick = {
                        datePickerState.selectedDateMillis?.let { pickedUtcMillis ->
                            val pickedLocalDate = Instant.ofEpochMilli(pickedUtcMillis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            val currentZonedDateTime = Instant.ofEpochMilli(selectedDateMillis)
                                .atZone(ZoneId.systemDefault())
                            val updatedZonedDateTime = pickedLocalDate
                                .atTime(currentZonedDateTime.hour, currentZonedDateTime.minute)
                                .atZone(ZoneId.systemDefault())
                            selectedDateMillis = updatedZonedDateTime.toInstant().toEpochMilli()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(text = tullabStringResource(id = R.string.dialog_action_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(text = tullabStringResource(id = R.string.dialog_action_cancel))
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = tullabStringResource(id = R.string.lesson_edit_date_picker_title),
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            )
        }
    }

    if (showPastWarning) {
        AlertDialog(
            onDismissRequest = { showPastWarning = false },
            title = { Text(tullabStringResource(id = R.string.dashboard_log_lesson_save_past_warning_title)) },
            text = { Text(tullabStringResource(id = R.string.dashboard_log_lesson_save_past_warning_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showPastWarning = false
                    performSave()
                }) {
                    Text(tullabStringResource(id = R.string.dashboard_log_lesson_save_past_warning_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPastWarning = false }) {
                    Text(tullabStringResource(id = R.string.dialog_action_cancel))
                }
            }
        )
    }

    val parsedDuration = parseDurationDecimal(duration)
    val isDurationEntered = duration.trim().isNotEmpty()
    val isDurationValid = parsedDuration != null && parsedDuration > 0.0
    val isDurationRequired = isMarkAsPaidMode || (statusChoice == LogLessonStatusChoice.COMPLETED && pricingMode == PricingMode.PER_HOUR)

    val durationErrorText = when {
        isDurationEntered && !isDurationValid -> tullabStringResource(id = R.string.lesson_duration_invalid_error)
        !isDurationEntered && isDurationRequired -> tullabStringResource(id = R.string.lesson_duration_required_for_completed)
        else -> null
    }
    val isDurationError = durationErrorText != null

    ModalBottomSheet(
        onDismissRequest = onDismissAndReset,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header: Title and delete button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isMarkAsPaidMode) {
                            tullabStringResource(id = R.string.calendar_lesson_action_mark_as_paid)
                        } else {
                            tullabStringResource(id = R.string.dashboard_log_lesson_dialog_title)
                        },
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    if (!isMarkAsPaidMode && onDelete != null) {
                        IconButton(onClick = { showDeleteConfirmationDialog = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = tullabStringResource(id = R.string.lesson_dialog_delete_content_description),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // Status: M3 SingleChoiceSegmentedButtonRow
                if (!isMarkAsPaidMode) {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SegmentedButton(
                            selected = statusChoice == LogLessonStatusChoice.SCHEDULED,
                            onClick = {
                                if (statusChoice != LogLessonStatusChoice.SCHEDULED) {
                                    haptics.perform(TullabHapticFeedbackType.SEGMENT_PULSE)
                                }
                                statusChoice = LogLessonStatusChoice.SCHEDULED
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                            enabled = lesson.status != LessonStatus.PAID
                        ) {
                            Text(
                                text = tullabStringResource(id = R.string.lesson_status_choice_scheduled),
                                maxLines = 1
                            )
                        }
                        SegmentedButton(
                            selected = statusChoice == LogLessonStatusChoice.COMPLETED,
                            onClick = {
                                if (statusChoice != LogLessonStatusChoice.COMPLETED) {
                                    haptics.perform(TullabHapticFeedbackType.SEGMENT_PULSE)
                                }
                                statusChoice = LogLessonStatusChoice.COMPLETED
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                            enabled = lesson.status != LessonStatus.PAID
                        ) {
                            Text(
                                text = tullabStringResource(id = R.string.lesson_status_choice_completed),
                                maxLines = 1
                            )
                        }
                        SegmentedButton(
                            selected = statusChoice == LogLessonStatusChoice.CANCELLED,
                            onClick = {
                                if (statusChoice != LogLessonStatusChoice.CANCELLED) {
                                    haptics.perform(TullabHapticFeedbackType.SEGMENT_PULSE)
                                }
                                statusChoice = LogLessonStatusChoice.CANCELLED
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                            enabled = lesson.status != LessonStatus.PAID,
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MaterialTheme.colorScheme.errorContainer,
                                activeContentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Text(
                                text = tullabStringResource(id = R.string.lesson_status_choice_not_done),
                                maxLines = 1
                            )
                        }
                    }
                }

                // Date & Time Selectors Row (50/50 matching Duration & Rate fields)
                val isLessonPaid = lesson.status == LessonStatus.PAID
                val isDateEditable = !isMarkAsPaidMode && !isLessonPaid
                val isTimeEditable = !isMarkAsPaidMode && !isLessonPaid
                val formattedStartTime = formatLessonStartTime(selectedDateMillis)
                val dateSelectorColors = if (isDateConflict) {
                    OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.error,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.error,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.error,
                        disabledLabelColor = MaterialTheme.colorScheme.error,
                        disabledSupportingTextColor = MaterialTheme.colorScheme.error,
                        disabledContainerColor = Color.Transparent
                    )
                } else if (isDateEditable) {
                    OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledContainerColor = Color.Transparent
                    )
                } else {
                    OutlinedTextFieldDefaults.colors(
                        disabledContainerColor = Color.Transparent
                    )
                }

                val timeSelectorColors = if (isTimeEditable) {
                    OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledContainerColor = Color.Transparent
                    )
                } else {
                    OutlinedTextFieldDefaults.colors(
                        disabledContainerColor = Color.Transparent
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Date Selector
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = isDateEditable) { showDatePicker = true }
                    ) {
                        OutlinedTextField(
                            value = formattedDate,
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            readOnly = true,
                            isError = isDateConflict,
                            supportingText = if (isDateConflict) {
                                { Text(text = tullabStringResource(id = R.string.lesson_date_conflict_error)) }
                            } else null,
                            shape = RoundedCornerShape(12.dp),
                            label = { Text(text = tullabStringResource(id = R.string.lesson_dialog_date_label)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarToday,
                                    contentDescription = tullabStringResource(id = R.string.lesson_dialog_date_content_description),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            colors = dateSelectorColors
                        )
                    }

                    // Time Selector
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = isTimeEditable) { showTimePicker = true }
                    ) {
                        OutlinedTextField(
                            value = formattedStartTime,
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            readOnly = true,
                            shape = RoundedCornerShape(12.dp),
                            label = { Text(text = tullabStringResource(id = R.string.lesson_dialog_time_label)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Schedule,
                                    contentDescription = tullabStringResource(id = R.string.lesson_dialog_time_content_description),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            colors = timeSelectorColors
                        )
                    }
                }

                // Pricing Type: Material 3 SingleChoiceSegmentedButtonRow using secondaryContainer
                if (!isMarkAsPaidMode) {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SegmentedButton(
                            selected = pricingMode == PricingMode.PER_HOUR,
                            onClick = {
                                if (pricingMode != PricingMode.PER_HOUR) {
                                    haptics.perform(TullabHapticFeedbackType.SEGMENT_PULSE)
                                }
                                pricingMode = PricingMode.PER_HOUR
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                activeContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Text(text = tullabStringResource(id = R.string.pricing_mode_per_hour))
                        }
                        SegmentedButton(
                            selected = pricingMode == PricingMode.FLAT_FEE,
                            onClick = {
                                if (pricingMode != PricingMode.FLAT_FEE) {
                                    haptics.perform(TullabHapticFeedbackType.SEGMENT_PULSE)
                                }
                                pricingMode = PricingMode.FLAT_FEE
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                activeContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Text(text = tullabStringResource(id = R.string.pricing_mode_flat_fee))
                        }
                    }
                }

                // Side-by-side fields with shortened labels, hrs suffix, and currency prefix
                if (isMarkAsPaidMode) {
                    if (requiresFeePrompt) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = duration,
                                onValueChange = { newValue ->
                                    val normalized = newValue.replace(',', '.')
                                    var decimalAdded = false
                                    val sanitized = buildString {
                                        normalized.forEach { char ->
                                            when {
                                                char.isDigit() -> append(char)
                                                char == '.' && !decimalAdded -> {
                                                    append(char)
                                                    decimalAdded = true
                                                }
                                            }
                                        }
                                    }
                                    duration = sanitized
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                label = { Text(text = tullabStringResource(id = R.string.dashboard_log_lesson_duration_short_label)) },
                                suffix = { Text(text = tullabStringResource(id = R.string.dashboard_log_lesson_duration_hrs_suffix)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                enabled = lesson.status != LessonStatus.PAID,
                                isError = lesson.status != LessonStatus.PAID && isDurationError,
                                supportingText = when {
                                    lesson.status == LessonStatus.PAID -> {
                                        { Text(text = tullabStringResource(id = R.string.calendar_lesson_duration_locked_helper)) }
                                    }
                                    isDurationError -> {
                                        { Text(text = durationErrorText.orEmpty()) }
                                    }
                                    else -> null
                                }
                            )

                            OutlinedTextField(
                                value = customFee,
                                onValueChange = { newValue ->
                                    val normalized = newValue.replace(',', '.')
                                    var decimalAdded = false
                                    val sanitized = buildString {
                                        normalized.forEach { char ->
                                            when {
                                                char.isDigit() -> append(char)
                                                char == '.' && !decimalAdded -> {
                                                    append(char)
                                                    decimalAdded = true
                                                }
                                            }
                                        }
                                    }
                                    customFee = sanitized
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                label = { Text(text = tullabStringResource(id = R.string.log_lesson_fee_label)) },
                                prefix = { Text(text = currencySymbol) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = duration,
                            onValueChange = { newValue ->
                                val normalized = newValue.replace(',', '.')
                                var decimalAdded = false
                                val sanitized = buildString {
                                    normalized.forEach { char ->
                                        when {
                                            char.isDigit() -> append(char)
                                            char == '.' && !decimalAdded -> {
                                                append(char)
                                                decimalAdded = true
                                            }
                                        }
                                    }
                                }
                                duration = sanitized
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            label = { Text(text = tullabStringResource(id = R.string.dashboard_log_lesson_duration_short_label)) },
                            suffix = { Text(text = tullabStringResource(id = R.string.dashboard_log_lesson_duration_hrs_suffix)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            enabled = lesson.status != LessonStatus.PAID,
                            isError = lesson.status != LessonStatus.PAID && isDurationError,
                            supportingText = when {
                                lesson.status == LessonStatus.PAID -> {
                                    { Text(text = tullabStringResource(id = R.string.calendar_lesson_duration_locked_helper)) }
                                }
                                isDurationError -> {
                                    { Text(text = durationErrorText.orEmpty()) }
                                }
                                else -> null
                            }
                        )
                    }
                } else {
                    val rateLabel = if (pricingMode == PricingMode.PER_HOUR) {
                        tullabStringResource(id = R.string.dashboard_log_lesson_hourly_rate_short_label)
                    } else {
                        tullabStringResource(id = R.string.log_lesson_fee_label)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = duration,
                            onValueChange = { newValue ->
                                val normalized = newValue.replace(',', '.')
                                var decimalAdded = false
                                val sanitized = buildString {
                                    normalized.forEach { char ->
                                        when {
                                            char.isDigit() -> append(char)
                                            char == '.' && !decimalAdded -> {
                                                append(char)
                                                decimalAdded = true
                                            }
                                        }
                                    }
                                }
                                duration = sanitized
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            label = { Text(text = tullabStringResource(id = R.string.dashboard_log_lesson_duration_short_label)) },
                            suffix = { Text(text = tullabStringResource(id = R.string.dashboard_log_lesson_duration_hrs_suffix)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            enabled = lesson.status != LessonStatus.PAID,
                            isError = lesson.status != LessonStatus.PAID && isDurationError,
                            supportingText = when {
                                lesson.status == LessonStatus.PAID -> {
                                    { Text(text = tullabStringResource(id = R.string.calendar_lesson_duration_locked_helper)) }
                                }
                                isDurationError -> {
                                    { Text(text = durationErrorText.orEmpty()) }
                                }
                                else -> null
                            }
                        )

                        OutlinedTextField(
                            value = rateOrFeeInput,
                            onValueChange = { newValue ->
                                val normalized = newValue.replace(',', '.')
                                var decimalAdded = false
                                val sanitized = buildString {
                                    normalized.forEach { char ->
                                        when {
                                            char.isDigit() -> append(char)
                                            char == '.' && !decimalAdded -> {
                                                append(char)
                                                decimalAdded = true
                                            }
                                        }
                                    }
                                }
                                rateOrFeeInput = sanitized
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            label = { Text(text = rateLabel) },
                            prefix = { Text(text = currencySymbol) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            enabled = lesson.status != LessonStatus.PAID
                        )
                    }
                }

                // Notes field
                if (!isMarkAsPaidMode) {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        label = { Text(text = tullabStringResource(id = R.string.dashboard_log_lesson_notes_label)) },
                        singleLine = false,
                        minLines = 2,
                        maxLines = 4
                    )
                }

                // Total fee calculation (for Mark as Paid)
                if (isMarkAsPaidMode) {
                    val calculatedFee = calculateDialogTotalFee(
                        durationStr = duration,
                        rate = lesson.rateOrFee,
                        pricingMode = pricingMode,
                        requiresFeePrompt = requiresFeePrompt,
                        customFeeStr = customFee
                    )

                    Text(
                        text = tullabStringResource(
                            id = R.string.calendar_lesson_details_total,
                            formatCurrency(calculatedFee, currencyCode, locale)
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Keyboard-protected bottom row with Cancel and Save
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismissAndReset,
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text(text = tullabStringResource(id = R.string.dialog_action_cancel))
                }

                Button(
                    onClick = {
                        if (!isMarkAsPaidMode && isPastLesson && statusChoice == LogLessonStatusChoice.SCHEDULED) {
                            showPastWarning = true
                        } else {
                            performSave()
                        }
                    },
                    enabled = isSaveEnabled,
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    Text(
                        text = if (isMarkAsPaidMode) {
                            tullabStringResource(id = R.string.calendar_lesson_action_mark_as_paid)
                        } else {
                            tullabStringResource(id = R.string.dialog_action_save)
                        }
                    )
                }
            }
        }
    }
}

fun parseDurationDecimal(duration: String): Double? =
    duration.trim().replace(',', '.').toDoubleOrNull()

fun calculateDialogTotalFee(
    durationStr: String,
    rate: Double,
    pricingMode: PricingMode,
    requiresFeePrompt: Boolean = false,
    customFeeStr: String = ""
): Double {
    val parsedDuration = parseDurationDecimal(durationStr) ?: 0.0
    return if (requiresFeePrompt) {
        parseDurationDecimal(customFeeStr) ?: 0.0
    } else if (pricingMode == PricingMode.PER_HOUR) {
        parsedDuration * rate
    } else {
        rate
    }
}

fun isDialogSaveEnabled(
    durationStr: String,
    rateOrFeeInput: String,
    pricingMode: PricingMode,
    isMarkAsPaidMode: Boolean = false,
    requiresFeePrompt: Boolean = false,
    customFeeStr: String = "",
    isDataChanged: Boolean = true,
    statusChoice: LogLessonStatusChoice = LogLessonStatusChoice.COMPLETED,
    isDateConflict: Boolean = false
): Boolean {
    if (isDateConflict) return false
    if (!isMarkAsPaidMode && !isDataChanged) return false
    if (statusChoice == LogLessonStatusChoice.CANCELLED) return true

    val parsedDuration = parseDurationDecimal(durationStr)
    val isDurationEntered = durationStr.trim().isNotEmpty()
    val isDurationValid = parsedDuration != null && parsedDuration > 0.0
    val isFeeValid = !requiresFeePrompt || parseDurationDecimal(customFeeStr)?.let { it > 0.0 } == true
    val isRateOrFeeValid = parseDurationDecimal(rateOrFeeInput) != null

    return when {
        isMarkAsPaidMode -> isDurationValid && isFeeValid
        statusChoice == LogLessonStatusChoice.SCHEDULED -> (!isDurationEntered || isDurationValid) && isRateOrFeeValid
        pricingMode == PricingMode.PER_HOUR -> isDurationValid && isRateOrFeeValid
        else -> (!isDurationEntered || isDurationValid) && isRateOrFeeValid
    }
}

fun getConflictingLessonDates(
    existingLessons: List<Lesson>,
    currentLessonId: Int,
    studentId: Int,
    zoneId: ZoneId = ZoneId.systemDefault()
): Set<LocalDate> {
    return existingLessons
        .filter { it.studentId == studentId && it.id != currentLessonId && it.status != LessonStatus.CANCELLED }
        .map { Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate() }
        .toSet()
}

fun isLessonDateConflicting(
    dateMillis: Long,
    conflictingDates: Set<LocalDate>,
    zoneId: ZoneId = ZoneId.systemDefault()
): Boolean {
    val localDate = Instant.ofEpochMilli(dateMillis).atZone(zoneId).toLocalDate()
    return conflictingDates.contains(localDate)
}

fun resolveInitialStatusChoice(
    lessonStatus: LessonStatus,
    lessonDateMillis: Long,
    today: LocalDate,
    zoneId: ZoneId = ZoneId.systemDefault()
): LogLessonStatusChoice {
    val initialLocalDate = Instant.ofEpochMilli(lessonDateMillis)
        .atZone(zoneId)
        .toLocalDate()
    val isStrictlyPast = initialLocalDate.isBefore(today)
    return when (lessonStatus) {
        LessonStatus.COMPLETED, LessonStatus.PAID -> LogLessonStatusChoice.COMPLETED
        LessonStatus.CANCELLED -> LogLessonStatusChoice.CANCELLED
        LessonStatus.SCHEDULED -> {
            if (isStrictlyPast) LogLessonStatusChoice.COMPLETED else LogLessonStatusChoice.SCHEDULED
        }
    }
}
