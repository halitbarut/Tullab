package com.barutdev.tullab.ui.screens.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.barutdev.tullab.R
import com.barutdev.tullab.domain.model.Lesson
import com.barutdev.tullab.domain.model.LessonStatus
import com.barutdev.tullab.domain.model.PricingMode
import com.barutdev.tullab.ui.theme.LocalLocale
import com.barutdev.tullab.util.formatCurrency
import com.barutdev.tullab.util.getCurrencySymbol
import com.barutdev.tullab.util.tullabStringResource
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogLessonDialog(
    showDialog: Boolean,
    lesson: Lesson?,
    onDismiss: () -> Unit,
    onSave: ((duration: String, notes: String, pricingMode: PricingMode, rateOrFee: String, isCompleted: Boolean) -> Unit)? = null,
    onComplete: ((duration: String, notes: String, pricingMode: PricingMode, rateOrFee: String) -> Unit)? = null,
    onMarkNotDone: ((notes: String) -> Unit)? = null,
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
        statusChoice: LogLessonStatusChoice
    ) -> Unit)? = null
) {
    if (!showDialog || lesson == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val locale = LocalLocale.current

    val formatter = remember(locale) {
        DateTimeFormatter.ofPattern("EEEE, MMMM d", locale)
    }
    val formattedDate = remember(lesson.id, formatter) {
        Instant.ofEpochMilli(lesson.date)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(formatter)
    }

    val today = remember { LocalDate.now(ZoneId.systemDefault()) }
    val lessonDate = remember(lesson.date) {
        Instant.ofEpochMilli(lesson.date)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
    val isPastLesson = remember(lessonDate, today) {
        lessonDate.isBefore(today)
    }
    val isPastOrToday = remember(lessonDate, today) {
        !lessonDate.isAfter(today)
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
    // If scheduled: if past/today default to COMPLETED ("Yapıldı"), if future default to SCHEDULED ("Planlandı").
    val initialStatusChoice = remember(lesson) {
        when (lesson.status) {
            LessonStatus.COMPLETED, LessonStatus.PAID -> LogLessonStatusChoice.COMPLETED
            LessonStatus.CANCELLED -> LogLessonStatusChoice.CANCELLED
            LessonStatus.SCHEDULED -> {
                if (isPastOrToday) LogLessonStatusChoice.COMPLETED else LogLessonStatusChoice.SCHEDULED
            }
        }
    }
    var statusChoice by remember(lesson.id, initialStatusChoice) {
        mutableStateOf(initialStatusChoice)
    }

    val isDataChanged = duration != initialDurationStr ||
            notes != initialNotesStr ||
            pricingMode != initialPricingMode ||
            rateOrFeeInput != initialRateOrFeeStr ||
            statusChoice != initialStatusChoice

    var showPastWarning by remember { mutableStateOf(false) }

    val isSaveEnabled = isDialogSaveEnabled(
        durationStr = duration,
        rateOrFeeInput = rateOrFeeInput,
        pricingMode = pricingMode,
        isMarkAsPaidMode = isMarkAsPaidMode,
        requiresFeePrompt = requiresFeePrompt,
        customFeeStr = customFee,
        isDataChanged = isDataChanged,
        statusChoice = statusChoice
    )

    fun resetInputs() {
        duration = lesson.durationInHours?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: ""
        notes = lesson.notes.orEmpty()
        customFee = ""
        pricingMode = lesson.pricingMode
        rateOrFeeInput = lesson.rateOrFee.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }
        statusChoice = initialStatusChoice
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
                onSaveStatusAndDetails(duration, notes, pricingMode, rateOrFeeInput, statusChoice)
            }
            statusChoice == LogLessonStatusChoice.CANCELLED -> {
                if (onMarkNotDone != null) {
                    onMarkNotDone(notes)
                } else if (onSave != null) {
                    onSave(duration, notes, pricingMode, rateOrFeeInput, false)
                } else {
                    onComplete?.invoke(duration, notes, pricingMode, rateOrFeeInput)
                }
            }
            statusChoice == LogLessonStatusChoice.COMPLETED -> {
                if (onSave != null) {
                    onSave(duration, notes, pricingMode, rateOrFeeInput, true)
                } else {
                    onComplete?.invoke(duration, notes, pricingMode, rateOrFeeInput)
                }
            }
            else -> { // SCHEDULED
                if (onSave != null) {
                    onSave(duration, notes, pricingMode, rateOrFeeInput, false)
                } else if (onSaveScheduled != null) {
                    onSaveScheduled(duration, notes, pricingMode, rateOrFeeInput)
                } else {
                    onComplete?.invoke(duration, notes, pricingMode, rateOrFeeInput)
                }
            }
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
                // Header: Title
                Text(
                    text = if (isMarkAsPaidMode) {
                        tullabStringResource(id = R.string.calendar_lesson_action_mark_as_paid)
                    } else {
                        tullabStringResource(id = R.string.dashboard_log_lesson_dialog_title)
                    },
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = tullabStringResource(id = R.string.dashboard_log_lesson_dialog_date, formattedDate),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Status: M3 SingleChoiceSegmentedButtonRow
                if (!isMarkAsPaidMode) {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SegmentedButton(
                            selected = statusChoice == LogLessonStatusChoice.SCHEDULED,
                            onClick = { statusChoice = LogLessonStatusChoice.SCHEDULED },
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
                            onClick = { statusChoice = LogLessonStatusChoice.COMPLETED },
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
                            onClick = { statusChoice = LogLessonStatusChoice.CANCELLED },
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

                // Pricing Type: Material 3 SingleChoiceSegmentedButtonRow using secondaryContainer
                if (!isMarkAsPaidMode) {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SegmentedButton(
                            selected = pricingMode == PricingMode.PER_HOUR,
                            onClick = { pricingMode = PricingMode.PER_HOUR },
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
                            onClick = { pricingMode = PricingMode.FLAT_FEE },
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
                                supportingText = if (lesson.status == LessonStatus.PAID) {
                                    { Text(text = tullabStringResource(id = R.string.calendar_lesson_duration_locked_helper)) }
                                } else null
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
                            supportingText = if (lesson.status == LessonStatus.PAID) {
                                { Text(text = tullabStringResource(id = R.string.calendar_lesson_duration_locked_helper)) }
                            } else null
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
                            supportingText = if (lesson.status == LessonStatus.PAID) {
                                { Text(text = tullabStringResource(id = R.string.calendar_lesson_duration_locked_helper)) }
                            } else null
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
    statusChoice: LogLessonStatusChoice = LogLessonStatusChoice.COMPLETED
): Boolean {
    if (!isMarkAsPaidMode && !isDataChanged) return false
    if (statusChoice == LogLessonStatusChoice.CANCELLED) return true

    val parsedDuration = parseDurationDecimal(durationStr)
    val isDurationEntered = durationStr.trim().isNotEmpty()
    val isDurationValid = parsedDuration != null && parsedDuration > 0.0
    val isFeeValid = !requiresFeePrompt || parseDurationDecimal(customFeeStr)?.let { it > 0.0 } == true
    val isRateOrFeeValid = parseDurationDecimal(rateOrFeeInput) != null

    return when {
        isMarkAsPaidMode -> isDurationValid && isFeeValid
        pricingMode == PricingMode.PER_HOUR -> isDurationValid && isRateOrFeeValid
        else -> (!isDurationEntered || isDurationValid) && isRateOrFeeValid
    }
}
