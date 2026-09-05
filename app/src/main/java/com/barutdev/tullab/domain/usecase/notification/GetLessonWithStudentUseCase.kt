package com.barutdev.tullab.domain.usecase.notification

import com.barutdev.tullab.domain.model.LessonWithStudent
import com.barutdev.tullab.domain.repository.LessonRepository
import javax.inject.Inject

/**
 * UseCase for retrieving a lesson with its associated student data.
 *
 * Used by NotificationAlarmReceiver to fetch fresh lesson and student data
 * at notification time, ensuring the notification contains up-to-date information.
 *
 * Returns null if lesson or student not found.
 */
class GetLessonWithStudentUseCase @Inject constructor(
    private val lessonRepository: LessonRepository
) {
    suspend operator fun invoke(lessonId: Int): LessonWithStudent? {
        return lessonRepository.getLessonWithStudent(lessonId)
    }
}
