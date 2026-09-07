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
import androidx.compose.material3.Switch
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
    onMarkNotDone: (notes: String) -> Unit,
    isMarkAsPaidMode: Boolean = false,
    requiresFeePrompt: Boolean = false,
    onMarkAsPaid: ((duration: String, customFee: String) -> Unit)? = null,
    onSaveScheduled: ((duration: String, notes: String, pricingMode: PricingMode, rateOrFee: String) -> Unit)? = null,
    isPastScheduled: Boolean = false,
    currencyCode: String = "USD"
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

    val isPastLesson = remember(lesson.date) {
        val today = LocalDate.now(ZoneId.systemDefault())
        Instant.ofEpochMilli(lesson.date)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .isBefore(today)
    }

    val currencySymbol = remember(currencyCode, locale) {
        getCurrencySymbol(currencyCode, locale)
    }

    val initialDurationStr = remember(lesson) { lesson.durationInHours?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "" }
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

    val initialIsMarkCompleted = remember(lesson) {
        lesson.isCompleted
    }
    var isMarkCompleted by rememberSaveable(lesson.id) {
        mutableStateOf(initialIsMarkCompleted)
    }

    val isDataChanged = duration != initialDurationStr ||
            notes != initialNotesStr ||
            pricingMode != initialPricingMode ||
            rateOrFeeInput != initialRateOrFeeStr ||
            isMarkCompleted != initialIsMarkCompleted

    var showPastWarning by remember { mutableStateOf(false) }

    val isDurationEntered = duration.trim().isNotEmpty()
    val parsedDuration = duration.trim().replace(',', '.').toDoubleOrNull()
    val isDurationValid = parsedDuration != null && parsedDuration > 0.0
    val isFeeValid = !requiresFeePrompt || customFee.trim().replace(',', '.').toDoubleOrNull()?.let { it > 0.0 } == true
    val isRateOrFeeValid = rateOrFeeInput.trim().replace(',', '.').toDoubleOrNull() != null

    val isSaveEnabled = when {
        isMarkAsPaidMode -> isDurationValid && isFeeValid
        pricingMode == PricingMode.PER_HOUR -> isDurationValid && isRateOrFeeValid && isDataChanged
        else -> (!isDurationEntered || isDurationValid) && isRateOrFeeValid && isDataChanged
    }

    fun resetInputs() {
        duration = lesson.durationInHours?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: ""
        notes = lesson.notes.orEmpty()
        customFee = ""
        pricingMode = lesson.pricingMode
        rateOrFeeInput = lesson.rateOrFee.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }
        isMarkCompleted = lesson.isCompleted
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
            onSave != null -> {
                onSave(duration, notes, pricingMode, rateOrFeeInput, isMarkCompleted)
            }
            isMarkCompleted -> {
                onComplete?.invoke(duration, notes, pricingMode, rateOrFeeInput)
            }
            else -> {
                onSaveScheduled?.invoke(duration, notes, pricingMode, rateOrFeeInput)
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
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Title only
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

            // Pricing Type: Toned-down Material 3 SingleChoiceSegmentedButtonRow using secondaryContainer
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

            // Dedicated "Mark as completed" Switch placed right above the notes field
            if (!isMarkAsPaidMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tullabStringResource(id = R.string.dashboard_log_lesson_mark_completed_switch),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = isMarkCompleted,
                        onCheckedChange = { isMarkCompleted = it },
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
                val parsedDuration = duration.trim().replace(',', '.').toDoubleOrNull() ?: 0.0
                val rate = lesson.rateOrFee

                val calculatedFee = if (requiresFeePrompt) {
                    customFee.trim().replace(',', '.').toDoubleOrNull() ?: 0.0
                } else if (pricingMode == PricingMode.PER_HOUR) {
                    parsedDuration * rate
                } else {
                    rate
                }

                Text(
                    text = tullabStringResource(
                        id = R.string.calendar_lesson_details_total,
                        formatCurrency(calculatedFee, currencyCode)
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Spacious single row: subtle Cancel (left), discreet red Not Done (center), solid primary Save button (right)
            val showNotDone = !isMarkAsPaidMode && (isPastLesson || onSaveScheduled == null)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismissAndReset,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(text = tullabStringResource(id = R.string.dialog_action_cancel))
                }

                if (showNotDone) {
                    TextButton(
                        onClick = {
                            onMarkNotDone(notes)
                            resetInputs()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(text = tullabStringResource(id = R.string.dashboard_log_lesson_not_done_button))
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = {
                        if (!isMarkAsPaidMode && isPastLesson && !isMarkCompleted) {
                            showPastWarning = true
                        } else {
                            performSave()
                        }
                    },
                    enabled = isSaveEnabled,
                    contentPadding = PaddingValues(horizontal = 16.dp)
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
