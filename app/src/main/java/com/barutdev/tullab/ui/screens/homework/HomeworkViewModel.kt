package com.barutdev.tullab.ui.screens.homework

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barutdev.tullab.R
import com.barutdev.tullab.domain.model.Homework
import com.barutdev.tullab.domain.model.HomeworkStatus
import com.barutdev.tullab.domain.model.Student
import com.barutdev.tullab.domain.repository.HomeworkRepository
import com.barutdev.tullab.domain.repository.StudentRepository
import com.barutdev.tullab.navigation.STUDENT_ID_ARG
import com.barutdev.tullab.navigation.HOMEWORK_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeworkViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val homeworkRepository: HomeworkRepository,
    private val studentRepository: StudentRepository,
) : ViewModel() {

    private val studentIdState: StateFlow<Int?> =
        savedStateHandle.getStateFlow<Int?>(STUDENT_ID_ARG, savedStateHandle[STUDENT_ID_ARG])

    val studentId: Int?
        get() = studentIdState.value

    val hasStudentReference: Boolean
        get() = studentId != null

    private val student: StateFlow<Student?> = studentIdState
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                studentRepository.getStudentById(id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val studentName: StateFlow<String> = student
        .map { studentSnapshot -> studentSnapshot?.fullName ?: "" }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
        )

    val homework: StateFlow<List<Homework>> = studentIdState
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList())
            } else {
                homeworkRepository.getHomeworkForStudent(id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val dialogVisibility = MutableStateFlow(false)
    val isDialogVisible: StateFlow<Boolean> = dialogVisibility.asStateFlow()

    private val editingHomeworkState = MutableStateFlow<Homework?>(null)
    val editingHomework: StateFlow<Homework?> = editingHomeworkState.asStateFlow()

    init {
        Log.d("HomeworkViewModel", "Created for studentId=$studentId")
        
        val initialHomeworkId = savedStateHandle.get<Int>(HOMEWORK_ID_ARG)?.takeIf { it != -1 }
        if (initialHomeworkId != null) {
            viewModelScope.launch {
                val homeworkItem = homeworkRepository.getHomeworkById(initialHomeworkId).first()
                if (homeworkItem != null) {
                    showEditHomeworkDialog(homeworkItem)
                }
            }
        }
    }


    fun showAddHomeworkDialog() {
        editingHomeworkState.value = null
        dialogVisibility.value = true
    }

    fun showEditHomeworkDialog(homework: Homework) {
        editingHomeworkState.value = homework
        dialogVisibility.value = true
    }

    fun dismissHomeworkDialog() {
        dialogVisibility.value = false
        editingHomeworkState.value = null
    }

    fun onSubmitHomework(
        title: String,
        description: String,
        dueDate: Long,
        status: HomeworkStatus,
        performanceNotes: String?
    ) {
        val editingHomework = editingHomeworkState.value
        viewModelScope.launch {
            if (editingHomework == null) {
                addHomework(
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    status = status,
                    performanceNotes = performanceNotes
                )
            } else {
                updateHomework(
                    homework = editingHomework,
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    status = status,
                    performanceNotes = performanceNotes
                )
            }
            dismissHomeworkDialog()
        }
    }

    private suspend fun addHomework(
        title: String,
        description: String,
        dueDate: Long,
        status: HomeworkStatus,
        performanceNotes: String?
    ) {
        val targetStudentId = studentId ?: return
        val homework = Homework(
            id = 0,
            studentId = targetStudentId,
            title = title.trim(),
            description = description.trim(),
            creationDate = System.currentTimeMillis(),
            dueDate = dueDate,
            status = status,
            performanceNotes = performanceNotes?.trim().takeIf { it?.isNotBlank() == true }
        )
        homeworkRepository.insertHomework(homework)
    }

    private suspend fun updateHomework(
        homework: Homework,
        title: String,
        description: String,
        dueDate: Long,
        status: HomeworkStatus,
        performanceNotes: String?
    ) {
        val updated = homework.copy(
            title = title.trim(),
            description = description.trim(),
            dueDate = dueDate,
            status = status,
            performanceNotes = performanceNotes?.trim().takeIf { it?.isNotBlank() == true }
        )
        homeworkRepository.updateHomework(updated)
    }
}
