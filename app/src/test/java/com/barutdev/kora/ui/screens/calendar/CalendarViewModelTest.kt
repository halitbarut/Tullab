package com.barutdev.kora.ui.screens.calendar

import androidx.lifecycle.SavedStateHandle
import com.barutdev.kora.domain.model.Homework
import com.barutdev.kora.domain.model.HomeworkStatus
import com.barutdev.kora.domain.repository.HomeworkRepository
import com.barutdev.kora.domain.repository.LessonRepository
import com.barutdev.kora.domain.repository.StudentRepository
import com.barutdev.kora.domain.repository.UserPreferencesRepository
import com.barutdev.kora.domain.usecase.notification.CancelNotificationAlarmsUseCase
import com.barutdev.kora.domain.usecase.notification.ScheduleNotificationAlarmsUseCase
import com.barutdev.kora.navigation.STUDENT_ID_ARG
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
            scheduleNotificationAlarmsUseCase = scheduleNotificationAlarmsUseCase,
            cancelNotificationAlarmsUseCase = cancelNotificationAlarmsUseCase
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
}
