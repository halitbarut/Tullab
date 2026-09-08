package com.barutdev.tullab.data.local

import com.barutdev.tullab.domain.model.HomeworkStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeworkStatusConverterTest {

    private val converter = HomeworkStatusConverter()

    @Test
    fun `fromStatus serializes status to name`() {
        assertEquals("PENDING", converter.fromStatus(HomeworkStatus.PENDING))
        assertEquals("COMPLETED", converter.fromStatus(HomeworkStatus.COMPLETED))
        assertEquals("CANCELLED", converter.fromStatus(HomeworkStatus.CANCELLED))
    }

    @Test
    fun `toStatus deserializes valid status names`() {
        assertEquals(HomeworkStatus.PENDING, converter.toStatus("PENDING"))
        assertEquals(HomeworkStatus.COMPLETED, converter.toStatus("COMPLETED"))
        assertEquals(HomeworkStatus.CANCELLED, converter.toStatus("CANCELLED"))
    }

    @Test
    fun `toStatus maps legacy OVERDUE string to PENDING`() {
        assertEquals(HomeworkStatus.PENDING, converter.toStatus("OVERDUE"))
    }

    @Test
    fun `toStatus falls back to PENDING for unknown strings`() {
        assertEquals(HomeworkStatus.PENDING, converter.toStatus("UNKNOWN_STATUS"))
    }
}
