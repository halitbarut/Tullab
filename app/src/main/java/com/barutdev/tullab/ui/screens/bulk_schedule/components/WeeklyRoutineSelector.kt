package com.barutdev.tullab.ui.screens.bulk_schedule.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import com.barutdev.tullab.R
import com.barutdev.tullab.domain.model.WeeklyRoutineEndCondition
import com.barutdev.tullab.util.tullabStringResource
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.WeekFields
import com.barutdev.tullab.ui.theme.LocalLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyRoutineSelector(
    selectedDaysOfWeek: Set<DayOfWeek>,
    onDayOfWeekToggled: (DayOfWeek) -> Unit,
    routineStartDate: LocalDate,
    onStartDateChanged: (LocalDate) -> Unit,
    endCondition: WeeklyRoutineEndCondition,
    onEndConditionTypeChanged: (Boolean) -> Unit,
    onEndDateChanged: (LocalDate) -> Unit,
    targetCountInput: String,
    onTargetCountChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    timePickerSlot: @Composable () -> Unit = {}
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val locale = LocalLocale.current

    Column(modifier = modifier.fillMaxWidth()) {
        Text(tullabStringResource(R.string.bulk_schedule_repeat_on), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val days = remember(locale) {
                val weekFields = WeekFields.of(locale)
                val firstDay = weekFields.firstDayOfWeek
                (0..6).map { firstDay.plus(it.toLong()) }
            }
            days.forEach { dayOfWeek ->
                val isSelected = selectedDaysOfWeek.contains(dayOfWeek)
                val dayName = dayOfWeek.getDisplayName(TextStyle.SHORT, locale).take(1).uppercase(locale)
                FilterChip(
                    selected = isSelected,
                    onClick = { onDayOfWeekToggled(dayOfWeek) },
                    label = { Text(dayName) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(tullabStringResource(R.string.bulk_schedule_routine_start_date), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        val formatter = remember(locale) { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale) }
        OutlinedButton(onClick = { showStartDatePicker = true }) {
            Text(routineStartDate.format(formatter))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        timePickerSlot()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(tullabStringResource(R.string.bulk_schedule_ends), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = endCondition is WeeklyRoutineEndCondition.ByEndDate,
                onClick = { 
                    if (endCondition !is WeeklyRoutineEndCondition.ByEndDate) {
                        onEndConditionTypeChanged(true)
                    }
                }
            )
            Text(tullabStringResource(R.string.bulk_schedule_ends_on))
            if (endCondition is WeeklyRoutineEndCondition.ByEndDate) {
                val formatter = remember(locale) { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale) }
                OutlinedButton(onClick = { showEndDatePicker = true }, modifier = Modifier.padding(start = 8.dp)) {
                    Text(endCondition.endDate.format(formatter))
                }
            } else {
                Text(tullabStringResource(R.string.bulk_schedule_date), modifier = Modifier.padding(start = 8.dp))
            }
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = endCondition is WeeklyRoutineEndCondition.ByTargetCount,
                onClick = { 
                    if (endCondition !is WeeklyRoutineEndCondition.ByTargetCount) {
                        onEndConditionTypeChanged(false)
                    }
                }
            )
            Text(tullabStringResource(R.string.bulk_schedule_after))
            if (endCondition is WeeklyRoutineEndCondition.ByTargetCount) {
                val isCountError = targetCountInput.isNotEmpty() &&
                    (targetCountInput.trim().toIntOrNull()?.let { it <= 0 || it > 30 } ?: true)
                OutlinedTextField(
                    value = targetCountInput,
                    onValueChange = onTargetCountChanged,
                    isError = isCountError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(100.dp).padding(start = 8.dp)
                )
                Text(tullabStringResource(R.string.bulk_schedule_occurrences_suffix), modifier = Modifier.padding(start = 8.dp))
            } else {
                Text(tullabStringResource(R.string.bulk_schedule_occurrences), modifier = Modifier.padding(start = 8.dp))
            }
        }
    }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = routineStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        onStartDateChanged(date)
                    }
                    showStartDatePicker = false
                }) {
                    Text(tullabStringResource(R.string.dialog_action_ok))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (endCondition as? WeeklyRoutineEndCondition.ByEndDate)?.endDate
                ?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        onEndDateChanged(date)
                    }
                    showEndDatePicker = false
                }) {
                    Text(tullabStringResource(R.string.dialog_action_ok))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
