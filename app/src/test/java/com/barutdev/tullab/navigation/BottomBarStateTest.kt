package com.barutdev.tullab.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BottomBarStateTest {

    @Test
    fun shouldRestore_returnsTrue_whenStudentMatchesRecordedValue() {
        val state = BottomBarState()
        state.record(TullabDestination.Calendar, studentId = 12)

        assertTrue(state.shouldRestore(TullabDestination.Calendar, 12))
    }

    @Test
    fun shouldRestore_returnsFalse_whenStudentDiffers() {
        val state = BottomBarState()
        state.record(TullabDestination.Calendar, studentId = 12)

        assertFalse(state.shouldRestore(TullabDestination.Calendar, 7))
    }

    @Test
    fun clear_removesStoredState() {
        val state = BottomBarState()
        state.record(TullabDestination.Homework, studentId = 5)
        state.clear(TullabDestination.Homework)

        assertFalse(state.shouldRestore(TullabDestination.Homework, 5))
        assertNull(state.lastStudentId(TullabDestination.Homework))
    }

    @Test
    fun lastStudentId_returnsMostRecentId() {
        val state = BottomBarState()
        state.record(TullabDestination.Dashboard, studentId = 1)
        state.record(TullabDestination.Dashboard, studentId = 8)

        assertEquals(8, state.lastStudentId(TullabDestination.Dashboard))
    }

    @Test
    fun lastKnownStudentId_tracksLatestStudentId() {
        val state = BottomBarState()

        assertNull(state.lastKnownStudentId())

        state.record(TullabDestination.Calendar, studentId = 4)
        assertEquals(4, state.lastKnownStudentId())

        state.record(TullabDestination.Homework, studentId = 9)
        assertEquals(9, state.lastKnownStudentId())

        state.clear(TullabDestination.Homework)
        assertEquals(9, state.lastKnownStudentId())
    }
}
