package com.barutdev.kora.ui.screens.student_profile.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.barutdev.kora.R

@Composable
fun ScheduledLessonsRatePromptDialog(
    scheduledLessonsCount: Int,
    onConfirm: (updateScheduled: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.rate_change_dialog_title))
        },
        text = {
            Text(
                text = stringResource(
                    id = R.string.rate_change_dialog_message,
                    scheduledLessonsCount
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(true) }) {
                Text(text = stringResource(R.string.rate_change_dialog_update_all))
            }
        },
        dismissButton = {
            TextButton(onClick = { onConfirm(false) }) {
                Text(text = stringResource(R.string.rate_change_dialog_future_only))
            }
        }
    )
}
