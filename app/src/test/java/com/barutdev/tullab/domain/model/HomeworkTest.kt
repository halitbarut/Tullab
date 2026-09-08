package com.barutdev.tullab.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class HomeworkTest {

    private val today = LocalDate.of(2026, 9, 8)
    private val yesterdayEpoch = today.minusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    private val todayEpoch = today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    private val tomorrowEpoch = today.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    private fun createHomework(status: HomeworkStatus, dueDate: Long): Homework {
        return Homework(
            id = 1,
            studentId = 10,
            title = "Math Assignment",
            description = "Algebra exercises",
            creationDate = today.minusDays(5).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            dueDate = dueDate,
            status = status,
            performanceNotes = null
        )
    }

    @Test
    fun `isOverdue returns true when status is PENDING and due date is in the past`() {
        val homework = createHomework(HomeworkStatus.PENDING, yesterdayEpoch)
        assertTrue(homework.isOverdue(today))
    }

    @Test
    fun `isOverdue returns false when status is PENDING and due date is today`() {
        val homework = createHomework(HomeworkStatus.PENDING, todayEpoch)
        assertFalse(homework.isOverdue(today))
    }

    @Test
    fun `isOverdue returns false when status is PENDING and due date is in the future`() {
        val homework = createHomework(HomeworkStatus.PENDING, tomorrowEpoch)
        assertFalse(homework.isOverdue(today))
    }

    @Test
    fun `isOverdue returns false when status is COMPLETED even if due date is in the past`() {
        val homework = createHomework(HomeworkStatus.COMPLETED, yesterdayEpoch)
        assertFalse(homework.isOverdue(today))
    }

    @Test
    fun `isOverdue returns false when status is CANCELLED even if due date is in the past`() {
        val homework = createHomework(HomeworkStatus.CANCELLED, yesterdayEpoch)
        assertFalse(homework.isOverdue(today))
    }
}
