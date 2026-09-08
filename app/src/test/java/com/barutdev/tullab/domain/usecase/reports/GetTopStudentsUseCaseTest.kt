package com.barutdev.tullab.domain.usecase.reports

import com.barutdev.tullab.domain.model.Lesson
import com.barutdev.tullab.domain.model.LessonStatus
import com.barutdev.tullab.domain.model.Student
import com.barutdev.tullab.domain.model.reports.ReportRange
import com.barutdev.tullab.domain.model.reports.TopStudentEntry
import com.barutdev.tullab.reports.FakeLessonRepository
import com.barutdev.tullab.reports.FakeStudentRepository
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetTopStudentsUseCaseTest {

    private val zoneId: ZoneId = ZoneId.of("UTC")
    private val clock: Clock = Clock.fixed(Instant.parse("2025-03-20T10:00:00Z"), zoneId)
    private val lessonRepository = FakeLessonRepository()
    private val studentRepository = FakeStudentRepository()

    private val useCase = GetTopStudentsUseCase(
        lessonRepository = lessonRepository,
        studentRepository = studentRepository,
        clock = clock,
        zoneId = zoneId
    )

    @Test
    fun `returns top three students ordered by hours within selected range`() = runTest {
        studentRepository.setStudents(
            listOf(
                Student(id = 1, fullName = "Alice", hourlyRate = 50.0),
                Student(id = 2, fullName = "Bilal", hourlyRate = 40.0),
                Student(id = 3, fullName = "Chloe", hourlyRate = 45.0),
                Student(id = 4, fullName = "Diego", hourlyRate = 42.0)
            )
        )

        lessonRepository.setLessons(
            listOf(
                lesson(
                    id = 1,
                    studentId = 1,
                    status = LessonStatus.COMPLETED,
                    durationHours = 1.5,
                    lessonDate = LocalDate.of(2025, 3, 1),
                    paymentDate = null
                ),
                lesson(
                    id = 2,
                    studentId = 1,
                    status = LessonStatus.PAID,
                    durationHours = 2.0,
                    lessonDate = LocalDate.of(2025, 3, 8),
                    paymentDate = LocalDate.of(2025, 3, 9)
                ),
                lesson(
                    id = 3,
                    studentId = 2,
                    status = LessonStatus.PAID,
                    durationHours = 2.5,
                    lessonDate = LocalDate.of(2025, 3, 5),
                    paymentDate = LocalDate.of(2025, 3, 6)
                ),
                lesson(
                    id = 4,
                    studentId = 4,
                    status = LessonStatus.COMPLETED,
                    durationHours = 2.5,
                    lessonDate = LocalDate.of(2025, 3, 16),
                    paymentDate = null
                ),
                lesson(
                    id = 5,
                    studentId = 3,
                    status = LessonStatus.PAID,
                    durationHours = 2.0,
                    lessonDate = LocalDate.of(2025, 3, 11),
                    paymentDate = LocalDate.of(2025, 3, 11)
                )
            )
        )

        val result = useCase(ReportRange.ThisMonth)

        assertEquals(3, result.size)
        assertOrderedEntry(result[0], "Alice", hours = Duration.ofMinutes(210))
        assertOrderedEntry(result[1], "Bilal", hours = Duration.ofMinutes(150))
        assertOrderedEntry(result[2], "Diego", hours = Duration.ofMinutes(150))
    }

    @Test
    fun `ignores lessons outside range and with non-positive duration`() = runTest {
        studentRepository.setStudents(
            listOf(
                Student(id = 1, fullName = "Alice", hourlyRate = 50.0),
                Student(id = 2, fullName = "Bilal", hourlyRate = 40.0)
            )
        )

        lessonRepository.setLessons(
            listOf(
                lesson(
                    id = 1,
                    studentId = 1,
                    status = LessonStatus.PAID,
                    durationHours = 0.0,
                    lessonDate = LocalDate.of(2025, 3, 10),
                    paymentDate = LocalDate.of(2025, 3, 11)
                ),
                lesson(
                    id = 2,
                    studentId = 1,
                    status = LessonStatus.PAID,
                    durationHours = 1.5,
                    lessonDate = LocalDate.of(2025, 1, 10),
                    paymentDate = LocalDate.of(2025, 1, 10)
                ),
                lesson(
                    id = 3,
                    studentId = 2,
                    status = LessonStatus.COMPLETED,
                    durationHours = 2.0,
                    lessonDate = LocalDate.of(2025, 3, 15),
                    paymentDate = null
                )
            )
        )

        val result = useCase(ReportRange.LastThirtyDays)

        assertEquals(1, result.size)
        assertOrderedEntry(result[0], "Bilal", hours = Duration.ofMinutes(120))
    }

    @Test
    fun `returns available students when fewer than three results`() = runTest {
        studentRepository.setStudents(
            listOf(Student(id = 1, fullName = "Alice", hourlyRate = 50.0))
        )
        lessonRepository.setLessons(
            listOf(
                lesson(
                    id = 1,
                    studentId = 1,
                    status = LessonStatus.COMPLETED,
                    durationHours = 1.0,
                    lessonDate = LocalDate.of(2025, 3, 18),
                    paymentDate = null
                )
            )
        )

        val result = useCase(ReportRange.ThisMonth)

        assertEquals(1, result.size)
        val entry = result.first()
        assertEquals("Alice", entry.studentName)
        assertEquals(Duration.ofMinutes(60), entry.hoursTaught)
    }

    private fun lesson(
        id: Int,
        studentId: Int,
        status: LessonStatus,
        durationHours: Double?,
        lessonDate: LocalDate,
        paymentDate: LocalDate?
    ): Lesson = Lesson(
        id = id,
        studentId = studentId,
        status = status,
        durationInHours = durationHours,
        date = lessonDate.atStartOfDay(zoneId).toInstant().toEpochMilli(),
        notes = null,
        pricingMode = com.barutdev.tullab.domain.model.PricingMode.PER_HOUR,
        rateOrFee = 0.0,
        paymentTimestamp = paymentDate?.atStartOfDay(zoneId)?.toInstant()?.toEpochMilli()
    )

    private fun assertOrderedEntry(entry: TopStudentEntry, name: String, hours: Duration) {
        assertEquals(name, entry.studentName)
        assertEquals(hours, entry.hoursTaught)
    }
}
