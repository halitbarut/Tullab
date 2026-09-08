package com.barutdev.tullab.domain.usecase.reports

import com.barutdev.tullab.domain.model.Lesson
import com.barutdev.tullab.domain.model.LessonStatus
import com.barutdev.tullab.domain.model.Student
import com.barutdev.tullab.domain.model.reports.ReportRange
import com.barutdev.tullab.domain.model.reports.TopStudentEntry
import com.barutdev.tullab.domain.repository.LessonRepository
import com.barutdev.tullab.domain.repository.StudentRepository
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlin.math.max
import kotlin.math.roundToLong

class GetTopStudentsUseCase @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val studentRepository: StudentRepository,
    private val clock: Clock,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) {

    suspend operator fun invoke(range: ReportRange?): List<TopStudentEntry> {
        val targetRange = range ?: ReportRange.default
        val today = LocalDate.now(clock)
        val bounds = targetRange.bounds(today = today, zoneId = zoneId)

        val lessons = lessonRepository.getAllLessons().first()
        if (lessons.isEmpty()) return emptyList()

        val students = studentRepository.getAllStudents().first().associateBy { it.id }

        val totals = lessons
            .asSequence()
            .filter { it.isEligible(bounds) }
            .mapNotNull { lesson ->
                val minutes = lesson.durationMinutes()
                if (minutes <= 0L) return@mapNotNull null
                val student = students[lesson.studentId] ?: return@mapNotNull null
                Accumulator(student = student, minutes = minutes)
            }
            .groupBy { it.student.id }
            .mapValues { (_, entries) -> entries.sumOf { it.minutes } }

        if (totals.isEmpty()) return emptyList()

        return totals
            .mapNotNull { (studentId, totalMinutes) ->
                val student = students[studentId] ?: return@mapNotNull null
                TopStudentEntry(
                    studentId = student.toStableUuid(),
                    studentName = student.fullName,
                    hoursTaught = Duration.ofMinutes(totalMinutes)
                )
            }
            .sortedWith(
                compareByDescending<TopStudentEntry> { it.hoursTaught }
                    .thenBy { it.studentName }
            )
            .take(3)
    }

    private fun Lesson.isEligible(bounds: ClosedRange<LocalDate>): Boolean {
        val relevantStatus = status == LessonStatus.COMPLETED || status == LessonStatus.PAID
        if (!relevantStatus) return false
        val lessonDate = Instant.ofEpochMilli(date).atZone(zoneId).toLocalDate()
        return lessonDate in bounds
    }

    private fun Lesson.durationMinutes(): Long {
        val hours = durationInHours ?: return 0L
        if (hours <= 0.0) return 0L
        return max(0L, (hours * 60.0).roundToLong())
    }

    private fun Student.toStableUuid(): UUID =
        UUID.nameUUIDFromBytes("student-$id".toByteArray())

    private data class Accumulator(
        val student: Student,
        val minutes: Long
    )
}
