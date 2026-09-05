package com.barutdev.tullab.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class LessonTest {

    @Test
    fun calculatedValue_whenPerHour_returnsDurationTimesRate() {
        val lesson = Lesson(
            id = 1,
            studentId = 1,
            date = System.currentTimeMillis(),
            status = LessonStatus.COMPLETED,
            durationInHours = 1.5,
            notes = null,
            pricingMode = PricingMode.PER_HOUR,
            rateOrFee = 50.0
        )

        assertEquals(75.0, lesson.calculatedValue, 0.0)
    }

    @Test
    fun calculatedValue_whenPerHourAndDurationNull_returnsZero() {
        val lesson = Lesson(
            id = 1,
            studentId = 1,
            date = System.currentTimeMillis(),
            status = LessonStatus.COMPLETED,
            durationInHours = null,
            notes = null,
            pricingMode = PricingMode.PER_HOUR,
            rateOrFee = 50.0
        )

        assertEquals(0.0, lesson.calculatedValue, 0.0)
    }

    @Test
    fun calculatedValue_whenFlatFee_returnsRateOrFee() {
        val lesson = Lesson(
            id = 1,
            studentId = 1,
            date = System.currentTimeMillis(),
            status = LessonStatus.COMPLETED,
            durationInHours = 2.0, // Should be ignored
            notes = null,
            pricingMode = PricingMode.FLAT_FEE,
            rateOrFee = 60.0
        )

        assertEquals(60.0, lesson.calculatedValue, 0.0)
    }
}
