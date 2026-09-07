package com.barutdev.tullab.ui.screens.calendar

import androidx.lifecycle.SavedStateHandle
import com.barutdev.tullab.domain.model.Homework
import com.barutdev.tullab.domain.model.HomeworkStatus
import com.barutdev.tullab.domain.model.Lesson
import com.barutdev.tullab.domain.model.LessonStatus
import com.barutdev.tullab.domain.repository.HomeworkRepository
import com.barutdev.tullab.domain.repository.LessonRepository
import com.barutdev.tullab.domain.repository.StudentRepository
import com.barutdev.tullab.domain.repository.UserPreferencesRepository
import com.barutdev.tullab.domain.repository.PaymentRepository
import com.barutdev.tullab.domain.usecase.notification.CancelNotificationAlarmsUseCase
import com.barutdev.tullab.domain.usecase.notification.ScheduleNotificationAlarmsUseCase
import com.barutdev.tullab.domain.usecase.lesson.UndoBulkLessonsUseCase
import com.barutdev.tullab.domain.model.BatchUndoSession
import com.barutdev.tullab.navigation.STUDENT_ID_ARG
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var studentRepository: StudentRepository
    private lateinit var lessonRepository: LessonRepository
    private lateinit var homeworkRepository: HomeworkRepository
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var scheduleNotificationAlarmsUseCase: ScheduleNotificationAlarmsUseCase
    private lateinit var cancelNotificationAlarmsUseCase: CancelNotificationAlarmsUseCase
    private lateinit var undoBulkLessonsUseCase: UndoBulkLessonsUseCase
    private lateinit var paymentRepository: PaymentRepository
    private lateinit var viewModel: CalendarViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        savedStateHandle = SavedStateHandle(mapOf(STUDENT_ID_ARG to 1))
        studentRepository = mockk(relaxed = true)
        lessonRepository = mockk(relaxed = true)
        homeworkRepository = mockk(relaxed = true)
        userPreferencesRepository = mockk(relaxed = true)
        scheduleNotificationAlarmsUseCase = mockk(relaxed = true)
        cancelNotificationAlarmsUseCase = mockk(relaxed = true)
        undoBulkLessonsUseCase = mockk(relaxed = true)
        paymentRepository = mockk(relaxed = true)

        // Make state flows emit correctly
        every { studentRepository.getStudentById(1) } returns flowOf(null)
        every { lessonRepository.getLessonsForStudent(1) } returns flowOf(emptyList())
        every { homeworkRepository.getHomeworkForStudent(1) } returns flowOf(emptyList())

        viewModel = CalendarViewModel(
            savedStateHandle = savedStateHandle,
            studentRepository = studentRepository,
            lessonRepository = lessonRepository,
            homeworkRepository = homeworkRepository,
            userPreferencesRepository = userPreferencesRepository,
            paymentRepository = paymentRepository,
            scheduleNotificationAlarmsUseCase = scheduleNotificationAlarmsUseCase,
            cancelNotificationAlarmsUseCase = cancelNotificationAlarmsUseCase,
            undoBulkLessonsUseCase = undoBulkLessonsUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun homeworkWithStatus(status: HomeworkStatus) = Homework(
        id = 10,
        studentId = 1,
        title = "Test",
        description = "",
        creationDate = 0L,
        dueDate = 0L,
        status = status,
        performanceNotes = null
    )

    @Test
    fun `toggleHomeworkStatus from PENDING changes to COMPLETED`() = runTest {
        val homework = homeworkWithStatus(HomeworkStatus.PENDING)
        coEvery { homeworkRepository.updateHomework(any()) } returns Unit

        viewModel.toggleHomeworkStatus(homework)
        advanceUntilIdle()

        coVerify { 
            homeworkRepository.updateHomework(match { it.status == HomeworkStatus.COMPLETED }) 
        }
    }

    @Test
    fun `toggleHomeworkStatus from COMPLETED changes to PENDING`() = runTest {
        val homework = homeworkWithStatus(HomeworkStatus.COMPLETED)
        coEvery { homeworkRepository.updateHomework(any()) } returns Unit

        viewModel.toggleHomeworkStatus(homework)
        advanceUntilIdle()

        coVerify { 
            homeworkRepository.updateHomework(match { it.status == HomeworkStatus.PENDING }) 
        }
    }

    @Test
    fun `toggleHomeworkStatus from OVERDUE changes to COMPLETED`() = runTest {
        val homework = homeworkWithStatus(HomeworkStatus.OVERDUE)
        coEvery { homeworkRepository.updateHomework(any()) } returns Unit

        viewModel.toggleHomeworkStatus(homework)
        advanceUntilIdle()

        coVerify { 
            homeworkRepository.updateHomework(match { it.status == HomeworkStatus.COMPLETED }) 
        }
    }

    @Test
    fun `toggleHomeworkStatus from CANCELLED does nothing`() = runTest {
        val homework = homeworkWithStatus(HomeworkStatus.CANCELLED)
        
        viewModel.toggleHomeworkStatus(homework)
        advanceUntilIdle()

        coVerify(exactly = 0) { homeworkRepository.updateHomework(any()) }
    }

    @Test
    fun `onMarkLessonAsPaidClicked sets pendingLessonForPayment if SCHEDULED`() = runTest {
        val lesson = Lesson(id = 1, studentId = 1, date = 0L, status = LessonStatus.SCHEDULED, durationInHours = null, notes = null, pricingMode = com.barutdev.tullab.domain.model.PricingMode.PER_HOUR, rateOrFee = 50.0)
        every { userPreferencesRepository.userPreferences } returns flowOf(
            com.barutdev.tullab.domain.model.UserPreferences(
                isDarkMode = false,
                languageCode = "en",
                currencyCode = "USD",
                defaultHourlyRate = 50.0,
                lessonRemindersEnabled = false,
                logReminderEnabled = false,
                lessonReminderHour = 18,
                lessonReminderMinute = 0,
                logReminderHour = 20,
                logReminderMinute = 0
            )
        )
        every { studentRepository.getStudentById(1) } returns flowOf(
            com.barutdev.tullab.domain.model.Student(
                id = 1,
                fullName = "Test User",
                hourlyRate = 50.0
            )
        )

        viewModel.onMarkLessonAsPaidClicked(lesson)
        advanceUntilIdle()

        assertEquals(lesson, viewModel.pendingLessonForPayment.value)
        assertEquals(false, viewModel.requiresFeePrompt.value)
    }

    @Test
    fun `onMarkLessonAsPaidClicked directly pays if COMPLETED and has duration and rate`() = runTest {
        val lesson = Lesson(id = 1, studentId = 1, date = 0L, status = LessonStatus.COMPLETED, durationInHours = 1.0, notes = null, pricingMode = com.barutdev.tullab.domain.model.PricingMode.PER_HOUR, rateOrFee = 50.0)
        every { userPreferencesRepository.userPreferences } returns flowOf(
            com.barutdev.tullab.domain.model.UserPreferences(
                isDarkMode = false,
                languageCode = "en",
                currencyCode = "USD",
                defaultHourlyRate = 50.0,
                lessonRemindersEnabled = false,
                logReminderEnabled = false,
                lessonReminderHour = 18,
                lessonReminderMinute = 0,
                logReminderHour = 20,
                logReminderMinute = 0
            )
        )
        every { studentRepository.getStudentById(1) } returns flowOf(
            com.barutdev.tullab.domain.model.Student(
                id = 1,
                fullName = "Test User",
                hourlyRate = 50.0
            )
        )

        viewModel.onMarkLessonAsPaidClicked(lesson)
        advanceUntilIdle()

        coVerify { paymentRepository.markLessonAsPaid(lesson.id, lesson.durationInHours, null) }
        assertEquals(null, viewModel.pendingLessonForPayment.value)
    }

    @Test
    fun `onConfirmRevertPayment calls paymentRepository and clears state`() = runTest {
        val lesson = Lesson(id = 1, studentId = 1, date = 0L, status = LessonStatus.PAID, durationInHours = 1.0, notes = null, pricingMode = com.barutdev.tullab.domain.model.PricingMode.PER_HOUR, rateOrFee = 0.0)
        coEvery { paymentRepository.revertLessonPayment(1) } returns Unit

        viewModel.onRevertLessonPaymentClicked(lesson)
        assertEquals(lesson, viewModel.lessonToRevert.value)

        viewModel.onConfirmRevertPayment()
        advanceUntilIdle()

        coVerify { paymentRepository.revertLessonPayment(1) }
        assertEquals(null, viewModel.lessonToRevert.value)
    }

    @Test
    fun `onSaveScheduledLesson calls updateLesson and dismisses dialog`() = runTest {
        val lesson = Lesson(id = 1, studentId = 1, date = 0L, status = LessonStatus.SCHEDULED, durationInHours = null, notes = null, pricingMode = com.barutdev.tullab.domain.model.PricingMode.PER_HOUR, rateOrFee = 50.0)
        coEvery { lessonRepository.updateLesson(any()) } returns Unit

        viewModel.onLogLessonClicked(lesson) // Opens the dialog
        assertEquals(lesson, viewModel.lessonToLog.value)

        viewModel.onSaveScheduledLesson(
            lesson = lesson,
            duration = "1.5",
            notes = "Test Note",
            pricingMode = com.barutdev.tullab.domain.model.PricingMode.FLAT_FEE,
            rateOrFee = "60.0"
        )
        advanceUntilIdle()

        coVerify {
            lessonRepository.updateLesson(match {
                it.id == lesson.id &&
                it.durationInHours == 1.5 &&
                it.notes == "Test Note" &&
                it.pricingMode == com.barutdev.tullab.domain.model.PricingMode.FLAT_FEE &&
                it.rateOrFee == 60.0 &&
                it.status == LessonStatus.SCHEDULED // Should remain SCHEDULED
            })
        }
        assertEquals(null, viewModel.lessonToLog.value)
    }

    @Test
    fun `undoBulkLessons triggers use case and clears state on success`() = runTest {
        val session = BatchUndoSession(1, listOf(10, 11))
        viewModel.setBatchUndoSession(session)
        assertEquals(session, viewModel.batchUndoSession.value)

        coEvery { undoBulkLessonsUseCase(session) } returns Result.success(Unit)
        
        viewModel.undoBulkLessons()
        advanceUntilIdle()

        coVerify { undoBulkLessonsUseCase(session) }
        assertEquals(null, viewModel.batchUndoSession.value)
    }

    @Test
    fun `onSaveLessonDetails uncompletes completed unpaid lesson and persists duration`() = runTest {
        val completedLesson = Lesson(
            id = 10,
            studentId = 1,
            date = 1000L,
            status = LessonStatus.COMPLETED,
            durationInHours = 1.0,
            notes = "Old note",
            pricingMode = com.barutdev.tullab.domain.model.PricingMode.PER_HOUR,
            rateOrFee = 50.0
        )
        coEvery { lessonRepository.updateLesson(any()) } returns Unit

        viewModel.onLogLessonClicked(completedLesson)
        assertEquals(completedLesson, viewModel.lessonToLog.value)

        viewModel.onSaveLessonDetails(
            lesson = completedLesson,
            duration = "2,5",
            notes = "Updated note",
            pricingMode = com.barutdev.tullab.domain.model.PricingMode.PER_HOUR,
            rateOrFee = "55.0",
            isCompleted = false
        )
        advanceUntilIdle()

        coVerify {
            lessonRepository.updateLesson(match {
                it.id == 10 &&
                it.status == LessonStatus.SCHEDULED &&
                it.durationInHours == 2.5 &&
                it.notes == "Updated note" &&
                it.rateOrFee == 55.0
            })
        }
        assertEquals(null, viewModel.lessonToLog.value)
        assertEquals(false, viewModel.isLogLessonDialogVisible.value)
    }

    @Test
    fun `onSaveLessonDetails completes scheduled lesson and persists duration`() = runTest {
        val scheduledLesson = Lesson(
            id = 11,
            studentId = 1,
            date = 1000L,
            status = LessonStatus.SCHEDULED,
            durationInHours = null,
            notes = null,
            pricingMode = com.barutdev.tullab.domain.model.PricingMode.PER_HOUR,
            rateOrFee = 50.0
        )
        coEvery { lessonRepository.updateLesson(any()) } returns Unit

        viewModel.onLogLessonClicked(scheduledLesson)
        assertEquals(scheduledLesson, viewModel.lessonToLog.value)

        viewModel.onSaveLessonDetails(
            lesson = scheduledLesson,
            duration = "1.5",
            notes = "Done",
            pricingMode = com.barutdev.tullab.domain.model.PricingMode.PER_HOUR,
            rateOrFee = "50.0",
            isCompleted = true
        )
        advanceUntilIdle()

        coVerify {
            lessonRepository.updateLesson(match {
                it.id == 11 &&
                it.status == LessonStatus.COMPLETED &&
                it.durationInHours == 1.5 &&
                it.notes == "Done"
            })
        }
        assertEquals(null, viewModel.lessonToLog.value)
        assertEquals(false, viewModel.isLogLessonDialogVisible.value)
    }
}
