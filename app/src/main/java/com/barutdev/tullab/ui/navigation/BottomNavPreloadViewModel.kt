package com.barutdev.tullab.ui.navigation

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barutdev.tullab.domain.repository.HomeworkRepository
import com.barutdev.tullab.domain.repository.LessonRepository
import com.barutdev.tullab.domain.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@HiltViewModel
class BottomNavPreloadViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val lessonRepository: LessonRepository,
    private val homeworkRepository: HomeworkRepository
) : ViewModel() {

    private val primedStudentIds = mutableSetOf<Int>()
    private val primeMutex = Mutex()

    @VisibleForTesting
    internal fun hasPrimed(studentId: Int): Boolean = primedStudentIds.contains(studentId)

    fun prime(studentId: Int) {
        viewModelScope.launch {
            val shouldPrime = primeMutex.withLock {
                if (primedStudentIds.contains(studentId)) {
                    false
                } else {
                    primedStudentIds.add(studentId)
                    true
                }
            }
            if (!shouldPrime) return@launch

            studentRepository.getStudentById(studentId).firstOrNull()
            lessonRepository.getLessonsForStudent(studentId).firstOrNull()
            homeworkRepository.getHomeworkForStudent(studentId).firstOrNull()
        }
    }
}
