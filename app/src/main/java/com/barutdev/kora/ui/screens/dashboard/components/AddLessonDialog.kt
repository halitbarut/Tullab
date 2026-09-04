package com.barutdev.kora.ui.screens.dashboard.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.barutdev.kora.util.koraStringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.barutdev.kora.R
import kotlin.text.StringBuilder

import com.barutdev.kora.domain.model.PricingMode
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButton
import androidx.compose.ui.Alignment

@Composable
fun AddLessonDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onSave: (duration: String, notes: String, pricingMode: PricingMode, rateOrFee: String) -> Unit
) {
    if (!showDialog) return

    var duration by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var pricingMode by rememberSaveable { mutableStateOf(PricingMode.PER_HOUR) }
    var rateOrFeeInput by rememberSaveable { mutableStateOf("") }

    val isDurationValid = duration.trim().replace(',', '.').toDoubleOrNull()?.let { it > 0.0 } == true
    val isSaveEnabled = if (pricingMode == PricingMode.PER_HOUR) {
        isDurationValid
    } else {
        rateOrFeeInput.trim().replace(',', '.').toDoubleOrNull() != null
    }

    AlertDialog(
        onDismissRequest = {
            duration = ""
            notes = ""
            rateOrFeeInput = ""
            onDismiss()
        },
        title = {
            Text(text = koraStringResource(id = R.string.dashboard_add_lesson_dialog_title))
        },
        text = {
            Column {
                Column(Modifier.selectableGroup()) {
                    Row(
                        Modifier.fillMaxWidth().selectable(
                            selected = (pricingMode == PricingMode.PER_HOUR),
                            onClick = { pricingMode = PricingMode.PER_HOUR }
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (pricingMode == PricingMode.PER_HOUR),
                            onClick = { pricingMode = PricingMode.PER_HOUR }
                        )
                        Text(text = koraStringResource(id = R.string.pricing_mode_per_hour))
                    }
                    Row(
                        Modifier.fillMaxWidth().selectable(
                            selected = (pricingMode == PricingMode.FLAT_FEE),
                            onClick = { pricingMode = PricingMode.FLAT_FEE }
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (pricingMode == PricingMode.FLAT_FEE),
                            onClick = { pricingMode = PricingMode.FLAT_FEE }
                        )
                        Text(text = koraStringResource(id = R.string.pricing_mode_flat_fee))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (pricingMode == PricingMode.PER_HOUR) {
                    TextField(
                        value = duration,
                        onValueChange = { newValue ->
                            val normalized = newValue.replace(',', '.')
                            var decimalAdded = false
                            val sanitized = StringBuilder()
                            normalized.forEach { char ->
                                when {
                                    char.isDigit() -> sanitized.append(char)
                                    char == '.' && !decimalAdded -> {
                                        sanitized.append(char)
                                        decimalAdded = true
                                    }
                                }
                            }
                            duration = sanitized.toString()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = koraStringResource(id = R.string.dashboard_add_lesson_duration_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                val rateLabel = if (pricingMode == PricingMode.PER_HOUR) {
                    koraStringResource(id = R.string.student_profile_hourly_rate_label)
                } else {
                    koraStringResource(id = R.string.log_lesson_fee_label)
                }

                TextField(
                    value = rateOrFeeInput,
                    onValueChange = { rateOrFeeInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = rateLabel) },
                    placeholder = { Text(text = koraStringResource(id = R.string.student_profile_optional_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = koraStringResource(id = R.string.dashboard_add_lesson_notes_label)) },
                    singleLine = false,
                    minLines = 2,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(duration, notes, pricingMode, rateOrFeeInput)
                    duration = ""
                    notes = ""
                    rateOrFeeInput = ""
                },
                enabled = isSaveEnabled
            ) {
                Text(text = koraStringResource(id = R.string.dialog_action_save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    duration = ""
                    notes = ""
                    rateOrFeeInput = ""
                    onDismiss()
                }
            ) {
                Text(text = koraStringResource(id = R.string.dialog_action_cancel))
            }
        }
    )
}
