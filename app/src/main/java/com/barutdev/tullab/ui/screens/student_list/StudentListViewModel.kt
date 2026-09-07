package com.barutdev.tullab.ui.screens.student_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barutdev.tullab.domain.model.LessonStatus
import com.barutdev.tullab.domain.model.Student
import com.barutdev.tullab.domain.repository.LessonRepository
import com.barutdev.tullab.domain.repository.StudentRepository
import com.barutdev.tullab.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class StudentWithDebt(
    val student: Student,
    val currentDebt: Double
)

private data class StudentDebt(
    val student: Student,
    val currentDebt: Double
)

data class StudentListUiState(
    val students: List<StudentWithDebt> = emptyList(),
    val defaultHourlyRate: Double = 0.0,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val hasAnyStudents: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class StudentListViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val lessonRepository: LessonRepository,
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val studentsFlow = studentRepository.getAllStudents()
    private val lessonsFlow = lessonRepository.getAllLessons()
    private val searchQuery = MutableStateFlow("")

    private val studentDebtFlow: Flow<List<StudentDebt>> = combine(
        studentsFlow,
        lessonsFlow
    ) { students, lessons ->
        val completedLessonsByStudent = lessons
            .filter { it.status == LessonStatus.COMPLETED }
            .groupBy { lesson -> lesson.studentId }

        students.map { student ->
            val totalDebt = completedLessonsByStudent[student.id]
                ?.sumOf { lesson -> lesson.calculatedValue }
                ?: 0.0
            StudentDebt(
                student = student,
                currentDebt = totalDebt
            )
        }
    }.flowOn(Dispatchers.Default)

    private val defaultHourlyRateFlow: Flow<Double> = userPreferencesRepository.userPreferences
        .map { preferences -> preferences.defaultHourlyRate }

    val uiState: StateFlow<StudentListUiState> = combine(
        studentDebtFlow,
        defaultHourlyRateFlow,
        searchQuery
    ) { studentDebts, defaultHourlyRate, query ->
        val studentsWithDebt = studentDebts.map { (student, currentDebt) ->
            StudentWithDebt(
                student = student,
                currentDebt = currentDebt
            )
        }
        val trimmedQuery = query.trim()
        val filteredStudents = if (trimmedQuery.isEmpty()) {
            studentsWithDebt
        } else {
            studentsWithDebt.filter { studentWithDebt ->
                studentWithDebt.student.fullName.contains(trimmedQuery, ignoreCase = true)
            }
        }

        StudentListUiState(
            students = filteredStudents,
            defaultHourlyRate = defaultHourlyRate,
            searchQuery = query,
            isSearchActive = trimmedQuery.isNotEmpty(),
            hasAnyStudents = studentsWithDebt.isNotEmpty(),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StudentListUiState()
    )

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun onClearSearchQuery() {
        searchQuery.value = ""
    }

    fun deleteStudent(studentId: Int) {
        viewModelScope.launch {
            studentRepository.deleteStudent(studentId)
        }
    }
}
