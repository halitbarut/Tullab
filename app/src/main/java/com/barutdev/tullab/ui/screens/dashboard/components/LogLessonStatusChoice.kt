package com.barutdev.tullab.ui.screens.dashboard.components

import com.barutdev.tullab.domain.model.LessonStatus

enum class LogLessonStatusChoice {
    SCHEDULED,
    COMPLETED,
    CANCELLED;

    fun toLessonStatus(): LessonStatus = when (this) {
        SCHEDULED -> LessonStatus.SCHEDULED
        COMPLETED -> LessonStatus.COMPLETED
        CANCELLED -> LessonStatus.CANCELLED
    }

    companion object {
        fun fromLessonStatus(status: LessonStatus): LogLessonStatusChoice = when (status) {
            LessonStatus.SCHEDULED -> SCHEDULED
            LessonStatus.COMPLETED, LessonStatus.PAID -> COMPLETED
            LessonStatus.CANCELLED -> CANCELLED
        }
    }
}
