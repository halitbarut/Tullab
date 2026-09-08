package com.barutdev.tullab.ui.screens.dashboard.components

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
import com.barutdev.tullab.util.tullabStringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.barutdev.tullab.R
import kotlin.text.StringBuilder

import com.barutdev.tullab.domain.model.PricingMode
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
    // Duration consistency: COMPLETED lessons require duration > 0 for any
    // pricing mode so hour statistics stay accurate. Flat-fee totals still
    // never multiply (see calculatedValue).
    val isRateValid = rateOrFeeInput.trim().replace(',', '.').toDoubleOrNull() != null
    val isSaveEnabled = isDurationValid && isRateValid

    AlertDialog(
        onDismissRequest = {
            duration = ""
            notes = ""
            rateOrFeeInput = ""
            onDismiss()
        },
        title = {
            Text(text = tullabStringResource(id = R.string.dashboard_add_lesson_dialog_title))
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
                        Text(text = tullabStringResource(id = R.string.pricing_mode_per_hour))
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
                        Text(text = tullabStringResource(id = R.string.pricing_mode_flat_fee))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.OutlinedTextField(
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
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    label = { Text(text = tullabStringResource(id = R.string.dashboard_add_lesson_duration_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                val rateLabel = if (pricingMode == PricingMode.PER_HOUR) {
                    tullabStringResource(id = R.string.student_profile_hourly_rate_label)
                } else {
                    tullabStringResource(id = R.string.log_lesson_fee_label)
                }

                androidx.compose.material3.OutlinedTextField(
                    value = rateOrFeeInput,
                    onValueChange = { rateOrFeeInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    label = { Text(text = rateLabel) },
                    placeholder = { Text(text = tullabStringResource(id = R.string.student_profile_optional_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                androidx.compose.material3.OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    label = { Text(text = tullabStringResource(id = R.string.dashboard_add_lesson_notes_label)) },
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
                Text(text = tullabStringResource(id = R.string.dialog_action_save))
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
                Text(text = tullabStringResource(id = R.string.dialog_action_cancel))
            }
        }
    )
}
