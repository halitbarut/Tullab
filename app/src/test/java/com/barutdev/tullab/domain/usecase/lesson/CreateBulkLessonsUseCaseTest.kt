package com.barutdev.tullab.domain.usecase.lesson

import com.barutdev.tullab.domain.model.BulkLessonCandidate
import com.barutdev.tullab.domain.model.BulkScheduleDraft
import com.barutdev.tullab.domain.model.Student
import com.barutdev.tullab.domain.repository.LessonRepository
import com.barutdev.tullab.domain.repository.StudentRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class CreateBulkLessonsUseCaseTest {

    private lateinit var lessonRepository: LessonRepository
    private lateinit var studentRepository: StudentRepository
    private lateinit var useCase: CreateBulkLessonsUseCase

    @Before
    fun setup() {
        lessonRepository = mockk()
        studentRepository = mockk()
        useCase = CreateBulkLessonsUseCase(lessonRepository, studentRepository)
    }

    @Test
    fun `filters out conflicts and creates lessons`() = runTest {
        val student = Student(id = 1, fullName = "Test", hourlyRate = 50.0)
        coEvery { studentRepository.getStudentById(1) } returns flowOf(student)
        
        val draft = BulkScheduleDraft(studentId = 1)
        
        val candidates = listOf(
            BulkLessonCandidate(LocalDate.now(), LocalTime.of(10, 0), 1000L, isConflict = false, isPast = false),
            BulkLessonCandidate(LocalDate.now(), LocalTime.of(11, 0), 2000L, isConflict = true, isPast = false)
        )
        
        coEvery { lessonRepository.insertLessons(any()) } returns listOf(1)
        
        val result = useCase(draft, candidates)
        
        assertTrue(result.isSuccess)
        val successData = result.getOrThrow()
        assertEquals(1, successData.createdCount)
        assertEquals(1, successData.skippedCount)
        assertEquals(false, successData.hasPastLessons)
        assertEquals(50.0, successData.createdLessons[0].rateOrFee, 0.0)
    }

    @Test
    fun `uses custom rate when specified in draft`() = runTest {
        val student = Student(id = 1, fullName = "Test", hourlyRate = 50.0)
        coEvery { studentRepository.getStudentById(1) } returns flowOf(student)
        
        val draft = BulkScheduleDraft(studentId = 1, useCustomRate = true, customRate = 75.0)
        
        val candidates = listOf(
            BulkLessonCandidate(LocalDate.now(), LocalTime.of(10, 0), 1000L, isConflict = false, isPast = false)
        )
        
        coEvery { lessonRepository.insertLessons(any()) } returns listOf(1)
        
        val result = useCase(draft, candidates)
        
        assertTrue(result.isSuccess)
        val successData = result.getOrThrow()
        assertEquals(75.0, successData.createdLessons[0].rateOrFee, 0.0)
    }
}
