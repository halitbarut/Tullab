package com.barutdev.tullab.ui.screens.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.barutdev.tullab.MainDispatcherRule
import com.barutdev.tullab.domain.model.Homework
import com.barutdev.tullab.domain.model.Lesson
import com.barutdev.tullab.domain.model.LessonStatus
import com.barutdev.tullab.domain.model.PaymentRecord
import com.barutdev.tullab.domain.model.PricingMode
import com.barutdev.tullab.domain.model.Student
import com.barutdev.tullab.domain.model.UserPreferences
import com.barutdev.tullab.domain.repository.HomeworkRepository
import com.barutdev.tullab.domain.repository.LessonRepository
import com.barutdev.tullab.domain.repository.PaymentRepository
import com.barutdev.tullab.domain.repository.StudentRepository
import com.barutdev.tullab.domain.repository.UserPreferencesRepository
import com.barutdev.tullab.domain.usecase.notification.CancelNotificationAlarmsUseCase
import com.barutdev.tullab.navigation.STUDENT_ID_ARG
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun totalAmountDueIsComputedUsingLessonCalculatedValue() = runTest {
        val student = Student(id = 1, fullName = "Test Student", hourlyRate = 100.0)
        
        val lessons = listOf(
            Lesson(
                id = 1,
                studentId = 1,
                date = 0L,
                status = LessonStatus.COMPLETED,
                durationInHours = 2.0,
                notes = null,
                pricingMode = PricingMode.PER_HOUR,
                rateOrFee = 80.0
            )
        )
        
        val viewModel = createViewModel(student, lessons)
        
        val state = viewModel.uiState.first { it.studentId == 1 && it.completedLessonsAwaitingPayment.isNotEmpty() }
        println(state.completedLessonsAwaitingPayment)
        
        // Debt should be 2.0 * 80.0 = 160.0 (not using profile's 100.0 rate)
        assertEquals(160.0, state.totalAmountDue, 0.0)
        
        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
    }

    // --- T012: rateBreakdownTiers tests ---

    @Test
    fun rateBreakdownTiers_singlePerHourLesson_producesOneTier() = runTest {
        val student = Student(id = 1, fullName = "Test Student", hourlyRate = 80.0)
        val lessons = listOf(
            Lesson(
                id = 2, studentId = 1, date = 0L,
                status = LessonStatus.COMPLETED,
                durationInHours = 1.5, notes = null,
                pricingMode = PricingMode.PER_HOUR,
                rateOrFee = 80.0
            )
        )

        val viewModel = createViewModel(student, lessons)
        val state = viewModel.uiState.first { it.studentId == 1 && it.completedLessonsAwaitingPayment.isNotEmpty() }

        assertEquals(1, state.rateBreakdownTiers.size)
        val tier = state.rateBreakdownTiers.first()
        assertEquals(PricingMode.PER_HOUR, tier.pricingMode)
        assertEquals(1.5, tier.totalHours, 0.001)
        assertEquals(80.0, tier.rateOrFee, 0.001)
        assertEquals(120.0, tier.subtotal, 0.001)

        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
    }

    @Test
    fun rateBreakdownTiers_flatFeeLessons_producesOneFlatFeeTier() = runTest {
        val student = Student(id = 1, fullName = "Test Student", hourlyRate = 80.0)
        val lessons = listOf(
            Lesson(
                id = 3, studentId = 1, date = 0L,
                status = LessonStatus.COMPLETED,
                durationInHours = null, notes = null,
                pricingMode = PricingMode.FLAT_FEE,
                rateOrFee = 50.0
            ),
            Lesson(
                id = 4, studentId = 1, date = 0L,
                status = LessonStatus.COMPLETED,
                durationInHours = null, notes = null,
                pricingMode = PricingMode.FLAT_FEE,
                rateOrFee = 50.0
            )
        )

        val viewModel = createViewModel(student, lessons)
        val state = viewModel.uiState.first { it.studentId == 1 && it.completedLessonsAwaitingPayment.isNotEmpty() }

        assertEquals(1, state.rateBreakdownTiers.size)
        val tier = state.rateBreakdownTiers.first()
        assertEquals(PricingMode.FLAT_FEE, tier.pricingMode)
        assertEquals(2, tier.lessonCount)
        assertEquals(50.0, tier.rateOrFee, 0.001)
        assertEquals(100.0, tier.subtotal, 0.001)

        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
    }

    @Test
    fun rateBreakdownTiers_mixedPricingModes_producesTwoTiers() = runTest {
        val student = Student(id = 1, fullName = "Test Student", hourlyRate = 80.0)
        val lessons = listOf(
            Lesson(
                id = 5, studentId = 1, date = 0L,
                status = LessonStatus.COMPLETED,
                durationInHours = 2.0, notes = null,
                pricingMode = PricingMode.PER_HOUR,
                rateOrFee = 80.0
            ),
            Lesson(
                id = 6, studentId = 1, date = 0L,
                status = LessonStatus.COMPLETED,
                durationInHours = null, notes = null,
                pricingMode = PricingMode.FLAT_FEE,
                rateOrFee = 60.0
            )
        )

        val viewModel = createViewModel(student, lessons)
        val state = viewModel.uiState.first { it.studentId == 1 && it.completedLessonsAwaitingPayment.isNotEmpty() }

        assertEquals(2, state.rateBreakdownTiers.size)
        val perHourTier = state.rateBreakdownTiers.first { it.pricingMode == PricingMode.PER_HOUR }
        val flatFeeTier = state.rateBreakdownTiers.first { it.pricingMode == PricingMode.FLAT_FEE }
        assertEquals(160.0, perHourTier.subtotal, 0.001)
        assertEquals(60.0, flatFeeTier.subtotal, 0.001)

        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
    }

    private fun createViewModel(
        student: Student,
        lessons: List<Lesson>
    ): DashboardViewModel {
        val savedStateHandle = SavedStateHandle(mapOf(STUDENT_ID_ARG to student.id))
        
        val studentRepository = object : StudentRepository {
            override fun getAllStudents(): Flow<List<Student>> = MutableStateFlow(listOf(student))
            override fun getStudentById(id: Int): Flow<Student?> = MutableStateFlow(student)
            override suspend fun addStudent(student: Student) {}
            override suspend fun updateStudentHourlyRate(studentId: Int, newRate: Double) {}
            override suspend fun updateStudentProfile(update: com.barutdev.tullab.domain.model.StudentProfileUpdate) {}
            override suspend fun deleteStudent(studentId: Int) {}
        }
        
        val lessonRepository = object : LessonRepository {
            override fun getAllLessons(): Flow<List<Lesson>> = MutableStateFlow(lessons)
            override fun getLessonsForStudent(studentId: Int): Flow<List<Lesson>> = MutableStateFlow(lessons)
            override suspend fun insertLesson(lesson: Lesson): Int = 0
            override suspend fun insertLessons(lessons: List<Lesson>): List<Int> = emptyList()
            override suspend fun updateLesson(lesson: Lesson) {}
            override suspend fun deleteLesson(lessonId: Int) {}
            override suspend fun deleteLessons(lessonIds: List<Int>) {}
            override suspend fun getLessonDatesForStudent(studentId: Int): List<Long> = emptyList()
            override suspend fun markCompletedLessonsAsPaid(studentId: Int) {}
            override suspend fun getScheduledLessonCount(studentId: Int): Int = 0
            override suspend fun getScheduledLessonsForStudent(studentId: Int): List<Lesson> = emptyList()
            override suspend fun updateScheduledLessonsRate(studentId: Int, newRate: Double) {}
            override fun getLessonsForDate(date: java.time.LocalDate): Flow<List<Lesson>> = MutableStateFlow(emptyList())
            override fun getCompletedLessonsForDate(date: java.time.LocalDate): Flow<List<Lesson>> = MutableStateFlow(emptyList())
            override suspend fun getLessonWithStudent(lessonId: Int): com.barutdev.tullab.domain.model.LessonWithStudent? = null
            override fun getActiveLessons(): Flow<List<Lesson>> = MutableStateFlow(emptyList())
        }

        val homeworkRepository = object : HomeworkRepository {
            override fun getHomeworkForStudent(studentId: Int): Flow<List<Homework>> = MutableStateFlow(emptyList())
            override fun getHomeworkById(homeworkId: Int): Flow<Homework?> = MutableStateFlow(null)
            override suspend fun insertHomework(homework: Homework) {}
            override suspend fun updateHomework(homework: Homework) {}
        }
        
        val paymentRepository = object : PaymentRepository {
            override fun observePaymentHistory(studentId: Int): Flow<List<PaymentRecord>> = MutableStateFlow(emptyList())
            override suspend fun markStudentAsPaid(studentId: Int) {}
            override suspend fun markLessonAsPaid(lessonId: Int, durationInHours: Double?, customFee: Double?) {}
            override suspend fun revertLessonPayment(lessonId: Int) {}
        }

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
            override suspend fun resetPreferences() {}
        }
        
        return DashboardViewModel(
            savedStateHandle = savedStateHandle,
            studentRepository = studentRepository,
            lessonRepository = lessonRepository,
            homeworkRepository = homeworkRepository,
            cancelNotificationAlarmsUseCase = mockk(relaxed = true),
            scheduleNotificationAlarmsUseCase = mockk(relaxed = true),
            paymentRepository = paymentRepository,
            userPreferencesRepository = userPreferencesRepository
        )
    }
}
