package com.barutdev.tullab.ui.screens.bulk_schedule.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.barutdev.tullab.R
import com.barutdev.tullab.util.tullabStringResource

@Composable
fun BulkSchedulePricingSection(
    useCustomRate: Boolean,
    onUseCustomRateToggled: (Boolean) -> Unit,
    customRateInput: String,
    onCustomRateChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(tullabStringResource(R.string.bulk_schedule_pricing_override), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = useCustomRate,
                onCheckedChange = onUseCustomRateToggled
            )
            Text(tullabStringResource(R.string.bulk_schedule_set_custom_rate))
        }
        
        if (useCustomRate) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = customRateInput,
                onValueChange = onCustomRateChanged,
                label = { Text(tullabStringResource(R.string.bulk_schedule_custom_hourly_rate)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
        }
    }
}
