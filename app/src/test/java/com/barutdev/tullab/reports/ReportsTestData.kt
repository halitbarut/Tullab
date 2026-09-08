package com.barutdev.tullab.reports

import com.barutdev.tullab.domain.model.Lesson
import com.barutdev.tullab.domain.model.Student
import com.barutdev.tullab.domain.model.StudentProfileUpdate
import com.barutdev.tullab.domain.model.reports.MonthlyEarningsPoint
import com.barutdev.tullab.domain.model.reports.ReportRange
import com.barutdev.tullab.domain.model.reports.ReportSummary
import com.barutdev.tullab.domain.model.reports.TopStudentEntry
import com.barutdev.tullab.domain.repository.LessonRepository
import com.barutdev.tullab.domain.repository.StudentRepository
import java.math.BigDecimal
import java.time.Duration
import java.time.YearMonth
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/**
 * Shared fixtures for Reports domain and UI tests.
 */
object ReportsTestData {
    val defaultRange: ReportRange = ReportRange.ThisMonth

    val summary: ReportSummary = ReportSummary(
        totalEarnings = BigDecimal("420.00"),
        totalHours = Duration.ofHours(18),
        activeStudents = 5
    )

    val monthlyEarnings: List<MonthlyEarningsPoint> = (0..5).map { offset ->
        val month = YearMonth.now().minusMonths((5 - offset).toLong())
        MonthlyEarningsPoint(
            month = month,
            earnings = BigDecimal.valueOf(150 + offset * 50L)
        )
    }

    val topStudents: List<TopStudentEntry> = listOf(
        TopStudentEntry(
            studentId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
            studentName = "Alice Johnson",
            hoursTaught = Duration.ofHours(6)
        ),
        TopStudentEntry(
            studentId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
            studentName = "Bilal Demir",
            hoursTaught = Duration.ofHours(5)
        ),
        TopStudentEntry(
            studentId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
            studentName = "Chloe Martin",
            hoursTaught = Duration.ofHours(4)
        )
    )

    fun emptySummary(): ReportSummary = ReportSummary(
        totalEarnings = BigDecimal.ZERO,
        totalHours = Duration.ZERO,
        activeStudents = 0
    )

    fun singleStudent(studentName: String = "Solo Student", hours: Long = 3): List<TopStudentEntry> =
        listOf(
            TopStudentEntry(
                studentId = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"),
                studentName = studentName,
                hoursTaught = Duration.ofHours(hours)
            )
        )
}

class FakeLessonRepository(
    var nextDelayMillis: Long = 0L
) : LessonRepository {
    private val lessonsFlow = MutableStateFlow<List<Lesson>>(emptyList())

    fun setLessons(lessons: List<Lesson>) {
        lessonsFlow.value = lessons
    }

    override fun getAllLessons(): Flow<List<Lesson>> = flow {
        val delayMillis = nextDelayMillis
        if (delayMillis > 0) {
            delay(delayMillis)
        }
        emitAll(lessonsFlow)
    }

    override fun getLessonsForStudent(studentId: Int): Flow<List<Lesson>> =
        throw UnsupportedOperationException()

    override suspend fun insertLesson(lesson: Lesson): Int =
        throw UnsupportedOperationException()

    override suspend fun insertLessons(lessons: List<Lesson>): List<Int> = emptyList()
    override suspend fun deleteLessons(lessonIds: List<Int>) = Unit
    override suspend fun getLessonDatesForStudent(studentId: Int): List<Long> = emptyList()
    override suspend fun updateLesson(lesson: Lesson) =
        throw UnsupportedOperationException()

    override suspend fun deleteLesson(lessonId: Int) =
        throw UnsupportedOperationException()
    override suspend fun getScheduledLessonCount(studentId: Int): Int = 0
    override suspend fun getScheduledLessonsForStudent(studentId: Int): List<Lesson> = emptyList()
    override suspend fun updateScheduledLessonsRate(studentId: Int, newRate: Double) = Unit

    override suspend fun markCompletedLessonsAsPaid(studentId: Int) =
        throw UnsupportedOperationException()

    override fun getLessonsForDate(date: java.time.LocalDate): Flow<List<Lesson>> =
        throw UnsupportedOperationException()

    override fun getCompletedLessonsForDate(date: java.time.LocalDate): Flow<List<Lesson>> =
        throw UnsupportedOperationException()

    override suspend fun getLessonWithStudent(lessonId: Int): com.barutdev.tullab.domain.model.LessonWithStudent? =
        throw UnsupportedOperationException()

    override fun getActiveLessons(): Flow<List<Lesson>> =
        throw UnsupportedOperationException()
}

class FakeStudentRepository : StudentRepository {
    private val studentsFlow = MutableStateFlow<List<Student>>(emptyList())

    fun setStudents(students: List<Student>) {
        studentsFlow.value = students
    }

    override fun getAllStudents(): Flow<List<Student>> = studentsFlow

    override fun getStudentById(id: Int): Flow<Student?> =
        throw UnsupportedOperationException()

    override suspend fun addStudent(student: Student) =
        throw UnsupportedOperationException()

    override suspend fun updateStudentHourlyRate(studentId: Int, newRate: Double) =
        throw UnsupportedOperationException()

    override suspend fun updateStudentProfile(update: StudentProfileUpdate) =
        throw UnsupportedOperationException()

    override suspend fun deleteStudent(studentId: Int) =
        throw UnsupportedOperationException()
}
