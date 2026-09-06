package com.barutdev.tullab.ui.screens.bulk_schedule.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.barutdev.tullab.R
import com.barutdev.tullab.ui.theme.LocalLocale
import com.barutdev.tullab.util.tullabStringResource
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DayTimeChipList(
    selectedDates: Set<LocalDate>,
    defaultStartTime: LocalTime,
    customDayTimes: Map<LocalDate, LocalTime>,
    onCustomTimeSelected: (LocalDate, LocalTime?) -> Unit,
    modifier: Modifier = Modifier
) {
    val locale = LocalLocale.current
    val dateFormatter = remember(locale) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale)
    }
    val timeFormatter = remember(locale) {
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)
    }

    var showTimePickerForDate by remember { mutableStateOf<LocalDate?>(null) }
    
    val sortedDates = remember(selectedDates) {
        selectedDates.sorted()
    }

    if (showTimePickerForDate != null) {
        val targetDate = showTimePickerForDate!!
        val initialTime = customDayTimes[targetDate] ?: defaultStartTime
        val timePickerState = rememberTimePickerState(
            initialHour = initialTime.hour,
            initialMinute = initialTime.minute,
            is24Hour = false
        )
        
        AlertDialog(
            onDismissRequest = { showTimePickerForDate = null },
            title = { Text(tullabStringResource(R.string.bulk_schedule_time_picker_title)) },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    val time = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    onCustomTimeSelected(targetDate, time)
                    showTimePickerForDate = null
                }) {
                    Text(tullabStringResource(R.string.dialog_action_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onCustomTimeSelected(targetDate, null)
                    showTimePickerForDate = null
                }) {
                    Text(tullabStringResource(R.string.dialog_action_cancel))
                }
            }
        )
    }

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        sortedDates.forEach { date ->
            val hasCustomTime = customDayTimes.containsKey(date)
            val effectiveTime = customDayTimes[date] ?: defaultStartTime
            
            InputChip(
                selected = hasCustomTime,
                onClick = { showTimePickerForDate = date },
                label = { 
                    Text(
                        "${date.format(dateFormatter)} • ${effectiveTime.format(timeFormatter)}"
                    ) 
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null
                    )
                }
            )
        }
    }
}
