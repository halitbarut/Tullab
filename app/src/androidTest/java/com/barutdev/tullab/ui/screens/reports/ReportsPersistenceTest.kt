package com.barutdev.tullab.ui.screens.reports

import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import com.barutdev.tullab.R
import com.barutdev.tullab.domain.model.Lesson
import com.barutdev.tullab.domain.model.LessonStatus
import com.barutdev.tullab.domain.model.PricingMode
import com.barutdev.tullab.domain.model.Student
import com.barutdev.tullab.domain.model.reports.ReportRange
import com.barutdev.tullab.domain.repository.LessonRepository
import com.barutdev.tullab.domain.repository.StudentRepository
import com.barutdev.tullab.domain.usecase.reports.GetMonthlyEarningsUseCase
import com.barutdev.tullab.domain.usecase.reports.GetReportSummaryUseCase
import com.barutdev.tullab.domain.usecase.reports.GetTopStudentsUseCase
import com.barutdev.tullab.ui.theme.TullabTheme
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class ReportsPersistenceTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val zoneId = ZoneId.of("UTC")
    private val clock: Clock = Clock.fixed(Instant.parse("2025-03-20T10:00:00Z"), zoneId)
    private val locale = Locale.US
    private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(locale)

    @Test
    fun rangeSelectionPersistsAfterRecreatingViewModel() {
        val lessonRepository = InMemoryLessonRepository()
        val studentRepository = InMemoryStudentRepository()

        studentRepository.setStudents(
            listOf(
                Student(id = 1, fullName = "Alice", hourlyRate = 50.0),
                Student(id = 2, fullName = "Bilal", hourlyRate = 45.0)
            )
        )

        lessonRepository.setLessons(
            listOf(
                lesson(
                    id = 1,
                    studentId = 1,
                    status = LessonStatus.PAID,
                    durationHours = 2.0,
                    lessonDate = LocalDate.of(2025, 3, 10),
                    paymentDate = LocalDate.of(2025, 3, 11)
                ),
                lesson(
                    id = 2,
                    studentId = 2,
                    status = LessonStatus.PAID,
                    durationHours = 3.0,
                    lessonDate = LocalDate.of(2025, 2, 20),
                    paymentDate = LocalDate.of(2025, 2, 20)
                )
            )
        )

        val summaryUseCase = GetReportSummaryUseCase(lessonRepository, studentRepository, clock, zoneId)
        val monthlyUseCase = GetMonthlyEarningsUseCase(lessonRepository, studentRepository, clock, zoneId)
        val topStudentsUseCase = GetTopStudentsUseCase(lessonRepository, studentRepository, clock, zoneId)
        val savedStateHandle = SavedStateHandle()

        var viewModelHolder by mutableStateOf(
            ReportsViewModel(
                getReportSummaryUseCase = summaryUseCase,
                getMonthlyEarningsUseCase = monthlyUseCase,
                getTopStudentsUseCase = topStudentsUseCase,
                savedStateHandle = savedStateHandle
            )
        )

        // Single setContent: observe the holder so ViewModel recreation does
        // not require installing content twice on the same rule.
        composeRule.setContent {
            TullabTheme {
                val currentViewModel = viewModelHolder
                val state by currentViewModel.uiState.collectAsState()
                ReportsScreenContent(
                    uiState = state,
                    locale = locale,
                    currencyFormatter = currencyFormatter,
                    onRangeSelected = currentViewModel::onRangeSelected,
                    onRetry = currentViewModel::refresh
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText(getString(R.string.reports_range_last_30_days))
            .performClick()

        composeRule.waitForIdle()

        // Simulate ViewModel recreation with the same SavedStateHandle
        viewModelHolder = ReportsViewModel(
            getReportSummaryUseCase = summaryUseCase,
            getMonthlyEarningsUseCase = monthlyUseCase,
            getTopStudentsUseCase = topStudentsUseCase,
            savedStateHandle = savedStateHandle
        )

        composeRule.waitForIdle()

        assertEquals(ReportRange.LastThirtyDays, viewModelHolder.uiState.value.selectedRange)
        composeRule.onNodeWithTag("reports-range-${R.string.reports_range_last_30_days}")
            .assertIsSelected()
    }

    private fun lesson(
        id: Int,
        studentId: Int,
        status: LessonStatus,
        durationHours: Double,
        lessonDate: LocalDate,
        paymentDate: LocalDate?,
        rateOrFee: Double = 50.0
    ): Lesson = Lesson(
        id = id,
        studentId = studentId,
        status = status,
        durationInHours = durationHours,
        date = lessonDate.atStartOfDay(zoneId).toInstant().toEpochMilli(),
        notes = null,
        pricingMode = PricingMode.PER_HOUR,
        rateOrFee = rateOrFee,
        paymentTimestamp = paymentDate?.atStartOfDay(zoneId)?.toInstant()?.toEpochMilli()
    )

    private class InMemoryLessonRepository : LessonRepository {
        private val lessons = MutableStateFlow<List<Lesson>>(emptyList())

        fun setLessons(items: List<Lesson>) {
            lessons.value = items
        }

        override fun getAllLessons(): Flow<List<Lesson>> = lessons

        override fun getLessonsForStudent(studentId: Int): Flow<List<Lesson>> = flowOf(emptyList())

        override suspend fun insertLesson(lesson: Lesson): Int = throw UnsupportedOperationException()

        override suspend fun updateLesson(lesson: Lesson) = throw UnsupportedOperationException()

        override suspend fun deleteLesson(lessonId: Int) = throw UnsupportedOperationException()

        override suspend fun markCompletedLessonsAsPaid(studentId: Int) = throw UnsupportedOperationException()

        override suspend fun getScheduledLessonCount(studentId: Int): Int = 0

        override suspend fun getScheduledLessonsForStudent(studentId: Int): List<Lesson> = emptyList()

        override suspend fun updateScheduledLessonsRate(studentId: Int, newRate: Double) = Unit

        override fun getLessonsForDate(date: LocalDate): Flow<List<Lesson>> = flowOf(emptyList())

        override fun getCompletedLessonsForDate(date: LocalDate): Flow<List<Lesson>> = flowOf(emptyList())

        override suspend fun getLessonWithStudent(lessonId: Int) = null

        override fun getActiveLessons(): Flow<List<Lesson>> = flowOf(emptyList())

        override suspend fun insertLessons(lessons: List<Lesson>): List<Int> = emptyList()

        override suspend fun deleteLessons(lessonIds: List<Int>) = Unit

        override suspend fun getLessonDatesForStudent(studentId: Int): List<Long> = emptyList()
    }

    private class InMemoryStudentRepository : StudentRepository {
        private val students = MutableStateFlow<List<Student>>(emptyList())

        fun setStudents(items: List<Student>) {
            students.value = items
        }

        override fun getAllStudents(): Flow<List<Student>> = students

        override fun getStudentById(id: Int): Flow<Student?> = flowOf(null)

        override suspend fun addStudent(student: Student) = throw UnsupportedOperationException()

        override suspend fun updateStudentHourlyRate(studentId: Int, newRate: Double) = throw UnsupportedOperationException()

        override suspend fun updateStudentProfile(update: com.barutdev.tullab.domain.model.StudentProfileUpdate) = throw UnsupportedOperationException()

        override suspend fun deleteStudent(studentId: Int) = throw UnsupportedOperationException()
    }

    private fun getString(id: Int): String =
        composeRule.activity.getString(id)
}
