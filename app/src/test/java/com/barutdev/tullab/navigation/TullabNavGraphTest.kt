package com.barutdev.tullab.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.navigation.NavDestination
import androidx.navigation.compose.ComposeNavigator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TullabNavGraphTest {

    @Test
    fun resolveSlideDirection_returnsLeft_whenNavigatingForwardBetweenBottomDestinations() {
        val direction = resolveSlideDirection(
            initialRoute = TullabDestination.Dashboard.route,
            targetRoute = TullabDestination.Calendar.route
        )

        assertEquals(
            AnimatedContentTransitionScope.SlideDirection.Left,
            direction
        )
    }

    @Test
    fun resolveSlideDirection_returnsRight_whenNavigatingBackward() {
        val direction = resolveSlideDirection(
            initialRoute = TullabDestination.Homework.route,
            targetRoute = TullabDestination.Calendar.route
        )

        assertEquals(
            AnimatedContentTransitionScope.SlideDirection.Right,
            direction
        )
    }

    @Test
    fun resolveSlideDirection_returnsNull_forNonBottomDestinations() {
        val direction = resolveSlideDirection(
            initialRoute = TullabDestination.StudentList.route,
            targetRoute = TullabDestination.Settings.route
        )

        assertNull(direction)
    }

    @Test
    fun shouldShowBottomBar_returnsTrue_forStudentScopedDestination() {
        val shouldShow = shouldShowBottomBar(
            currentDestination = destination(TullabDestination.Calendar.route)
        )

        assertTrue(shouldShow)
    }

    @Test
    fun shouldShowBottomBar_returnsFalse_forNonBottomDestination() {
        val shouldShow = shouldShowBottomBar(
            currentDestination = destination(TullabDestination.StudentList.route)
        )

        assertFalse(shouldShow)
    }



    private fun destination(route: String?): NavDestination? {
        if (route == null) return null
        val navigator = ComposeNavigator()
        return ComposeNavigator.Destination(navigator) {}.apply {
            this.route = route
        }
    }
}
