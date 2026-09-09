package com.barutdev.tullab.ui.preferences

import androidx.compose.runtime.staticCompositionLocalOf
import com.barutdev.tullab.domain.model.UserPreferences

val LocalUserPreferences = staticCompositionLocalOf {
    UserPreferences(
        isDarkMode = false,
        languageCode = "en",
        currencyCode = "USD",
        defaultHourlyRate = 0.0,
        lessonRemindersEnabled = false,
        logReminderEnabled = false,
        lessonReminderHour = 9,
        lessonReminderMinute = 0,
        logReminderHour = 20,
        logReminderMinute = 0,
        hapticFeedbackEnabled = true
    )
}
