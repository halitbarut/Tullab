package com.barutdev.kora.ui.screens.student_profile.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.barutdev.kora.R
import com.barutdev.kora.ui.screens.student_profile.ScheduledLessonsScope
import com.barutdev.kora.util.koraStringResource

/**
 * Context-aware rate-change prompt dialog.
 *
 * Shows a dialog title, message, and confirm/dismiss buttons tailored to
 * whether the affected scheduled lessons are in the past, future, or both —
 * fulfilling FR-001 and SC-001 of the 005-lesson-rate-fixes feature.
 *
 * Uses [koraStringResource] (not [stringResource]) so that the correct
 * locale is applied even when the user has overridden the app locale.
 */
@Composable
fun ScheduledLessonsRatePromptDialog(
    scheduledLessonsCount: Int,
    scope: ScheduledLessonsScope,
    onConfirm: (updateScheduled: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val titleRes = when (scope) {
        ScheduledLessonsScope.PAST_ONLY   -> R.string.rate_change_dialog_title_past
        ScheduledLessonsScope.FUTURE_ONLY -> R.string.rate_change_dialog_title_future
        ScheduledLessonsScope.MIXED       -> R.string.rate_change_dialog_title_mixed
    }
    val messageRes = when (scope) {
        ScheduledLessonsScope.PAST_ONLY   -> R.string.rate_change_dialog_message_past
        ScheduledLessonsScope.FUTURE_ONLY -> R.string.rate_change_dialog_message_future
        ScheduledLessonsScope.MIXED       -> R.string.rate_change_dialog_message_mixed
    }
    val confirmRes = when (scope) {
        ScheduledLessonsScope.PAST_ONLY   -> R.string.rate_change_dialog_update_past
        ScheduledLessonsScope.FUTURE_ONLY -> R.string.rate_change_dialog_update_future
        ScheduledLessonsScope.MIXED       -> R.string.rate_change_dialog_update_mixed
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = koraStringResource(id = titleRes))
        },
        text = {
            Text(text = koraStringResource(id = messageRes))
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(true) }) {
                Text(text = koraStringResource(id = confirmRes))
            }
        },
        dismissButton = {
            TextButton(onClick = { onConfirm(false) }) {
                Text(text = koraStringResource(id = R.string.rate_change_dialog_keep_original))
            }
        }
    )
}
