package com.barutdev.tullab.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.util.Date

/**
 * Formats a timestamp into localized time using the user's device preferences (12/24h and locale).
 */
fun formatLessonStartTime(context: Context, epochMillis: Long): String {
    return android.text.format.DateFormat.getTimeFormat(context).format(Date(epochMillis))
}

@Composable
fun formatLessonStartTime(epochMillis: Long): String {
    val context = LocalContext.current
    return formatLessonStartTime(context, epochMillis)
}
