package com.barutdev.kora.ui.screens.student_profile

/**
 * Classifies the temporal scope of uncompleted scheduled lessons when a tutor
 * updates a student's profile hourly rate.
 *
 * Used to drive the dialog title, message, and confirmation button text in
 * [ScheduledLessonsRatePromptDialog] so that the copy accurately reflects
 * whether the affected lessons are in the past, the future, or both.
 */
enum class ScheduledLessonsScope {
    /** All uncompleted scheduled lessons have dates strictly before today. */
    PAST_ONLY,

    /** All uncompleted scheduled lessons have dates on or after today. */
    FUTURE_ONLY,

    /** Uncompleted scheduled lessons exist both before and on/after today. */
    MIXED
}
