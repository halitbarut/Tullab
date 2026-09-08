package com.barutdev.tullab.ui.screens.calendar

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barutdev.tullab.domain.model.Homework
import com.barutdev.tullab.domain.model.HomeworkStatus
import com.barutdev.tullab.domain.model.Lesson
import com.barutdev.tullab.domain.model.LessonStatus
import com.barutdev.tullab.domain.repository.HomeworkRepository
import com.barutdev.tullab.domain.repository.LessonRepository
import com.barutdev.tullab.domain.repository.StudentRepository
import com.barutdev.tullab.domain.repository.UserPreferencesRepository
import com.barutdev.tullab.domain.usecase.notification.CancelNotificationAlarmsUseCase
import com.barutdev.tullab.domain.usecase.notification.ScheduleNotificationAlarmsUseCase
import com.barutdev.tullab.domain.usecase.lesson.UndoBulkLessonsUseCase
import com.barutdev.tullab.domain.model.BatchUndoSession
import com.barutdev.tullab.navigation.STUDENT_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.barutdev.tullab.domain.repository.PaymentRepository

@HiltViewModel
class CalendarViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val studentRepository: StudentRepository,
    private val lessonRepository: LessonRepository,
    private val homeworkRepository: HomeworkRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val paymentRepository: PaymentRepository,
    private val scheduleNotificationAlarmsUseCase: ScheduleNotificationAlarmsUseCase,
    private val cancelNotificationAlarmsUseCase: CancelNotificationAlarmsUseCase,
    private val undoBulkLessonsUseCase: UndoBulkLessonsUseCase
) : ViewModel() {

    val studentId: Int? = savedStateHandle[STUDENT_ID_ARG]
    val hasStudentReference: Boolean = studentId != null

    private val zoneId: ZoneId = ZoneId.systemDefault()
    private val initialDate = LocalDate.now(zoneId)

    private val currentMonthState = MutableStateFlow(YearMonth.from(initialDate))
    val currentMonth: StateFlow<YearMonth> = currentMonthState.asStateFlow()

    private val selectedDateState = MutableStateFlow(initialDate)
    val selectedDate: StateFlow<LocalDate> = selectedDateState.asStateFlow()

    private val logLessonDialogVisibility = MutableStateFlow(false)
    val isLogLessonDialogVisible: StateFlow<Boolean> = logLessonDialogVisibility.asStateFlow()

    private val selectedLessonForLogging = MutableStateFlow<Lesson?>(null)
    val lessonToLog: StateFlow<Lesson?> = selectedLessonForLogging.asStateFlow()

    private val studentNameState: StateFlow<String> = studentId?.let { id ->
        studentRepository.getStudentById(id)
            .map { student -> student?.fullName ?: "" }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ""
            )
    } ?: MutableStateFlow("")
    val studentName: StateFlow<String> = studentNameState

    private val lessonsState: StateFlow<List<Lesson>> = studentId?.let { id ->
        lessonRepository.getLessonsForStudent(id)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )
    } ?: MutableStateFlow(emptyList())
    val lessons: StateFlow<List<Lesson>> = lessonsState

    private val homeworkState: StateFlow<List<Homework>> = studentId?.let { id ->
        homeworkRepository.getHomeworkForStudent(id)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )
    } ?: MutableStateFlow(emptyList())
    val homework: StateFlow<List<Homework>> = homeworkState

    init {
        Log.d("CalendarViewModel", "Created for studentId=$studentId")
    }

    fun onPreviousMonth() {
        val newMonth = currentMonthState.value.minusMonths(1)
        currentMonthState.value = newMonth
        selectedDateState.value = newMonth.atDay(1)
    }

    fun onNextMonth() {
        val newMonth = currentMonthState.value.plusMonths(1)
        currentMonthState.value = newMonth
        selectedDateState.value = newMonth.atDay(1)
    }

    fun onSelectDate(date: LocalDate) {
        selectedDateState.value = date
    }

    suspend fun saveLesson(date: Long) {
        val targetStudentId = studentId ?: return
        val student = studentRepository.getStudentById(targetStudentId).firstOrNull() ?: return
        val prefs = userPreferencesRepository.userPreferences.first()
        val activeRate = student.customHourlyRate ?: student.hourlyRate.takeIf { it > 0.0 } ?: prefs.defaultHourlyRate
        
        val lesson = Lesson(
            id = 0,
            studentId = targetStudentId,
            date = date,
            status = LessonStatus.SCHEDULED,
            durationInHours = null,
            notes = null,
            pricingMode = com.barutdev.tullab.domain.model.PricingMode.PER_HOUR,
            rateOrFee = activeRate
        )
        val lessonId = lessonRepository.insertLesson(lesson)
        scheduleNotificationAlarmsUseCase(lessonId)
    }

    fun deleteLesson(lessonId: Int) {
        viewModelScope.launch {
            cancelNotificationAlarmsUseCase(lessonId)
            lessonRepository.deleteLesson(lessonId)
            clearLogLessonSelection()
        }
    }

    fun onLogLessonClicked(lesson: Lesson) {
        selectedLessonForLogging.value = lesson
        logLessonDialogVisibility.value = true
    }

    fun dismissLogLessonDialog() {
        clearLogLessonSelection()
    }

    fun onLogLessonComplete(duration: String, notes: String, pricingMode: com.barutdev.tullab.domain.model.PricingMode, rateOrFeeInput: String) {
        val lessonId = selectedLessonForLogging.value?.id ?: return
        viewModelScope.launch {
            completeLesson(lessonId, duration, notes, pricingMode, rateOrFeeInput)
        }
    }

    fun onSaveLessonDetails(
        lesson: Lesson,
        duration: String,
        notes: String,
        pricingMode: com.barutdev.tullab.domain.model.PricingMode,
        rateOrFee: String,
        isCompleted: Boolean,
        dateMillis: Long = lesson.date
    ) {
        viewModelScope.launch {
            val normalizedDuration = duration.trim().replace(',', '.')
            val durationValue = normalizedDuration.toDoubleOrNull()
            val normalizedRate = rateOrFee.trim().replace(',', '.')
            val rateValue = normalizedRate.toDoubleOrNull() ?: lesson.rateOrFee

            val newStatus = when {
                lesson.status == LessonStatus.PAID -> LessonStatus.PAID
                isCompleted -> LessonStatus.COMPLETED
                else -> LessonStatus.SCHEDULED
            }

            val updatedLesson = lesson.copy(
                date = dateMillis,
                status = newStatus,
                durationInHours = if (pricingMode == com.barutdev.tullab.domain.model.PricingMode.PER_HOUR) durationValue else null,
                notes = notes.trim().ifEmpty { null },
                pricingMode = pricingMode,
                rateOrFee = rateValue
            )
            lessonRepository.updateLesson(updatedLesson)
            when (newStatus) {
                LessonStatus.COMPLETED -> cancelNotificationAlarmsUseCase(lesson.id)
                LessonStatus.SCHEDULED -> {
                    cancelNotificationAlarmsUseCase(lesson.id)
                    scheduleNotificationAlarmsUseCase(lesson.id)
                }
                else -> cancelNotificationAlarmsUseCase(lesson.id)
            }
            clearLogLessonSelection()
        }
    }

    fun onSaveScheduledLesson(lesson: Lesson, duration: String, notes: String, pricingMode: com.barutdev.tullab.domain.model.PricingMode, rateOrFee: String) {
        viewModelScope.launch {
            val normalizedDuration = duration.trim().replace(',', '.')
            val durationValue = normalizedDuration.toDoubleOrNull()
            
            val normalizedRate = rateOrFee.trim().replace(',', '.')
            val rateValue = normalizedRate.toDoubleOrNull() ?: lesson.rateOrFee

            val updatedLesson = lesson.copy(
                durationInHours = if (pricingMode == com.barutdev.tullab.domain.model.PricingMode.PER_HOUR) durationValue else null,
                notes = notes.trim().ifEmpty { null },
                pricingMode = pricingMode,
                rateOrFee = rateValue
            )
            lessonRepository.updateLesson(updatedLesson)
            clearLogLessonSelection()
        }
    }

    fun onLogLessonMarkNotDone(notes: String, dateMillis: Long? = null) {
        val lessonId = selectedLessonForLogging.value?.id ?: return
        viewModelScope.launch {
            markLessonNotDone(lessonId, notes, dateMillis)
        }
    }

    private suspend fun completeLesson(lessonId: Int, duration: String, notes: String, pricingMode: com.barutdev.tullab.domain.model.PricingMode, rateOrFeeInput: String) {
        val normalizedDuration = duration.trim().replace(',', '.')
        val durationValue = normalizedDuration.toDoubleOrNull()
        if (pricingMode == com.barutdev.tullab.domain.model.PricingMode.PER_HOUR && (durationValue == null || durationValue <= 0.0)) {
            return
        }
        val parsedRateOrFee = rateOrFeeInput.trim().replace(',', '.').toDoubleOrNull()
        val lesson = lessons.value.firstOrNull { it.id == lessonId } ?: return
        val updatedLesson = lesson.copy(
            status = LessonStatus.COMPLETED,
            durationInHours = if (pricingMode == com.barutdev.tullab.domain.model.PricingMode.PER_HOUR) durationValue else null,
            notes = notes.trim().takeIf { it.isNotBlank() },
            pricingMode = pricingMode,
            rateOrFee = parsedRateOrFee ?: lesson.rateOrFee
        )
        lessonRepository.updateLesson(updatedLesson)
        cancelNotificationAlarmsUseCase(lessonId)
        clearLogLessonSelection()
    }

    private suspend fun markLessonNotDone(lessonId: Int, notes: String, dateMillis: Long? = null) {
        val lesson = lessons.value.firstOrNull { it.id == lessonId } ?: selectedLessonForLogging.value ?: return
        val updatedLesson = lesson.copy(
            status = LessonStatus.CANCELLED,
            date = dateMillis ?: lesson.date,
            durationInHours = null,
            notes = notes.trim().takeIf { it.isNotBlank() }
        )
        lessonRepository.updateLesson(updatedLesson)
        cancelNotificationAlarmsUseCase(lessonId)
        clearLogLessonSelection()
    }

    private fun clearLogLessonSelection() {
        logLessonDialogVisibility.value = false
        selectedLessonForLogging.value = null
    }

    private val _pendingLessonForPayment = MutableStateFlow<Lesson?>(null)
    val pendingLessonForPayment = _pendingLessonForPayment.asStateFlow()
    
    val currencyCode = userPreferencesRepository.userPreferences
        .map { it.currencyCode }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "USD"
        )

    private val requiresFeePromptState = MutableStateFlow(false)
    val requiresFeePrompt: StateFlow<Boolean> = requiresFeePromptState.asStateFlow()

    fun onMarkLessonAsPaidClicked(lesson: Lesson) {
        viewModelScope.launch {
            if (lesson.status == LessonStatus.COMPLETED && lesson.durationInHours != null && lesson.rateOrFee > 0.0) {
                paymentRepository.markLessonAsPaid(lesson.id, lesson.durationInHours, null)
            } else {
                requiresFeePromptState.value = lesson.rateOrFee <= 0.0
                _pendingLessonForPayment.value = lesson
            }
        }
    }

    fun dismissMarkLessonAsPaidDialog() {
        _pendingLessonForPayment.value = null
        requiresFeePromptState.value = false
    }

    fun onConfirmMarkLessonAsPaid(duration: Double?, customFee: Double?) {
        val lessonId = _pendingLessonForPayment.value?.id ?: return
        viewModelScope.launch {
            if (_pendingLessonForPayment.value?.status == LessonStatus.SCHEDULED) {
                cancelNotificationAlarmsUseCase(lessonId)
            }
            paymentRepository.markLessonAsPaid(lessonId, duration, customFee)
            _pendingLessonForPayment.value = null
        }
    }

    private val lessonToRevertState = MutableStateFlow<Lesson?>(null)
    val lessonToRevert: StateFlow<Lesson?> = lessonToRevertState.asStateFlow()

    fun onRevertLessonPaymentClicked(lesson: Lesson) {
        lessonToRevertState.value = lesson
    }

    fun dismissRevertDialog() {
        lessonToRevertState.value = null
    }

    fun onConfirmRevertPayment() {
        val lessonId = lessonToRevertState.value?.id ?: return
        viewModelScope.launch {
            paymentRepository.revertLessonPayment(lessonId)
            lessonToRevertState.value = null
        }
    }

    fun toggleHomeworkStatus(homework: Homework) {
        if (homework.status == HomeworkStatus.CANCELLED) return
        
        viewModelScope.launch {
            val newStatus = when (homework.status) {
                HomeworkStatus.PENDING -> HomeworkStatus.COMPLETED
                HomeworkStatus.COMPLETED -> HomeworkStatus.PENDING
                HomeworkStatus.CANCELLED -> HomeworkStatus.CANCELLED
            }
            homeworkRepository.updateHomework(homework.copy(status = newStatus))
        }
    }

    /**
     * Reverts a lesson payment as part of an undo action triggered by the Snackbar.
     * Delegates directly to [paymentRepository.revertLessonPayment].
     */
    fun revertLessonPaymentUndo(lessonId: Int) {
        viewModelScope.launch {
            paymentRepository.revertLessonPayment(lessonId)
        }
    }

    /**
     * Immediately deletes a lesson from the database and invokes [onDeleted] with the lesson
     * snapshot so the caller can offer an undo Snackbar.
     */
    fun deleteLessonWithUndo(lessonId: Int, onDeleted: (Lesson) -> Unit) {
        viewModelScope.launch {
            val lesson = lessons.value.firstOrNull { it.id == lessonId } ?: return@launch
            cancelNotificationAlarmsUseCase(lessonId)
            lessonRepository.deleteLesson(lessonId)
            clearLogLessonSelection()
            onDeleted(lesson)
        }
    }

    /**
     * Re-inserts a lesson snapshot into the database as an undo for deletion.
     */
    fun restoreLesson(lesson: Lesson) {
        viewModelScope.launch {
            val restoredId = lessonRepository.insertLesson(lesson.copy(id = 0))
            scheduleNotificationAlarmsUseCase(restoredId)
        }
    }

    /**
     * Reverts homework status as part of an undo action triggered by the Snackbar.
     */
    fun revertHomeworkStatusUndo(homework: Homework, previousStatus: HomeworkStatus) {
        viewModelScope.launch {
            homeworkRepository.updateHomework(homework.copy(status = previousStatus))
        }
    }

    private val batchUndoSessionState = MutableStateFlow<BatchUndoSession?>(null)
    val batchUndoSession: StateFlow<BatchUndoSession?> = batchUndoSessionState.asStateFlow()

    fun setBatchUndoSession(session: BatchUndoSession?) {
        batchUndoSessionState.value = session
    }

    fun undoBulkLessons() {
        val session = batchUndoSessionState.value ?: return
        viewModelScope.launch {
            val result = undoBulkLessonsUseCase(session)
            if (result.isSuccess) {
                batchUndoSessionState.value = null
            }
        }
    }
}

