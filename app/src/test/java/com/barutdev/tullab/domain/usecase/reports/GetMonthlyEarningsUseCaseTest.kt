package com.barutdev.tullab.domain.usecase.reports

import com.barutdev.tullab.domain.model.Lesson
import com.barutdev.tullab.domain.model.LessonStatus
import com.barutdev.tullab.domain.model.Student
import com.barutdev.tullab.reports.FakeLessonRepository
import com.barutdev.tullab.reports.FakeStudentRepository
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetMonthlyEarningsUseCaseTest {

    private val zoneId: ZoneId = ZoneId.of("UTC")
    private val clock: Clock = Clock.fixed(Instant.parse("2025-03-20T10:00:00Z"), zoneId)
    private val lessonRepository = FakeLessonRepository()
    private val studentRepository = FakeStudentRepository()

    private val useCase = GetMonthlyEarningsUseCase(
        lessonRepository = lessonRepository,
        studentRepository = studentRepository,
        clock = clock,
        zoneId = zoneId
    )

    @Test
    fun `returns six months of earnings in chronological order`() = runTest {
        studentRepository.setStudents(
            listOf(Student(id = 1, fullName = "Alice", hourlyRate = 50.0, lastPaymentDate = null))
        )
        lessonRepository.setLessons(
            listOf(
                paidLesson(
                    id = 1,
                    studentId = 1,
                    durationHours = 2.0,
                    rateOrFee = 50.0,
                    lessonDate = LocalDate.of(2024, 10, 12),
                    paymentDate = LocalDate.of(2024, 10, 15)
                ),
                paidLesson(
                    id = 2,
                    studentId = 1,
                    durationHours = 1.0,
                    rateOrFee = 50.0,
                    lessonDate = LocalDate.of(2024, 12, 1),
                    paymentDate = LocalDate.of(2024, 12, 2)
                ),
                paidLesson(
                    id = 3,
                    studentId = 1,
                    durationHours = 1.5,
                    rateOrFee = 50.0,
                    lessonDate = LocalDate.of(2025, 1, 8),
                    paymentDate = LocalDate.of(2025, 1, 8)
                ),
                paidLesson(
                    id = 4,
                    studentId = 1,
                    durationHours = 2.0,
                    rateOrFee = 50.0,
                    lessonDate = LocalDate.of(2025, 3, 5),
                    paymentDate = LocalDate.of(2025, 3, 5)
                ),
                Lesson(
                    id = 5,
                    studentId = 1,
                    status = LessonStatus.COMPLETED,
                    durationInHours = 1.0,
                    date = LocalDate.of(2023, 10, 25).atStartOfDay(zoneId).toInstant().toEpochMilli(),
                    notes = null,
                    pricingMode = com.barutdev.tullab.domain.model.PricingMode.PER_HOUR,
                    rateOrFee = 50.0,
                    paymentTimestamp = null
                )
            )
        )

        val points = useCase()

        val expectedMonths = listOf(
            YearMonth.of(2024, 10),
            YearMonth.of(2024, 11),
            YearMonth.of(2024, 12),
            YearMonth.of(2025, 1),
            YearMonth.of(2025, 2),
            YearMonth.of(2025, 3)
        )

        assertEquals(expectedMonths, points.map { it.month })
        assertEquals(BigDecimal("100.00"), points[0].earnings)
        assertEquals(BigDecimal.ZERO.setScale(2), points[1].earnings)
        assertEquals(BigDecimal("50.00"), points[2].earnings)
        assertEquals(BigDecimal("75.00"), points[3].earnings)
        assertEquals(BigDecimal.ZERO.setScale(2), points[4].earnings)
        assertEquals(BigDecimal("100.00"), points[5].earnings)
    }

    @Test
    fun `includes flat-fee lessons grouped by lesson month`() = runTest {
        studentRepository.setStudents(
            listOf(Student(id = 1, fullName = "Alice", hourlyRate = 50.0, lastPaymentDate = null))
        )
        lessonRepository.setLessons(
            listOf(
                Lesson(
                    id = 1,
                    studentId = 1,
                    status = LessonStatus.PAID,
                    durationInHours = null,
                    date = LocalDate.of(2025, 3, 5).atStartOfDay(zoneId).toInstant().toEpochMilli(),
                    notes = null,
                    pricingMode = com.barutdev.tullab.domain.model.PricingMode.FLAT_FEE,
                    rateOrFee = 200.0,
                    paymentTimestamp = LocalDate.of(2025, 3, 6).atStartOfDay(zoneId).toInstant().toEpochMilli()
                )
            )
        )

        val points = useCase()

        assertEquals(BigDecimal("200.00"), points.last().earnings)
    }

    @Test
    fun `returns zeros when no paid lessons exist`() = runTest {
        studentRepository.setStudents(emptyList())
        lessonRepository.setLessons(emptyList())

        val points = useCase()

        assertEquals(6, points.size)
        assertTrue(points.all { it.earnings == BigDecimal.ZERO.setScale(2) })
    }

    private fun paidLesson(
        id: Int,
        studentId: Int,
        durationHours: Double,
        lessonDate: LocalDate,
        paymentDate: LocalDate,
        rateOrFee: Double = 50.0
    ): Lesson = Lesson(
        id = id,
        studentId = studentId,
        status = LessonStatus.PAID,
        durationInHours = durationHours,
        date = lessonDate.atStartOfDay(zoneId).toInstant().toEpochMilli(),
        notes = null,
        pricingMode = com.barutdev.tullab.domain.model.PricingMode.PER_HOUR,
        rateOrFee = rateOrFee,
        paymentTimestamp = paymentDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
    )
}
