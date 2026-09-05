package com.barutdev.tullab.ui.navigation

import androidx.lifecycle.viewModelScope
import com.barutdev.tullab.MainDispatcherRule
import com.barutdev.tullab.domain.model.Homework
import com.barutdev.tullab.domain.model.Lesson
import com.barutdev.tullab.domain.model.Student
import com.barutdev.tullab.domain.model.StudentProfileUpdate
import com.barutdev.tullab.domain.repository.HomeworkRepository
import com.barutdev.tullab.domain.repository.LessonRepository
import com.barutdev.tullab.domain.repository.StudentRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BottomNavPreloadViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun prime_collectsRepositoriesOncePerStudent() = runTest {
        val studentRepository = RecordingStudentRepository()
        val lessonRepository = RecordingLessonRepository()
        val homeworkRepository = RecordingHomeworkRepository()
        val viewModel = BottomNavPreloadViewModel(studentRepository, lessonRepository, homeworkRepository)

        viewModel.prime(studentId = 42)
        advanceUntilIdle()

        assertEquals(1, studentRepository.studentCallCount)
        assertEquals(1, lessonRepository.lessonCallCount)
        assertEquals(1, homeworkRepository.homeworkCallCount)
        assertTrue(viewModel.hasPrimed(42))

        viewModel.prime(studentId = 42)
        advanceUntilIdle()

        assertEquals(1, studentRepository.studentCallCount)
        assertEquals(1, lessonRepository.lessonCallCount)
        assertEquals(1, homeworkRepository.homeworkCallCount)

        cancel(viewModel)
    }

    private fun cancel(viewModel: BottomNavPreloadViewModel) {
        viewModel.viewModelScope.cancel()
    }
}

private class RecordingStudentRepository : StudentRepository {
    var studentCallCount = 0
    override fun getAllStudents(): Flow<List<Student>> = flowOf(emptyList())
    override fun getStudentById(id: Int): Flow<Student?> = flowOf(null).also { studentCallCount += 1 }
    override suspend fun addStudent(student: Student) = error("unused")
    override suspend fun updateStudentHourlyRate(studentId: Int, newRate: Double) = error("unused")
    override suspend fun updateStudentProfile(update: StudentProfileUpdate) = error("unused")
    override suspend fun deleteStudent(studentId: Int) = error("unused")
}

private class RecordingLessonRepository : LessonRepository {
    var lessonCallCount = 0
    override fun getAllLessons(): Flow<List<Lesson>> = flowOf(emptyList())
    override fun getLessonsForStudent(studentId: Int): Flow<List<Lesson>> =
        flowOf(emptyList<Lesson>()).also { lessonCallCount += 1 }
    override suspend fun insertLesson(lesson: Lesson): Int = error("unused")
    override suspend fun updateLesson(lesson: Lesson) = error("unused")
    override suspend fun markCompletedLessonsAsPaid(studentId: Int) = error("unused")
    override suspend fun deleteLesson(lessonId: Int) = error("unused")
    override suspend fun getScheduledLessonCount(studentId: Int): Int = 0
    override suspend fun getScheduledLessonsForStudent(studentId: Int): List<Lesson> = emptyList()
    override suspend fun updateScheduledLessonsRate(studentId: Int, newRate: Double) {}
    override fun getLessonsForDate(date: java.time.LocalDate): Flow<List<Lesson>> = error("unused")
    override fun getCompletedLessonsForDate(date: java.time.LocalDate): Flow<List<Lesson>> = error("unused")
    override suspend fun getLessonWithStudent(lessonId: Int): com.barutdev.tullab.domain.model.LessonWithStudent? = error("unused")
    override fun getActiveLessons(): Flow<List<Lesson>> = error("unused")
}

private class RecordingHomeworkRepository : HomeworkRepository {
    var homeworkCallCount = 0
    override fun getHomeworkForStudent(studentId: Int): Flow<List<Homework>> =
        flowOf(emptyList<Homework>()).also { homeworkCallCount += 1 }
    override fun getHomeworkById(homeworkId: Int): Flow<Homework?> = flowOf(null)
    override suspend fun insertHomework(homework: Homework) = error("unused")
    override suspend fun updateHomework(homework: Homework) = error("unused")
}
