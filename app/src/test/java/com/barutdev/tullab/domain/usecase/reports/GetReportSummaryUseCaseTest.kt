package com.barutdev.tullab.domain.usecase.reports

import com.barutdev.tullab.domain.model.Lesson
import com.barutdev.tullab.domain.model.LessonStatus
import com.barutdev.tullab.domain.model.Student
import com.barutdev.tullab.domain.model.reports.ReportRange
import com.barutdev.tullab.reports.FakeLessonRepository
import com.barutdev.tullab.reports.FakeStudentRepository
import java.math.BigDecimal
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetReportSummaryUseCaseTest {

    private val zoneId: ZoneId = ZoneId.of("UTC")
    private val clock: Clock = Clock.fixed(Instant.parse("2025-03-20T10:00:00Z"), zoneId)
    private val lessonRepository = FakeLessonRepository()
    private val studentRepository = FakeStudentRepository()

    private val useCase = GetReportSummaryUseCase(
        lessonRepository = lessonRepository,
        studentRepository = studentRepository,
        clock = clock,
        zoneId = zoneId
    )

    @Test
    fun `returns aggregated summary for this month`() = runTest {
        studentRepository.setStudents(
            listOf(
                Student(id = 1, fullName = "Alice", hourlyRate = 50.0, lastPaymentDate = null),
                Student(id = 2, fullName = "Bilal", hourlyRate = 60.0, lastPaymentDate = null)
            )
        )

        lessonRepository.setLessons(
            listOf(
                lesson(
                    id = 1,
                    studentId = 1,
                    status = LessonStatus.PAID,
                    durationHours = 2.0,
                    rateOrFee = 50.0,
                    lessonDate = LocalDate.of(2025, 3, 6),
                    paymentDate = LocalDate.of(2025, 3, 7)
                ),
                lesson(
                    id = 2,
                    studentId = 2,
                    status = LessonStatus.PAID,
                    durationHours = 1.5,
                    rateOrFee = 60.0,
                    lessonDate = LocalDate.of(2025, 3, 9),
                    paymentDate = LocalDate.of(2025, 3, 10)
                ),
                lesson(
                    id = 3,
                    studentId = 1,
                    status = LessonStatus.COMPLETED,
                    durationHours = 1.0,
                    rateOrFee = 50.0,
                    lessonDate = LocalDate.of(2025, 3, 12),
                    paymentDate = null
                )
            )
        )

        val result = useCase(ReportRange.ThisMonth)

        assertEquals(BigDecimal("190.00"), result.totalEarnings)
        assertEquals(Duration.ofMinutes(270), result.totalHours)
        assertEquals(2, result.activeStudents)
    }

    @Test
    fun `aggregates flat-fee lessons via calculatedValue`() = runTest {
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
                    date = LocalDate.of(2025, 3, 6).atStartOfDay(zoneId).toInstant().toEpochMilli(),
                    notes = null,
                    pricingMode = com.barutdev.tullab.domain.model.PricingMode.FLAT_FEE,
                    rateOrFee = 120.0,
                    paymentTimestamp = LocalDate.of(2025, 3, 7).atStartOfDay(zoneId).toInstant().toEpochMilli()
                ),
                lesson(
                    id = 2,
                    studentId = 1,
                    status = LessonStatus.PAID,
                    durationHours = 2.0,
                    rateOrFee = 50.0,
                    lessonDate = LocalDate.of(2025, 3, 9),
                    paymentDate = LocalDate.of(2025, 3, 10)
                )
            )
        )

        val result = useCase(ReportRange.ThisMonth)

        assertEquals(BigDecimal("220.00"), result.totalEarnings)
    }

    @Test
    fun `filters earnings by lesson date consistently with hours`() = runTest {
        studentRepository.setStudents(
            listOf(Student(id = 1, fullName = "Alice", hourlyRate = 50.0, lastPaymentDate = null))
        )
        lessonRepository.setLessons(
            listOf(
                // Lesson taught in range but paid outside range: still counts
                // because range filtering uses lesson date for both hours and earnings.
                lesson(
                    id = 1,
                    studentId = 1,
                    status = LessonStatus.PAID,
                    durationHours = 2.0,
                    rateOrFee = 50.0,
                    lessonDate = LocalDate.of(2025, 3, 6),
                    paymentDate = LocalDate.of(2025, 4, 2)
                )
            )
        )

        val result = useCase(ReportRange.ThisMonth)

        assertEquals(BigDecimal("100.00"), result.totalEarnings)
        assertEquals(Duration.ofMinutes(120), result.totalHours)
    }

    @Test
    fun `returns zero summary when no lessons fall in range`() = runTest {
        studentRepository.setStudents(
            listOf(Student(id = 1, fullName = "Alice", hourlyRate = 50.0, lastPaymentDate = null))
        )
        lessonRepository.setLessons(
            listOf(
                lesson(
                    id = 1,
                    studentId = 1,
                    status = LessonStatus.PAID,
                    durationHours = 2.0,
                    rateOrFee = 50.0,
                    lessonDate = LocalDate.of(2024, 12, 1),
                    paymentDate = LocalDate.of(2024, 12, 2)
                )
            )
        )

        val result = useCase(ReportRange.ThisMonth)

        assertEquals(BigDecimal.ZERO.setScale(2), result.totalEarnings)
        assertEquals(Duration.ZERO, result.totalHours)
        assertEquals(0, result.activeStudents)
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
        pricingMode = com.barutdev.tullab.domain.model.PricingMode.PER_HOUR,
        rateOrFee = rateOrFee,
        paymentTimestamp = paymentDate?.atStartOfDay(zoneId)?.toInstant()?.toEpochMilli()
    )
}
