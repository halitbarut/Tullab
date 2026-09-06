package com.barutdev.tullab.ui.screens.bulk_schedule.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.barutdev.tullab.R
import com.barutdev.tullab.util.tullabStringResource

@Composable
fun PastDateWarningDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(tullabStringResource(R.string.bulk_schedule_past_date_warning_title))
            },
            text = {
                Text(tullabStringResource(R.string.bulk_schedule_past_date_warning_message))
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(tullabStringResource(R.string.dialog_action_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(tullabStringResource(R.string.dialog_action_cancel))
                }
            }
        )
    }
}
