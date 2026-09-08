package com.barutdev.tullab.ui.screens.dashboard.components

import com.barutdev.tullab.domain.model.Lesson
import com.barutdev.tullab.domain.model.LessonStatus
import com.barutdev.tullab.domain.model.PricingMode
import com.barutdev.tullab.R
import com.barutdev.tullab.ui.screens.calendar.resolveLessonActionTextRes
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LogLessonDialogValidationTest {

    @Test
    fun testDurationDecimalParsing() {
        assertEquals(1.5, parseDurationDecimal("1.5"))
        assertEquals(1.5, parseDurationDecimal("1,5"))
        assertEquals(2.0, parseDurationDecimal("2"))
        assertEquals(null, parseDurationDecimal("invalid"))
    }
    
    @Test
    fun testReactiveFeeCalculation() {
        // Hourly rate logic
        assertEquals(75.0, calculateDialogTotalFee("1.5", 50.0, PricingMode.PER_HOUR, false, ""), 0.0)
        assertEquals(75.0, calculateDialogTotalFee("1,5", 50.0, PricingMode.PER_HOUR, false, ""), 0.0)
        
        // Flat fee logic
        assertEquals(50.0, calculateDialogTotalFee("1.5", 50.0, PricingMode.FLAT_FEE, false, ""), 0.0)
        
        // Custom fee prompt
        assertEquals(100.0, calculateDialogTotalFee("1.5", 0.0, PricingMode.PER_HOUR, true, "100"), 0.0)
    }

    @Test
    fun testPositiveValidationLogic() {
        // Valid mark as paid
        assertTrue(isDialogSaveEnabled(durationStr = "1.5", customFeeStr = "", rateOrFeeInput = "50.0", pricingMode = PricingMode.PER_HOUR, isMarkAsPaidMode = true, requiresFeePrompt = false))
        
        // Invalid duration
        assertFalse(isDialogSaveEnabled(durationStr = "-1.0", customFeeStr = "", rateOrFeeInput = "50.0", pricingMode = PricingMode.PER_HOUR, isMarkAsPaidMode = true, requiresFeePrompt = false))
        assertFalse(isDialogSaveEnabled(durationStr = "0", customFeeStr = "", rateOrFeeInput = "50.0", pricingMode = PricingMode.PER_HOUR, isMarkAsPaidMode = true, requiresFeePrompt = false))
        assertFalse(isDialogSaveEnabled(durationStr = "", customFeeStr = "", rateOrFeeInput = "50.0", pricingMode = PricingMode.PER_HOUR, isMarkAsPaidMode = true, requiresFeePrompt = false))
        
        // Custom fee invalid vs valid
        assertFalse(isDialogSaveEnabled(durationStr = "1.5", customFeeStr = "-10", rateOrFeeInput = "0", pricingMode = PricingMode.PER_HOUR, isMarkAsPaidMode = true, requiresFeePrompt = true))
        assertTrue(isDialogSaveEnabled(durationStr = "1.5", customFeeStr = "100", rateOrFeeInput = "0", pricingMode = PricingMode.PER_HOUR, isMarkAsPaidMode = true, requiresFeePrompt = true))

        // Standard save mode with data changed
        assertTrue(isDialogSaveEnabled(durationStr = "2.0", rateOrFeeInput = "60.0", pricingMode = PricingMode.PER_HOUR, isDataChanged = true))
        assertFalse(isDialogSaveEnabled(durationStr = "2.0", rateOrFeeInput = "60.0", pricingMode = PricingMode.PER_HOUR, isDataChanged = false))

        // Mark as paid mode ignores isDataChanged guard
        assertTrue(isDialogSaveEnabled(durationStr = "1.5", rateOrFeeInput = "50.0", pricingMode = PricingMode.PER_HOUR, isMarkAsPaidMode = true, isDataChanged = false))
        assertTrue(isDialogSaveEnabled(durationStr = "1.5", rateOrFeeInput = "50.0", pricingMode = PricingMode.PER_HOUR, isMarkAsPaidMode = true, isDataChanged = true))

        // Cancelled status choice allows saving even with empty duration
        assertTrue(isDialogSaveEnabled(durationStr = "", rateOrFeeInput = "", pricingMode = PricingMode.PER_HOUR, isDataChanged = true, statusChoice = LogLessonStatusChoice.CANCELLED))
        assertFalse(isDialogSaveEnabled(durationStr = "", rateOrFeeInput = "", pricingMode = PricingMode.PER_HOUR, isDataChanged = false, statusChoice = LogLessonStatusChoice.CANCELLED))
    }

    @Test
    fun testDateModificationMarksDataChanged() {
        val originalDate = 1700000000000L
        val updatedDate = 1700086400000L
        val isDateChanged = updatedDate != originalDate
        assertTrue(isDateChanged)

        assertTrue(isDialogSaveEnabled(
            durationStr = "1.0",
            rateOrFeeInput = "50.0",
            pricingMode = PricingMode.PER_HOUR,
            isDataChanged = isDateChanged
        ))
    }

    @Test
    fun testScheduledLessonDurationValidation() {
        // Scheduled lesson does not strictly force duration
        assertTrue(
            isDialogSaveEnabled(
                durationStr = "",
                rateOrFeeInput = "50.0",
                pricingMode = PricingMode.PER_HOUR,
                isDataChanged = true,
                statusChoice = LogLessonStatusChoice.SCHEDULED
            )
        )

        // Scheduled lesson with valid duration is allowed
        assertTrue(
            isDialogSaveEnabled(
                durationStr = "1.5",
                rateOrFeeInput = "50.0",
                pricingMode = PricingMode.PER_HOUR,
                isDataChanged = true,
                statusChoice = LogLessonStatusChoice.SCHEDULED
            )
        )

        // Scheduled lesson with invalid entered duration is blocked
        assertFalse(
            isDialogSaveEnabled(
                durationStr = "-1.0",
                rateOrFeeInput = "50.0",
                pricingMode = PricingMode.PER_HOUR,
                isDataChanged = true,
                statusChoice = LogLessonStatusChoice.SCHEDULED
            )
        )
        assertFalse(
            isDialogSaveEnabled(
                durationStr = "0",
                rateOrFeeInput = "50.0",
                pricingMode = PricingMode.PER_HOUR,
                isDataChanged = true,
                statusChoice = LogLessonStatusChoice.SCHEDULED
            )
        )

        // Scheduled lesson without data changed is blocked
        assertFalse(
            isDialogSaveEnabled(
                durationStr = "",
                rateOrFeeInput = "50.0",
                pricingMode = PricingMode.PER_HOUR,
                isDataChanged = false,
                statusChoice = LogLessonStatusChoice.SCHEDULED
            )
        )
    }

    @Test
    fun testCompletedLessonDurationValidation() {
        // Completed lesson with PER_HOUR strictly requires positive duration
        assertFalse(
            isDialogSaveEnabled(
                durationStr = "",
                rateOrFeeInput = "50.0",
                pricingMode = PricingMode.PER_HOUR,
                isDataChanged = true,
                statusChoice = LogLessonStatusChoice.COMPLETED
            )
        )
        assertTrue(
            isDialogSaveEnabled(
                durationStr = "1.0",
                rateOrFeeInput = "50.0",
                pricingMode = PricingMode.PER_HOUR,
                isDataChanged = true,
                statusChoice = LogLessonStatusChoice.COMPLETED
            )
        )

        // Completed lesson with FLAT_FEE allows empty duration
        assertTrue(
            isDialogSaveEnabled(
                durationStr = "",
                rateOrFeeInput = "50.0",
                pricingMode = PricingMode.FLAT_FEE,
                isDataChanged = true,
                statusChoice = LogLessonStatusChoice.COMPLETED
            )
        )
    }

    @Test
    fun testDateTimeChangeDetection() {
        val zoneId = java.time.ZoneId.systemDefault()
        val originalZdt = java.time.ZonedDateTime.of(2026, 9, 8, 14, 30, 0, 0, zoneId)
        val originalMillis = originalZdt.toInstant().toEpochMilli()

        // Same date, hour, minute (differing only in seconds/nanos) -> not changed
        val sameDateTimeMillis = originalZdt.withSecond(45).withNano(123456).toInstant().toEpochMilli()
        val initialZdt1 = java.time.Instant.ofEpochMilli(originalMillis).atZone(zoneId)
        val currentZdt1 = java.time.Instant.ofEpochMilli(sameDateTimeMillis).atZone(zoneId)
        val isDateTimeChanged1 = initialZdt1.toLocalDate() != currentZdt1.toLocalDate() ||
            initialZdt1.hour != currentZdt1.hour ||
            initialZdt1.minute != currentZdt1.minute
        assertFalse(isDateTimeChanged1)

        // Date changed -> changed
        val dateChangedZdt = originalZdt.plusDays(1)
        val initialZdt2 = java.time.Instant.ofEpochMilli(originalMillis).atZone(zoneId)
        val currentZdt2 = dateChangedZdt
        val isDateTimeChanged2 = initialZdt2.toLocalDate() != currentZdt2.toLocalDate() ||
            initialZdt2.hour != currentZdt2.hour ||
            initialZdt2.minute != currentZdt2.minute
        assertTrue(isDateTimeChanged2)

        // Time (hour) changed -> changed
        val timeChangedZdt = originalZdt.withHour(15)
        val initialZdt3 = java.time.Instant.ofEpochMilli(originalMillis).atZone(zoneId)
        val currentZdt3 = timeChangedZdt
        val isDateTimeChanged3 = initialZdt3.toLocalDate() != currentZdt3.toLocalDate() ||
            initialZdt3.hour != currentZdt3.hour ||
            initialZdt3.minute != currentZdt3.minute
        assertTrue(isDateTimeChanged3)

        // Time (minute) changed -> changed
        val minuteChangedZdt = originalZdt.withMinute(45)
        val initialZdt4 = java.time.Instant.ofEpochMilli(originalMillis).atZone(zoneId)
        val currentZdt4 = minuteChangedZdt
        val isDateTimeChanged4 = initialZdt4.toLocalDate() != currentZdt4.toLocalDate() ||
            initialZdt4.hour != currentZdt4.hour ||
            initialZdt4.minute != currentZdt4.minute
        assertTrue(isDateTimeChanged4)
    }

    @Test
    fun testConflictingLessonDatesExcludesCurrentLessonAndCancelled() {
        val zoneId = ZoneId.systemDefault()
        val studentId = 10

        val lessonCurrent = Lesson(
            id = 1,
            studentId = studentId,
            date = ZonedDateTime.of(2026, 9, 10, 14, 0, 0, 0, zoneId).toInstant().toEpochMilli(),
            status = LessonStatus.SCHEDULED,
            durationInHours = 1.0,
            notes = null,
            pricingMode = PricingMode.PER_HOUR,
            rateOrFee = 50.0
        )
        val lessonOtherActive = Lesson(
            id = 2,
            studentId = studentId,
            date = ZonedDateTime.of(2026, 9, 12, 15, 0, 0, 0, zoneId).toInstant().toEpochMilli(),
            status = LessonStatus.SCHEDULED,
            durationInHours = 1.0,
            notes = null,
            pricingMode = PricingMode.PER_HOUR,
            rateOrFee = 50.0
        )
        val lessonCancelled = Lesson(
            id = 3,
            studentId = studentId,
            date = ZonedDateTime.of(2026, 9, 14, 16, 0, 0, 0, zoneId).toInstant().toEpochMilli(),
            status = LessonStatus.CANCELLED,
            durationInHours = 1.0,
            notes = null,
            pricingMode = PricingMode.PER_HOUR,
            rateOrFee = 50.0
        )
        val lessonDifferentStudent = Lesson(
            id = 4,
            studentId = 99,
            date = ZonedDateTime.of(2026, 9, 16, 17, 0, 0, 0, zoneId).toInstant().toEpochMilli(),
            status = LessonStatus.SCHEDULED,
            durationInHours = 1.0,
            notes = null,
            pricingMode = PricingMode.PER_HOUR,
            rateOrFee = 50.0
        )

        val existingLessons = listOf(lessonCurrent, lessonOtherActive, lessonCancelled, lessonDifferentStudent)

        val conflictingDates = getConflictingLessonDates(
            existingLessons = existingLessons,
            currentLessonId = lessonCurrent.id,
            studentId = studentId,
            zoneId = zoneId
        )

        // Other active lesson on 2026-09-12 must be conflicting
        assertTrue(conflictingDates.contains(LocalDate.of(2026, 9, 12)))

        // Current lesson's own date (2026-09-10) must NOT be conflicting
        assertFalse(conflictingDates.contains(LocalDate.of(2026, 9, 10)))

        // Cancelled lesson on 2026-09-14 must NOT be conflicting
        assertFalse(conflictingDates.contains(LocalDate.of(2026, 9, 14)))

        // Other student's lesson on 2026-09-16 must NOT be conflicting
        assertFalse(conflictingDates.contains(LocalDate.of(2026, 9, 16)))
    }

    @Test
    fun testIsLessonDateConflicting() {
        val zoneId = ZoneId.systemDefault()
        val conflictingDates = setOf(
            LocalDate.of(2026, 9, 12),
            LocalDate.of(2026, 9, 15)
        )

        val conflictingMillis = ZonedDateTime.of(2026, 9, 12, 10, 0, 0, 0, zoneId).toInstant().toEpochMilli()
        val nonConflictingMillis = ZonedDateTime.of(2026, 9, 13, 10, 0, 0, 0, zoneId).toInstant().toEpochMilli()

        assertTrue(isLessonDateConflicting(conflictingMillis, conflictingDates, zoneId))
        assertFalse(isLessonDateConflicting(nonConflictingMillis, conflictingDates, zoneId))
    }

    @Test
    fun testDateConflictDisablesSave() {
        // When there is a date conflict, saving is disabled even if other fields are valid and data is changed
        assertFalse(
            isDialogSaveEnabled(
                durationStr = "1.0",
                rateOrFeeInput = "50.0",
                pricingMode = PricingMode.PER_HOUR,
                isDataChanged = true,
                statusChoice = LogLessonStatusChoice.SCHEDULED,
                isDateConflict = true
            )
        )

        // Without date conflict, saving is enabled
        assertTrue(
            isDialogSaveEnabled(
                durationStr = "1.0",
                rateOrFeeInput = "50.0",
                pricingMode = PricingMode.PER_HOUR,
                isDataChanged = true,
                statusChoice = LogLessonStatusChoice.SCHEDULED,
                isDateConflict = false
            )
        )
    }

    @Test
    fun testResolveInitialStatusChoice() {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.of(2026, 9, 8)

        val pastMillis = ZonedDateTime.of(2026, 9, 7, 10, 0, 0, 0, zoneId).toInstant().toEpochMilli()
        val todayMillis = ZonedDateTime.of(2026, 9, 8, 10, 0, 0, 0, zoneId).toInstant().toEpochMilli()
        val futureMillis = ZonedDateTime.of(2026, 9, 9, 10, 0, 0, 0, zoneId).toInstant().toEpochMilli()

        // Unlogged past lesson (SCHEDULED, date < today) -> Pre-selects COMPLETED
        assertEquals(
            LogLessonStatusChoice.COMPLETED,
            resolveInitialStatusChoice(LessonStatus.SCHEDULED, pastMillis, today, zoneId)
        )

        // Today lesson (SCHEDULED, date == today) -> Preserves SCHEDULED
        assertEquals(
            LogLessonStatusChoice.SCHEDULED,
            resolveInitialStatusChoice(LessonStatus.SCHEDULED, todayMillis, today, zoneId)
        )

        // Future lesson (SCHEDULED, date > today) -> Preserves SCHEDULED
        assertEquals(
            LogLessonStatusChoice.SCHEDULED,
            resolveInitialStatusChoice(LessonStatus.SCHEDULED, futureMillis, today, zoneId)
        )

        // Already completed lesson on any date -> Preserves COMPLETED
        assertEquals(
            LogLessonStatusChoice.COMPLETED,
            resolveInitialStatusChoice(LessonStatus.COMPLETED, pastMillis, today, zoneId)
        )
        assertEquals(
            LogLessonStatusChoice.COMPLETED,
            resolveInitialStatusChoice(LessonStatus.COMPLETED, todayMillis, today, zoneId)
        )
        assertEquals(
            LogLessonStatusChoice.COMPLETED,
            resolveInitialStatusChoice(LessonStatus.COMPLETED, futureMillis, today, zoneId)
        )

        // Paid lesson on any date -> Preserves COMPLETED
        assertEquals(
            LogLessonStatusChoice.COMPLETED,
            resolveInitialStatusChoice(LessonStatus.PAID, pastMillis, today, zoneId)
        )
        assertEquals(
            LogLessonStatusChoice.COMPLETED,
            resolveInitialStatusChoice(LessonStatus.PAID, todayMillis, today, zoneId)
        )
        assertEquals(
            LogLessonStatusChoice.COMPLETED,
            resolveInitialStatusChoice(LessonStatus.PAID, futureMillis, today, zoneId)
        )

        // Cancelled lesson on any date -> Preserves CANCELLED
        assertEquals(
            LogLessonStatusChoice.CANCELLED,
            resolveInitialStatusChoice(LessonStatus.CANCELLED, pastMillis, today, zoneId)
        )
        assertEquals(
            LogLessonStatusChoice.CANCELLED,
            resolveInitialStatusChoice(LessonStatus.CANCELLED, todayMillis, today, zoneId)
        )
        assertEquals(
            LogLessonStatusChoice.CANCELLED,
            resolveInitialStatusChoice(LessonStatus.CANCELLED, futureMillis, today, zoneId)
        )
    }

    @Test
    fun testResolveLessonActionTextRes() {
        val today = LocalDate.of(2026, 9, 8)
        val pastDate = LocalDate.of(2026, 9, 7)
        val todayDate = LocalDate.of(2026, 9, 8)
        val futureDate = LocalDate.of(2026, 9, 9)

        // Unlogged past lesson -> "Log Lesson"
        assertEquals(
            R.string.calendar_lesson_action_log_lesson,
            resolveLessonActionTextRes(LessonStatus.SCHEDULED, pastDate, today)
        )

        // Scheduled today -> "Edit"
        assertEquals(
            R.string.calendar_lesson_action_edit,
            resolveLessonActionTextRes(LessonStatus.SCHEDULED, todayDate, today)
        )

        // Scheduled future -> "Edit"
        assertEquals(
            R.string.calendar_lesson_action_edit,
            resolveLessonActionTextRes(LessonStatus.SCHEDULED, futureDate, today)
        )

        // Completed lesson (past, today, future) -> "Edit"
        assertEquals(
            R.string.calendar_lesson_action_edit,
            resolveLessonActionTextRes(LessonStatus.COMPLETED, pastDate, today)
        )
        assertEquals(
            R.string.calendar_lesson_action_edit,
            resolveLessonActionTextRes(LessonStatus.COMPLETED, todayDate, today)
        )
        assertEquals(
            R.string.calendar_lesson_action_edit,
            resolveLessonActionTextRes(LessonStatus.COMPLETED, futureDate, today)
        )

        // Paid lesson (past, today, future) -> "Edit"
        assertEquals(
            R.string.calendar_lesson_action_edit,
            resolveLessonActionTextRes(LessonStatus.PAID, pastDate, today)
        )
        assertEquals(
            R.string.calendar_lesson_action_edit,
            resolveLessonActionTextRes(LessonStatus.PAID, todayDate, today)
        )
        assertEquals(
            R.string.calendar_lesson_action_edit,
            resolveLessonActionTextRes(LessonStatus.PAID, futureDate, today)
        )

        // Cancelled lesson (past, today, future) -> "Edit"
        assertEquals(
            R.string.calendar_lesson_action_edit,
            resolveLessonActionTextRes(LessonStatus.CANCELLED, pastDate, today)
        )
        assertEquals(
            R.string.calendar_lesson_action_edit,
            resolveLessonActionTextRes(LessonStatus.CANCELLED, todayDate, today)
        )
        assertEquals(
            R.string.calendar_lesson_action_edit,
            resolveLessonActionTextRes(LessonStatus.CANCELLED, futureDate, today)
        )
    }
}
