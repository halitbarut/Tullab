package com.barutdev.tullab.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TullabDestinationTest {

    @Test
    fun createRoute_buildsParameterizedRoute() {
        assertEquals("dashboard/42", TullabDestination.Dashboard.createRoute(42))
        assertEquals("calendar/7", TullabDestination.Calendar.createRoute(7))
        assertEquals("homework/3", TullabDestination.Homework.createRoute(3))
    }

    @Test
    fun studentScopedFromRoute_resolvesKnownRoute() {
        assertEquals(
            TullabDestination.Calendar,
            TullabDestination.studentScopedFromRoute(TullabDestination.Calendar.route)
        )
    }

    @Test
    fun studentScopedFromRoute_returnsNull_forUnknownRoute() {
        assertNull(TullabDestination.studentScopedFromRoute(TullabDestination.StudentList.route))
    }

    @Test
    fun bottomBarDestinations_preserveOrder() {
        val items = TullabDestination.bottomBarDestinations
        assertEquals(0, items.indexOf(TullabDestination.Dashboard))
        assertEquals(1, items.indexOf(TullabDestination.Calendar))
        assertEquals(2, items.indexOf(TullabDestination.Homework))
        assertEquals(3, items.size)
    }
}
