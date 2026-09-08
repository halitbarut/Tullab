package com.barutdev.tullab.ui.screens.bulk_schedule

import androidx.lifecycle.SavedStateHandle
import com.barutdev.tullab.domain.model.BulkLessonCandidate
import com.barutdev.tullab.domain.model.BulkScheduleMode
import com.barutdev.tullab.domain.model.BulkScheduleResult
import com.barutdev.tullab.domain.usecase.lesson.CalculateBulkLessonCandidatesUseCase
import com.barutdev.tullab.domain.usecase.lesson.CreateBulkLessonsUseCase
import com.barutdev.tullab.domain.usecase.lesson.UndoBulkLessonsUseCase
import com.barutdev.tullab.navigation.STUDENT_ID_ARG
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.DayOfWeek
import com.barutdev.tullab.domain.model.WeeklyRoutineEndCondition
import com.barutdev.tullab.domain.repository.HomeworkRepository
import com.barutdev.tullab.domain.repository.LessonRepository
import com.barutdev.tullab.domain.usecase.notification.CancelNotificationAlarmsUseCase
import com.barutdev.tullab.domain.usecase.notification.ScheduleNotificationAlarmsUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class BulkScheduleViewModelTest {

    private lateinit var calculateCandidatesUseCase: CalculateBulkLessonCandidatesUseCase
    private lateinit var createLessonsUseCase: CreateBulkLessonsUseCase
    private lateinit var undoUseCase: UndoBulkLessonsUseCase
    private lateinit var scheduleNotificationAlarmsUseCase: ScheduleNotificationAlarmsUseCase
    private lateinit var cancelNotificationAlarmsUseCase: CancelNotificationAlarmsUseCase
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: BulkScheduleViewModel

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var lessonRepository: LessonRepository
    private lateinit var homeworkRepository: HomeworkRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        calculateCandidatesUseCase = mockk(relaxed = true)
        createLessonsUseCase = mockk(relaxed = true)
        undoUseCase = mockk(relaxed = true)
        lessonRepository = mockk(relaxed = true)
        homeworkRepository = mockk(relaxed = true)
        scheduleNotificationAlarmsUseCase = mockk(relaxed = true)
        cancelNotificationAlarmsUseCase = mockk(relaxed = true)
        savedStateHandle = SavedStateHandle(mapOf(STUDENT_ID_ARG to 1))
        
        viewModel = BulkScheduleViewModel(
            savedStateHandle,
            calculateCandidatesUseCase,
            createLessonsUseCase,
            undoUseCase,
            lessonRepository,
            homeworkRepository,
            scheduleNotificationAlarmsUseCase,
            cancelNotificationAlarmsUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has correct student ID`() {
        val state = viewModel.state.value
        assertEquals(1, state.draft.studentId)
        assertEquals(BulkScheduleMode.CALENDAR_GRID, state.draft.mode)
    }

    @Test
    fun `OnModeChanged updates state and generates preview`() = runTest {
        viewModel.onEvent(BulkScheduleEvent.OnModeChanged(BulkScheduleMode.WEEKLY_ROUTINE))
        advanceUntilIdle()
        
        assertEquals(BulkScheduleMode.WEEKLY_ROUTINE, viewModel.state.value.draft.mode)
    }

    @Test
    fun `OnDateToggled adds and removes dates`() = runTest {
        val today = LocalDate.now()
        
        // Add date
        viewModel.onEvent(BulkScheduleEvent.OnDateToggled(today))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.draft.selectedDates.contains(today))
        
        // Remove date
        viewModel.onEvent(BulkScheduleEvent.OnDateToggled(today))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.draft.selectedDates.isEmpty())
    }

    @Test
    fun `OnDefaultStartTimeChanged updates default time`() = runTest {
        val newTime = java.time.LocalTime.of(14, 30)
        viewModel.onEvent(BulkScheduleEvent.OnDefaultStartTimeChanged(newTime))
        advanceUntilIdle()
        
        assertEquals(newTime, viewModel.state.value.draft.defaultStartTime)
    }

    @Test
    fun `OnCustomDayTimeChanged updates custom times map`() = runTest {
        val today = LocalDate.now()
        val customTime = java.time.LocalTime.of(18, 0)
        
        // Set custom time
        viewModel.onEvent(BulkScheduleEvent.OnCustomDayTimeChanged(today, customTime))
        advanceUntilIdle()
        assertEquals(customTime, viewModel.state.value.draft.customDayTimes[today])
        
        // Remove custom time
        viewModel.onEvent(BulkScheduleEvent.OnCustomDayTimeChanged(today, null))
        advanceUntilIdle()
        assertNull(viewModel.state.value.draft.customDayTimes[today])
    }

    @Test
    fun `OnRoutineStartDateChanged updates draft start date`() = runTest {
        val newDate = LocalDate.now().plusDays(3)
        viewModel.onEvent(BulkScheduleEvent.OnRoutineStartDateChanged(newDate))
        advanceUntilIdle()
        assertEquals(newDate, viewModel.state.value.draft.routineStartDate)
    }

    @Test
    fun `OnRoutineEndConditionChanged updates condition type`() = runTest {
        viewModel.onEvent(BulkScheduleEvent.OnRoutineEndConditionChanged(isByEndDate = true))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.draft.endCondition is WeeklyRoutineEndCondition.ByEndDate)

        viewModel.onEvent(BulkScheduleEvent.OnRoutineEndConditionChanged(isByEndDate = false))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.draft.endCondition is WeeklyRoutineEndCondition.ByTargetCount)
    }

    @Test
    fun `OnRoutineEndDateChanged updates end date in condition`() = runTest {
        viewModel.onEvent(BulkScheduleEvent.OnRoutineEndConditionChanged(isByEndDate = true))
        advanceUntilIdle()
        
        val newEndDate = LocalDate.now().plusDays(40)
        viewModel.onEvent(BulkScheduleEvent.OnRoutineEndDateChanged(newEndDate))
        advanceUntilIdle()
        
        val condition = viewModel.state.value.draft.endCondition as WeeklyRoutineEndCondition.ByEndDate
        assertEquals(newEndDate, condition.endDate)
    }

    @Test
    fun `OnRoutineTargetCountChanged updates targetCountInput and draft count even when exceeding 30`() = runTest {
        viewModel.onEvent(BulkScheduleEvent.OnModeChanged(BulkScheduleMode.WEEKLY_ROUTINE))
        viewModel.onEvent(BulkScheduleEvent.OnDayOfWeekToggled(DayOfWeek.MONDAY))
        coEvery { calculateCandidatesUseCase(any(), any()) } returns (1..40).map { mockk(relaxed = true) }

        viewModel.onEvent(BulkScheduleEvent.OnRoutineTargetCountChanged("15"))
        advanceUntilIdle()
        assertEquals("15", viewModel.state.value.targetCountInput)
        val condition = viewModel.state.value.draft.endCondition as WeeklyRoutineEndCondition.ByTargetCount
        assertEquals(15, condition.targetCount)

        viewModel.onEvent(BulkScheduleEvent.OnRoutineTargetCountChanged("40"))
        advanceUntilIdle()
        val currentCondition = viewModel.state.value.draft.endCondition as WeeklyRoutineEndCondition.ByTargetCount
        assertEquals(40, currentCondition.targetCount)
        assertEquals("40", viewModel.state.value.targetCountInput)
        assertTrue(viewModel.state.value.isCapReached)
    }

    @Test
    fun `OnRoutineEndConditionChanged toggling retains targetCountInput instead of hardcoding 4`() = runTest {
        viewModel.onEvent(BulkScheduleEvent.OnModeChanged(BulkScheduleMode.WEEKLY_ROUTINE))
        viewModel.onEvent(BulkScheduleEvent.OnDayOfWeekToggled(DayOfWeek.MONDAY))
        coEvery { calculateCandidatesUseCase(any(), any()) } returns (1..50).map { mockk(relaxed = true) }

        viewModel.onEvent(BulkScheduleEvent.OnRoutineTargetCountChanged("50"))
        advanceUntilIdle()
        assertEquals("50", viewModel.state.value.targetCountInput)
        assertEquals(50, (viewModel.state.value.draft.endCondition as WeeklyRoutineEndCondition.ByTargetCount).targetCount)

        // Switch to ByEndDate
        viewModel.onEvent(BulkScheduleEvent.OnRoutineEndConditionChanged(isByEndDate = true))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.draft.endCondition is WeeklyRoutineEndCondition.ByEndDate)
        assertEquals("50", viewModel.state.value.targetCountInput)

        // Switch back to ByTargetCount
        viewModel.onEvent(BulkScheduleEvent.OnRoutineEndConditionChanged(isByEndDate = false))
        advanceUntilIdle()
        val condition = viewModel.state.value.draft.endCondition as WeeklyRoutineEndCondition.ByTargetCount
        assertEquals(50, condition.targetCount)
        assertEquals("50", viewModel.state.value.targetCountInput)
        assertTrue(viewModel.state.value.isCapReached)
    }

    @Test
    fun `OnUseCustomRateToggled updates draft`() = runTest {
        viewModel.onEvent(BulkScheduleEvent.OnUseCustomRateToggled(true))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.draft.useCustomRate)
        
        viewModel.onEvent(BulkScheduleEvent.OnUseCustomRateToggled(false))
        advanceUntilIdle()
        assertFalse(viewModel.state.value.draft.useCustomRate)
    }

    @Test
    fun `OnCustomRateChanged updates input and draft if valid`() = runTest {
        viewModel.onEvent(BulkScheduleEvent.OnCustomRateChanged("50.5"))
        advanceUntilIdle()
        assertEquals("50.5", viewModel.state.value.customRateInput)
        assertEquals(50.5, viewModel.state.value.draft.customRate)
        
        viewModel.onEvent(BulkScheduleEvent.OnCustomRateChanged("invalid"))
        advanceUntilIdle()
        assertEquals("invalid", viewModel.state.value.customRateInput)
        assertEquals(null, viewModel.state.value.draft.customRate) // clears value if invalid
    }

    @Test
    fun `ConfirmSchedule triggers create lessons if candidates are valid`() = runTest {
        val today = LocalDate.now()
        
        coEvery { calculateCandidatesUseCase(any(), any()) } returns listOf(
            BulkLessonCandidate(today, java.time.LocalTime.of(10, 0), 100L, isConflict = false, isPast = false)
        )
        
        viewModel.onEvent(BulkScheduleEvent.OnDateToggled(today))
        advanceUntilIdle()
        
        coEvery { createLessonsUseCase(any(), any()) } returns Result.success(
            BulkScheduleResult(studentId = 1, createdLessons = listOf(mockk(relaxed = true)), skippedCount = 0, hasPastLessons = false)
        )
        
        viewModel.onEvent(BulkScheduleEvent.ConfirmSchedule)
        advanceUntilIdle()
        
        val state = viewModel.state.value
        assertTrue(state.snackbarMessage is SnackbarState.Success)
        val successState = state.snackbarMessage as SnackbarState.Success
        assertEquals(1, successState.createdCount)
        assertTrue(successState.isUndoable)
    }

    @Test
    fun `ConfirmSchedule shows warning if past lessons are present`() = runTest {
        val pastDate = LocalDate.now().minusDays(1)
        
        coEvery { calculateCandidatesUseCase(any(), any()) } returns listOf(
            BulkLessonCandidate(pastDate, java.time.LocalTime.of(10, 0), 100L, isConflict = false, isPast = true)
        )
        
        viewModel.onEvent(BulkScheduleEvent.OnDateToggled(pastDate))
        advanceUntilIdle()
        
        viewModel.onEvent(BulkScheduleEvent.ConfirmSchedule)
        advanceUntilIdle()
        
        assertTrue(viewModel.state.value.showPastDateWarning)
        
        // then proceed
        coEvery { createLessonsUseCase(any(), any()) } returns Result.success(
            BulkScheduleResult(studentId = 1, createdLessons = listOf(mockk(relaxed = true)), skippedCount = 0, hasPastLessons = true)
        )
        viewModel.onEvent(BulkScheduleEvent.ProceedWithPastDates)
        advanceUntilIdle()
        
        val state = viewModel.state.value
        assertTrue(state.snackbarMessage is SnackbarState.Success)
    }

    @Test
    fun `isCapReached is set to true when 31 candidates are generated`() = runTest {
        val today = LocalDate.now()
        val candidates = (1..31).map { 
            BulkLessonCandidate(today.plusDays(it.toLong()), java.time.LocalTime.of(10, 0), it.toLong(), isConflict = false, isPast = false)
        }
        
        coEvery { calculateCandidatesUseCase(any(), any()) } returns candidates
        
        viewModel.onEvent(BulkScheduleEvent.OnDateToggled(today))
        advanceUntilIdle()
        
        assertTrue(viewModel.state.value.isCapReached)
    }
}
