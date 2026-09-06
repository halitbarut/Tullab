package com.barutdev.tullab.domain.usecase.lesson

import com.barutdev.tullab.domain.model.BatchUndoSession
import com.barutdev.tullab.domain.repository.LessonRepository
import javax.inject.Inject

class UndoBulkLessonsUseCase @Inject constructor(
    private val lessonRepository: LessonRepository
) {
    suspend operator fun invoke(session: BatchUndoSession): Result<Unit> {
        return try {
            if (session.lessonIds.isNotEmpty()) {
                lessonRepository.deleteLessons(session.lessonIds)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
