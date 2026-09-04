package com.barutdev.kora.ui.screens.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.barutdev.kora.util.koraStringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.barutdev.kora.R
import com.barutdev.kora.domain.model.Lesson
import com.barutdev.kora.ui.theme.LocalLocale
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun LogLessonDialog(
    showDialog: Boolean,
    lesson: Lesson?,
    onDismiss: () -> Unit,
    onComplete: (duration: String, notes: String) -> Unit,
    onMarkNotDone: (notes: String) -> Unit,
    isMarkAsPaidMode: Boolean = false,
    requiresFeePrompt: Boolean = false,
    onMarkAsPaid: ((duration: String, customFee: String) -> Unit)? = null
) {
    if (!showDialog || lesson == null) return

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

    var duration by rememberSaveable(lesson.id) {
        mutableStateOf(lesson.durationInHours?.toString().orEmpty())
    }
    var notes by rememberSaveable(lesson.id) {
        mutableStateOf(lesson.notes.orEmpty())
    }
    var customFee by rememberSaveable(lesson.id) {
        mutableStateOf("")
    }

    val isDurationValid = duration.trim().replace(',', '.').toDoubleOrNull()?.let { it > 0.0 } == true
    val isFeeValid = !requiresFeePrompt || customFee.trim().replace(',', '.').toDoubleOrNull()?.let { it > 0.0 } == true
    val isCompleteEnabled = isDurationValid && isFeeValid

    fun resetInputs() {
        duration = lesson.durationInHours?.toString().orEmpty()
        notes = lesson.notes.orEmpty()
        customFee = ""
    }

    AlertDialog(
        onDismissRequest = {
            resetInputs()
            onDismiss()
        },
        title = {
            Text(
                text = if (isMarkAsPaidMode) {
                    koraStringResource(id = R.string.calendar_lesson_action_mark_as_paid)
                } else {
                    koraStringResource(id = R.string.dashboard_log_lesson_dialog_title)
                }
            )
        },
        text = {
            Column {
                Text(text = koraStringResource(id = R.string.dashboard_log_lesson_dialog_date, formattedDate))
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
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
                    label = { Text(text = koraStringResource(id = R.string.dashboard_log_lesson_duration_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = lesson.status != com.barutdev.kora.domain.model.LessonStatus.PAID,
                    supportingText = if (lesson.status == com.barutdev.kora.domain.model.LessonStatus.PAID) {
                        { Text(text = koraStringResource(id = R.string.calendar_lesson_duration_locked_helper)) }
                    } else null
                )
                if (requiresFeePrompt) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
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
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "Custom Fee (Required)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
                if (!isMarkAsPaidMode) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = koraStringResource(id = R.string.dashboard_log_lesson_notes_label)) },
                        singleLine = false,
                        minLines = 2,
                        maxLines = 4
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isMarkAsPaidMode) {
                        onMarkAsPaid?.invoke(duration, customFee)
                    } else {
                        onComplete(duration, notes)
                    }
                    resetInputs()
                },
                enabled = isCompleteEnabled
            ) {
                Text(
                    text = if (isMarkAsPaidMode) {
                        koraStringResource(id = R.string.calendar_lesson_action_mark_as_paid)
                    } else {
                        koraStringResource(id = R.string.dashboard_log_lesson_complete_button)
                    }
                )
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isMarkAsPaidMode) {
                    TextButton(
                        onClick = {
                            onMarkNotDone(notes)
                            resetInputs()
                        }
                    ) {
                        Text(text = koraStringResource(id = R.string.dashboard_log_lesson_mark_not_done_button))
                    }
                }
                TextButton(
                    onClick = {
                        resetInputs()
                        onDismiss()
                    }
                ) {
                    Text(text = koraStringResource(id = R.string.dialog_action_cancel))
                }
            }
        }
    )
}

