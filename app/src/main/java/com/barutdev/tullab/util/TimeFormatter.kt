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

/**
 * Calculates the duration in milliseconds from the given [now] instant until the next midnight in [zone].
 * Enforces a minimum delay of 1000ms to guard against race conditions or immediate zero-delays.
 */
fun calculateDelayToNextMidnight(
    now: java.time.Instant = java.time.Instant.now(),
    zone: java.time.ZoneId = java.time.ZoneId.systemDefault()
): Long {
    val zdt = now.atZone(zone)
    val nextMidnight = zdt.toLocalDate().plusDays(1).atStartOfDay(zone)
    val delayMs = java.time.Duration.between(zdt, nextMidnight).toMillis()
    return delayMs.coerceAtLeast(1000L)
}

