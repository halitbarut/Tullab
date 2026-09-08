package com.barutdev.tullab.ui.screens.calendar

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.barutdev.tullab.R
import com.barutdev.tullab.domain.model.Homework
import com.barutdev.tullab.domain.model.HomeworkStatus
import com.barutdev.tullab.ui.theme.TullabTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class CalendarMidnightTransitionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private class TestLifecycleOwner(initialState: Lifecycle.State = Lifecycle.State.RESUMED) : LifecycleOwner {
        private val registry = LifecycleRegistry(this).apply {
            currentState = initialState
        }
        override val lifecycle: Lifecycle = registry

        fun handleLifecycleEvent(event: Lifecycle.Event) {
            registry.handleLifecycleEvent(event)
        }
    }

    private fun getString(id: Int): String {
        return ApplicationProvider.getApplicationContext<android.content.Context>().getString(id)
    }

    @Test
    fun testMidnightTransition_updatesUtcToday_andTriggersOverdueBadge() {
        val selectedDate = LocalDate.of(2026, 9, 8)
        val currentMonth = YearMonth.of(2026, 9)
        val dueEpochMillis = selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        val pendingHomework = Homework(
            id = 101,
            studentId = 1,
            title = "Algebra Assignment",
            description = "Complete exercises 1 to 10",
            creationDate = dueEpochMillis,
            dueDate = dueEpochMillis,
            status = HomeworkStatus.PENDING,
            performanceNotes = null
        )

        // Start 1000ms before midnight UTC
        var currentInstant = Instant.parse("2026-09-08T23:59:59.000Z")

        composeTestRule.setContent {
            TullabTheme {
                CalendarScreenContent(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    lessons = emptyList(),
                    homework = listOf(pendingHomework),
                    currencyCode = "USD",
                    onPreviousMonth = {},
                    onNextMonth = {},
                    onSelectDate = {},
                    onLogLessonClick = {},
                    onLessonMarkAsPaidClick = {},
                    onRevertPaymentClick = {},
                    onToggleHomeworkStatus = {},
                    onHomeworkDetailsClick = {},
                    zoneId = ZoneOffset.UTC,
                    timeProvider = { currentInstant }
                )
            }
        }

        val overdueBadgeText = getString(R.string.homework_badge_overdue)

        // Initially on 2026-09-08, homework due 2026-09-08 is NOT overdue
        composeTestRule.onAllNodesWithText(overdueBadgeText).assertCountEquals(0)

        // Advance time past midnight UTC and advance the test clock by the midnight delay
        currentInstant = Instant.parse("2026-09-09T00:00:01.000Z")
        composeTestRule.mainClock.advanceTimeBy(1500L)
        composeTestRule.waitForIdle()

        // After midnight transition, homework is now overdue and badge is displayed
        composeTestRule.onNodeWithText(overdueBadgeText).assertIsDisplayed()
    }

    @Test
    fun testOnResume_refreshesDates_andRecalculatesStatus() {
        val testLifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        val selectedDate = LocalDate.of(2026, 9, 8)
        val currentMonth = YearMonth.of(2026, 9)
        val dueEpochMillis = selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        val pendingHomework = Homework(
            id = 102,
            studentId = 1,
            title = "Geometry Assignment",
            description = "Proofs 1 to 5",
            creationDate = dueEpochMillis,
            dueDate = dueEpochMillis,
            status = HomeworkStatus.PENDING,
            performanceNotes = null
        )

        // Start midday on Sept 8
        var currentInstant = Instant.parse("2026-09-08T12:00:00.000Z")

        composeTestRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides testLifecycleOwner) {
                TullabTheme {
                    CalendarScreenContent(
                        currentMonth = currentMonth,
                        selectedDate = selectedDate,
                        lessons = emptyList(),
                        homework = listOf(pendingHomework),
                        currencyCode = "USD",
                        onPreviousMonth = {},
                        onNextMonth = {},
                        onSelectDate = {},
                        onLogLessonClick = {},
                        onLessonMarkAsPaidClick = {},
                        onRevertPaymentClick = {},
                        onToggleHomeworkStatus = {},
                        onHomeworkDetailsClick = {},
                        zoneId = ZoneOffset.UTC,
                        timeProvider = { currentInstant }
                    )
                }
            }
        }

        val overdueBadgeText = getString(R.string.homework_badge_overdue)

        // Initially not overdue
        composeTestRule.onAllNodesWithText(overdueBadgeText).assertCountEquals(0)

        // Simulate app pausing, time advancing to next day, and app resuming
        testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        currentInstant = Instant.parse("2026-09-09T08:00:00.000Z")
        testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        composeTestRule.waitForIdle()

        // Homework is now overdue and badge is displayed
        composeTestRule.onNodeWithText(overdueBadgeText).assertIsDisplayed()
    }
}
