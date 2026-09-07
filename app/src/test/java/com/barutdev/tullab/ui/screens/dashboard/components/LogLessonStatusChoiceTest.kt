package com.barutdev.tullab.ui.screens.dashboard.components

import com.barutdev.tullab.domain.model.LessonStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class LogLessonStatusChoiceTest {

    @Test
    fun testConversionToLessonStatus() {
        assertEquals(LessonStatus.SCHEDULED, LogLessonStatusChoice.SCHEDULED.toLessonStatus())
        assertEquals(LessonStatus.COMPLETED, LogLessonStatusChoice.COMPLETED.toLessonStatus())
        assertEquals(LessonStatus.CANCELLED, LogLessonStatusChoice.CANCELLED.toLessonStatus())
    }

    @Test
    fun testConversionFromLessonStatus() {
        assertEquals(LogLessonStatusChoice.SCHEDULED, LogLessonStatusChoice.fromLessonStatus(LessonStatus.SCHEDULED))
        assertEquals(LogLessonStatusChoice.COMPLETED, LogLessonStatusChoice.fromLessonStatus(LessonStatus.COMPLETED))
        assertEquals(LogLessonStatusChoice.COMPLETED, LogLessonStatusChoice.fromLessonStatus(LessonStatus.PAID))
        assertEquals(LogLessonStatusChoice.CANCELLED, LogLessonStatusChoice.fromLessonStatus(LessonStatus.CANCELLED))
    }
}
