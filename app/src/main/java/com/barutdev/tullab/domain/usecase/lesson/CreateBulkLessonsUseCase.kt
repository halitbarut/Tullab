package com.barutdev.tullab.domain.usecase.lesson

import com.barutdev.tullab.domain.model.BulkLessonCandidate
import com.barutdev.tullab.domain.model.BulkScheduleDraft
import com.barutdev.tullab.domain.model.BulkScheduleResult
import com.barutdev.tullab.domain.model.Lesson
import com.barutdev.tullab.domain.model.LessonStatus
import com.barutdev.tullab.domain.model.PricingMode
import com.barutdev.tullab.domain.repository.LessonRepository
import com.barutdev.tullab.domain.repository.StudentRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class CreateBulkLessonsUseCase @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val studentRepository: StudentRepository
) {
    suspend operator fun invoke(
        draft: BulkScheduleDraft,
        candidates: List<BulkLessonCandidate>
    ): Result<BulkScheduleResult> {
        return try {
            val student = studentRepository.getStudentById(draft.studentId).firstOrNull()
                ?: return Result.failure(IllegalArgumentException("Student not found"))
            
            val validCandidates = candidates.filter { !it.isConflict }
            val skippedCount = candidates.size - validCandidates.size
            val hasPastLessons = validCandidates.any { it.isPast }

            val lessonsToCreate = validCandidates.map { candidate ->
                Lesson(
                    id = 0,
                    studentId = draft.studentId,
                    date = candidate.epochMillis,
                    status = LessonStatus.SCHEDULED,
                    durationInHours = null,
                    notes = null,
                    pricingMode = PricingMode.PER_HOUR,
                    rateOrFee = draft.effectiveRate(student.hourlyRate),
                    paymentTimestamp = null
                )
            }

            if (lessonsToCreate.isNotEmpty()) {
                val insertedIds = lessonRepository.insertLessons(lessonsToCreate)
                val createdLessons = lessonsToCreate.zip(insertedIds) { lesson, id ->
                    lesson.copy(id = id)
                }
                
                Result.success(
                    BulkScheduleResult(
                        studentId = draft.studentId,
                        createdLessons = createdLessons,
                        skippedCount = skippedCount,
                        hasPastLessons = hasPastLessons
                    )
                )
            } else {
                Result.success(
                    BulkScheduleResult(
                        studentId = draft.studentId,
                        createdLessons = emptyList(),
                        skippedCount = skippedCount,
                        hasPastLessons = false
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
