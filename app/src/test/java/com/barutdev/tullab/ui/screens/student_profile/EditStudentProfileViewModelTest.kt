package com.barutdev.tullab.ui.screens.student_profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.barutdev.tullab.MainDispatcherRule
import com.barutdev.tullab.domain.model.Lesson
import com.barutdev.tullab.domain.model.LessonStatus
import com.barutdev.tullab.domain.model.PricingMode
import com.barutdev.tullab.domain.model.Student
import com.barutdev.tullab.domain.model.StudentProfileUpdate
import com.barutdev.tullab.domain.model.UserPreferences
import com.barutdev.tullab.domain.repository.LessonRepository
import com.barutdev.tullab.domain.repository.StudentRepository
import com.barutdev.tullab.domain.repository.UserPreferencesRepository
import com.barutdev.tullab.navigation.STUDENT_ID_ARG
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class EditStudentProfileViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun onSave_whenRateChangedAndScheduledLessonsExist_showsPrompt() = runTest {
        val (viewModel, _) = createViewModel(
            student = Student(id = 1, fullName = "Test", hourlyRate = 50.0, customHourlyRate = 50.0),
            scheduledLessonsCount = 2
        )
        
        viewModel.uiState.first { !it.isLoading }
        
        viewModel.onHourlyRateChanged("75")
        viewModel.onSave()
        
        val state = viewModel.uiState.first { it.isScheduledLessonsPromptVisible }
        
        assertTrue(state.isScheduledLessonsPromptVisible)
        assertEquals(2, state.scheduledLessonsCount)
        assertEquals(75.0, state.pendingProfileUpdate?.customHourlyRate)
        
        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
    }

    @Test
    fun onConfirmScheduledLessonsRateUpdate_updatesStudentAndLessons() = runTest {
        val (viewModel, lessonRepo) = createViewModel(
            student = Student(id = 1, fullName = "Test", hourlyRate = 50.0, customHourlyRate = 50.0),
            scheduledLessonsCount = 2
        )
        
        viewModel.uiState.first { !it.isLoading }
        
        viewModel.onHourlyRateChanged("75")
        viewModel.onSave()
        
        viewModel.uiState.first { it.isScheduledLessonsPromptVisible }
        
        viewModel.onConfirmScheduledLessonsRateUpdate(updateScheduled = true)
        
        val state = viewModel.uiState.first { !it.isScheduledLessonsPromptVisible }
        
        assertFalse(state.isScheduledLessonsPromptVisible)
        assertNull(state.pendingProfileUpdate)
        
        advanceUntilIdle()
        
        coVerify { lessonRepo.updateScheduledLessonsRate(1, 75.0) }
        
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun onConfirmScheduledLessonsRateUpdate_doesNotUpdateLessonsIfFalse() = runTest {
        val (viewModel, lessonRepo) = createViewModel(
            student = Student(id = 1, fullName = "Test", hourlyRate = 50.0, customHourlyRate = 50.0),
            scheduledLessonsCount = 2
        )
        
        viewModel.uiState.first { !it.isLoading }
        
        viewModel.onHourlyRateChanged("75")
        viewModel.onSave()
        
        viewModel.uiState.first { it.isScheduledLessonsPromptVisible }
        
        viewModel.onConfirmScheduledLessonsRateUpdate(updateScheduled = false)
        
        val state = viewModel.uiState.first { !it.isScheduledLessonsPromptVisible }
        
        assertFalse(state.isScheduledLessonsPromptVisible)
        
        coVerify(exactly = 0) { lessonRepo.updateScheduledLessonsRate(any(), any()) }
        
        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
    }

    // --- T007: Date-based scope classification tests ---

    @Test
    fun onSave_whenAllScheduledLessonsAreInFuture_setsScope_FUTURE_ONLY() = runTest {
        val today = LocalDate.now()
        val futureDateMillis = today.plusDays(3).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val lessons = listOf(
            makeScheduledLesson(id = 10, dateMillis = futureDateMillis),
            makeScheduledLesson(id = 11, dateMillis = today.plusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
        )
        val (viewModel, _) = createViewModel(
            student = Student(id = 1, fullName = "Test", hourlyRate = 50.0, customHourlyRate = 50.0),
            scheduledLessons = lessons
        )

        viewModel.uiState.first { !it.isLoading }
        viewModel.onHourlyRateChanged("75")
        viewModel.onSave()

        val state = viewModel.uiState.first { it.isScheduledLessonsPromptVisible }

        assertEquals(ScheduledLessonsScope.FUTURE_ONLY, state.scheduledLessonsScope)

        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
    }

    @Test
    fun onSave_whenAllScheduledLessonsAreInPast_setsScope_PAST_ONLY() = runTest {
        val today = LocalDate.now()
        val pastDateMillis = today.minusDays(3).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val lessons = listOf(
            makeScheduledLesson(id = 12, dateMillis = pastDateMillis),
            makeScheduledLesson(id = 13, dateMillis = today.minusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
        )
        val (viewModel, _) = createViewModel(
            student = Student(id = 1, fullName = "Test", hourlyRate = 50.0, customHourlyRate = 50.0),
            scheduledLessons = lessons
        )

        viewModel.uiState.first { !it.isLoading }
        viewModel.onHourlyRateChanged("75")
        viewModel.onSave()

        val state = viewModel.uiState.first { it.isScheduledLessonsPromptVisible }

        assertEquals(ScheduledLessonsScope.PAST_ONLY, state.scheduledLessonsScope)

        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
    }

    @Test
    fun onSave_whenScheduledLessonsSpanPastAndFuture_setsScope_MIXED() = runTest {
        val today = LocalDate.now()
        val futureDateMillis = today.plusDays(3).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val pastDateMillis = today.minusDays(3).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val lessons = listOf(
            makeScheduledLesson(id = 14, dateMillis = futureDateMillis),
            makeScheduledLesson(id = 15, dateMillis = pastDateMillis)
        )
        val (viewModel, _) = createViewModel(
            student = Student(id = 1, fullName = "Test", hourlyRate = 50.0, customHourlyRate = 50.0),
            scheduledLessons = lessons
        )

        viewModel.uiState.first { !it.isLoading }
        viewModel.onHourlyRateChanged("75")
        viewModel.onSave()

        val state = viewModel.uiState.first { it.isScheduledLessonsPromptVisible }

        assertEquals(ScheduledLessonsScope.MIXED, state.scheduledLessonsScope)

        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
    }

    private fun makeScheduledLesson(id: Int, dateMillis: Long) = Lesson(
        id = id,
        studentId = 1,
        date = dateMillis,
        status = LessonStatus.SCHEDULED,
        durationInHours = null,
        notes = null,
        pricingMode = PricingMode.PER_HOUR,
        rateOrFee = 50.0
    )

    private fun createViewModel(
        student: Student,
        scheduledLessonsCount: Int = 0,
        scheduledLessons: List<Lesson> = emptyList()
    ): Pair<EditStudentProfileViewModel, LessonRepository> {
        val savedStateHandle = SavedStateHandle(mapOf(STUDENT_ID_ARG to student.id))

        val studentRepository = object : StudentRepository {
            override fun getAllStudents(): Flow<List<Student>> = MutableStateFlow(listOf(student))
            override fun getStudentById(id: Int): Flow<Student?> = MutableStateFlow(student)
            override suspend fun addStudent(student: Student) {}
            override suspend fun updateStudentHourlyRate(studentId: Int, newRate: Double) {}
            override suspend fun updateStudentProfile(update: StudentProfileUpdate) {}
            override suspend fun deleteStudent(studentId: Int) {}
        }

        val lessonRepository = mockk<LessonRepository>(relaxed = true)
        // Support both the old count-based approach and new list approach
        val effectiveList = if (scheduledLessons.isEmpty() && scheduledLessonsCount > 0) {
            List(scheduledLessonsCount) { makeScheduledLesson(it, System.currentTimeMillis() + 86400000L) } // Future lessons
        } else {
            scheduledLessons
        }
        val effectiveCount = effectiveList.size
        coEvery { lessonRepository.getScheduledLessonCount(student.id) } returns effectiveCount
        coEvery { lessonRepository.getScheduledLessonsForStudent(student.id) } returns effectiveList

        val userPreferencesRepository = object : UserPreferencesRepository {
            override val userPreferences = MutableStateFlow(UserPreferences(isDarkMode = false, languageCode = "en", currencyCode = "USD", defaultHourlyRate = 50.0, lessonRemindersEnabled = false, logReminderEnabled = false, lessonReminderHour = 9, lessonReminderMinute = 0, logReminderHour = 18, logReminderMinute = 0))
            override suspend fun isFirstRunCompleted(): Boolean = false
            override suspend fun setFirstRunCompleted() {}
            override suspend fun getSavedLanguageOrNull(): String? = null
            override suspend fun getSavedCurrencyOrNull(): String? = null
            override suspend fun isOnboardingCompleted(): Boolean = false
            override suspend fun setOnboardingCompleted(completed: Boolean) {}
            override suspend fun updateTheme(isDarkMode: Boolean) {}
            override suspend fun updateLanguage(languageCode: String) {}
            override suspend fun updateCurrency(currencyCode: String) {}
            override suspend fun updateDefaultHourlyRate(hourlyRate: Double) {}
            override suspend fun updateLessonRemindersEnabled(isEnabled: Boolean) {}
            override suspend fun updateLogReminderEnabled(isEnabled: Boolean) {}
            override suspend fun updateLessonReminderTime(hour: Int, minute: Int) {}
            override suspend fun updateLogReminderTime(hour: Int, minute: Int) {}
            override suspend fun updateHapticFeedbackEnabled(isEnabled: Boolean) {}
            override suspend fun resetPreferences() {}
        }

        val viewModel = EditStudentProfileViewModel(
            savedStateHandle = savedStateHandle,
            studentRepository = studentRepository,
            lessonRepository = lessonRepository,
            userPreferencesRepository = userPreferencesRepository
        )

        return Pair(viewModel, lessonRepository)
    }
}
