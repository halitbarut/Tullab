package com.barutdev.tullab.domain.usecase.lesson

import com.barutdev.tullab.domain.model.BatchUndoSession
import com.barutdev.tullab.domain.repository.LessonRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UndoBulkLessonsUseCaseTest {

    private lateinit var lessonRepository: LessonRepository
    private lateinit var useCase: UndoBulkLessonsUseCase

    @Before
    fun setup() {
        lessonRepository = mockk(relaxed = true)
        useCase = UndoBulkLessonsUseCase(lessonRepository)
    }

    @Test
    fun `deletes lessons correctly`() = runTest {
        val session = BatchUndoSession(studentId = 1, lessonIds = listOf(1, 2, 3))
        
        val result = useCase(session)
        
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { lessonRepository.deleteLessons(listOf(1, 2, 3)) }
    }
}
