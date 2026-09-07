package com.barutdev.tullab.ui.screens.dashboard

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barutdev.tullab.R
import com.barutdev.tullab.domain.model.Homework
import com.barutdev.tullab.domain.model.Lesson
import com.barutdev.tullab.domain.model.LessonStatus
import com.barutdev.tullab.domain.model.Student
import com.barutdev.tullab.domain.model.PaymentRecord
import com.barutdev.tullab.domain.repository.LessonRepository
import com.barutdev.tullab.domain.repository.HomeworkRepository
import com.barutdev.tullab.domain.repository.StudentRepository
import com.barutdev.tullab.domain.repository.PaymentRepository
import com.barutdev.tullab.domain.repository.UserPreferencesRepository
import com.barutdev.tullab.domain.usecase.notification.CancelNotificationAlarmsUseCase
import com.barutdev.tullab.domain.usecase.notification.ScheduleNotificationAlarmsUseCase
import com.barutdev.tullab.navigation.STUDENT_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.flowOn

data class DashboardUiState(
    val studentId: Int? = null,
    val studentName: String = "",
    val hourlyRate: Double = 0.0,
    val totalHours: Double = 0.0,
    val totalAmountDue: Double = 0.0,
    val completedLessonsAwaitingPayment: List<Lesson> = emptyList(),
    /** Breakdown of the current payment cycle by (pricingMode, rateOrFee) — drives multi-rate display in PaymentTrackingCard. */
    val rateBreakdownTiers: List<PaymentBreakdownTier> = emptyList(),
    val lastPaymentDate: Long? = null,
    val isAddLessonDialogVisible: Boolean = false,
    val upcomingLessons: List<Lesson> = emptyList(),
    val pastLessonsToLog: List<Lesson> = emptyList(),
    val isLogLessonDialogVisible: Boolean = false,
    val lessonToLog: Lesson? = null,
    val isPaymentHistoryDialogVisible: Boolean = false,
    val isMarkAsPaidDialogVisible: Boolean = false
)

sealed interface DashboardEvent {
    data object StudentRemoved : DashboardEvent
    data class ShowToast(val messageRes: Int) : DashboardEvent
}

private data class DashboardComputation(
    val student: Student?,
    val completedLessons: List<Lesson>,
    val upcomingLessons: List<Lesson>,
    val pastLessonsToLog: List<Lesson>,
    val totalHours: Double,
    val lastPaymentDate: Long?,
    val isAddLessonDialogVisible: Boolean,
    val isLogLessonDialogVisible: Boolean,
    val lessonToLog: Lesson?
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val studentRepository: StudentRepository,
    private val lessonRepository: LessonRepository,
    private val homeworkRepository: HomeworkRepository,
    private val cancelNotificationAlarmsUseCase: CancelNotificationAlarmsUseCase,
    private val scheduleNotificationAlarmsUseCase: ScheduleNotificationAlarmsUseCase,
    private val paymentRepository: PaymentRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val studentId: Int = checkNotNull(
        savedStateHandle[STUDENT_ID_ARG]
    )

    val boundStudentId: Int = studentId

    private val _events = MutableSharedFlow<DashboardEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<DashboardEvent> = _events.asSharedFlow()

    private var hasReceivedInitialStudentEmission = false

    private val addLessonDialogVisibility = MutableStateFlow(false)
    private val logLessonDialogVisibility = MutableStateFlow(false)
    private val selectedLessonForLogging = MutableStateFlow<Lesson?>(null)

    private val paymentHistoryDialogVisibility = MutableStateFlow(false)
    private val markAsPaidDialogVisibility = MutableStateFlow(false)

    val student: StateFlow<Student?> = studentRepository.getStudentById(studentId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val lessons: StateFlow<List<Lesson>> = lessonRepository.getLessonsForStudent(studentId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val homework: StateFlow<List<Homework>> =
        homeworkRepository.getHomeworkForStudent(studentId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val paymentHistory: StateFlow<List<PaymentRecord>> =
        paymentRepository.observePaymentHistory(studentId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    init {
        Log.d("DashboardViewModel", "Created for studentId=$studentId")
        viewModelScope.launch {
            var previousStudentSnapshot: Student? = null
            student.collect { studentSnapshot ->
                if (!hasReceivedInitialStudentEmission) {
                    hasReceivedInitialStudentEmission = true
                    if (studentSnapshot == null) {
                        _events.tryEmit(DashboardEvent.StudentRemoved)
                    }
                } else if (previousStudentSnapshot != null && studentSnapshot == null) {
                    _events.tryEmit(DashboardEvent.StudentRemoved)
                }
                previousStudentSnapshot = studentSnapshot
            }
        }
    }

    private val defaultHourlyRateFlow = userPreferencesRepository.userPreferences
        .map { preferences -> preferences.defaultHourlyRate }

    private val dashboardComputation: kotlinx.coroutines.flow.Flow<DashboardComputation> = combine(
        student,
        lessons,
        addLessonDialogVisibility,
        logLessonDialogVisibility,
        selectedLessonForLogging
    ) { student, lessons, isAddLessonDialogVisible, isLogDialogVisible, lessonToLog ->
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        val upcomingEndDate = today.plusDays(7)

        val completedLessons = lessons
            .filter { it.status == LessonStatus.COMPLETED }
            .sortedByDescending { it.date }
        val upcomingLessons = lessons
            .filter { lesson ->
                if (lesson.status != LessonStatus.SCHEDULED) return@filter false
                val lessonDate = Instant.ofEpochMilli(lesson.date).atZone(zoneId).toLocalDate()
                !lessonDate.isBefore(today) && !lessonDate.isAfter(upcomingEndDate)
            }
            .sortedBy { it.date }
        val pastLessonsToLog = lessons
            .filter { lesson ->
                if (lesson.status != LessonStatus.SCHEDULED) return@filter false
                val lessonDate = Instant.ofEpochMilli(lesson.date).atZone(zoneId).toLocalDate()
                lessonDate.isBefore(today)
            }
            .sortedByDescending { it.date }
        val totalHours = completedLessons.mapNotNull { it.durationInHours }.sum()
        DashboardComputation(
            student = student,
            completedLessons = completedLessons,
            upcomingLessons = upcomingLessons,
            pastLessonsToLog = pastLessonsToLog,
            totalHours = totalHours,
            lastPaymentDate = student?.lastPaymentDate,
            isAddLessonDialogVisible = isAddLessonDialogVisible,
            isLogLessonDialogVisible = isLogDialogVisible,
            lessonToLog = lessonToLog
        )
    }.flowOn(Dispatchers.Default)

    private val baseUiState: kotlinx.coroutines.flow.Flow<DashboardUiState> = combine(
        dashboardComputation,
        defaultHourlyRateFlow
    ) { computation, defaultHourlyRate ->
        val student = computation.student
        val hourlyRate = student?.customHourlyRate ?: defaultHourlyRate
        DashboardUiState(
            studentId = student?.id,
            studentName = student?.fullName.orEmpty(),
            hourlyRate = hourlyRate,
            totalHours = computation.totalHours,
            totalAmountDue = computation.completedLessons.sumOf { it.calculatedValue },
            completedLessonsAwaitingPayment = computation.completedLessons,
            rateBreakdownTiers = computePaymentBreakdownTiers(computation.completedLessons),
            lastPaymentDate = computation.lastPaymentDate,
            isAddLessonDialogVisible = computation.isAddLessonDialogVisible,
            upcomingLessons = computation.upcomingLessons,
            pastLessonsToLog = computation.pastLessonsToLog,
            isLogLessonDialogVisible = computation.isLogLessonDialogVisible,
            lessonToLog = computation.lessonToLog
        )
    }.flowOn(Dispatchers.Default)

    val uiState: StateFlow<DashboardUiState> = combine(
        baseUiState,
        paymentHistoryDialogVisibility,
        markAsPaidDialogVisibility
    ) { base, isPaymentHistoryVisible, isMarkAsPaidVisible ->
        base.copy(
            isPaymentHistoryDialogVisible = isPaymentHistoryVisible,
            isMarkAsPaidDialogVisible = isMarkAsPaidVisible
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )

    fun showAddLessonDialog() {
        addLessonDialogVisibility.value = true
    }

    fun dismissAddLessonDialog() {
        addLessonDialogVisibility.value = false
    }

    suspend fun addLesson(duration: String, notes: String, pricingMode: com.barutdev.tullab.domain.model.PricingMode, rateOrFeeInput: String) {
        val normalizedDuration = duration.trim().replace(',', '.')
        val durationValue = normalizedDuration.toDoubleOrNull()
        if (pricingMode == com.barutdev.tullab.domain.model.PricingMode.PER_HOUR && (durationValue == null || durationValue <= 0.0)) {
            return
        }

        val studentSnapshot = studentRepository.getStudentById(studentId).firstOrNull() ?: return
        val prefs = userPreferencesRepository.userPreferences.first()
        val activeRate = studentSnapshot.customHourlyRate ?: if (studentSnapshot.hourlyRate > 0.0) studentSnapshot.hourlyRate else prefs.defaultHourlyRate

        val parsedRateOrFee = rateOrFeeInput.trim().replace(',', '.').toDoubleOrNull()
        val finalRateOrFee = parsedRateOrFee ?: activeRate

        val lesson = Lesson(
            id = 0,
            studentId = studentId,
            date = System.currentTimeMillis(),
            status = LessonStatus.COMPLETED,
            durationInHours = if (pricingMode == com.barutdev.tullab.domain.model.PricingMode.PER_HOUR) durationValue else null,
            notes = notes.trim().takeIf { it.isNotBlank() },
            pricingMode = pricingMode,
            rateOrFee = finalRateOrFee
        )

        lessonRepository.insertLesson(lesson)
        addLessonDialogVisibility.value = false
    }

    fun onSaveLesson(duration: String, notes: String, pricingMode: com.barutdev.tullab.domain.model.PricingMode, rateOrFeeInput: String) {
        viewModelScope.launch {
            addLesson(duration, notes, pricingMode, rateOrFeeInput)
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
            clearLogLessonSelection()
        }
    }

    fun onSaveLessonDetails(
        lesson: Lesson,
        duration: String,
        notes: String,
        pricingMode: com.barutdev.tullab.domain.model.PricingMode,
        rateOrFee: String,
        isCompleted: Boolean
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
                status = newStatus,
                durationInHours = if (pricingMode == com.barutdev.tullab.domain.model.PricingMode.PER_HOUR) durationValue else null,
                notes = notes.trim().ifEmpty { null },
                pricingMode = pricingMode,
                rateOrFee = rateValue
            )
            lessonRepository.updateLesson(updatedLesson)
            when (newStatus) {
                LessonStatus.COMPLETED -> cancelNotificationAlarmsUseCase(lesson.id)
                LessonStatus.SCHEDULED -> scheduleNotificationAlarmsUseCase(lesson.id)
                else -> Unit
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

    fun onLogLessonMarkNotDone(notes: String) {
        val lessonId = selectedLessonForLogging.value?.id ?: return
        viewModelScope.launch {
            markLessonNotDone(lessonId, notes)
        }
    }

    suspend fun markCurrentCycleAsPaid() {
        val hasLessonsToMark = lessons.value.any { it.status == LessonStatus.COMPLETED }
        if (!hasLessonsToMark) return
        paymentRepository.markStudentAsPaid(studentId)
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

    private suspend fun markLessonNotDone(lessonId: Int, notes: String) {
        val lesson = lessons.value.firstOrNull { it.id == lessonId } ?: return
        val updatedLesson = lesson.copy(
            status = LessonStatus.CANCELLED,
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

fun showPaymentHistoryDialog() {
        val history = paymentHistory.value
        if (history.isEmpty()) {
            _events.tryEmit(DashboardEvent.ShowToast(R.string.no_payment_history_toast))
        } else {
            paymentHistoryDialogVisibility.value = true
        }
    }

    fun dismissPaymentHistoryDialog() {
        paymentHistoryDialogVisibility.value = false
    }

    fun showMarkAsPaidDialog() {
        markAsPaidDialogVisibility.value = true
    }

    fun dismissMarkAsPaidDialog() {
        markAsPaidDialogVisibility.value = false
    }

    fun confirmMarkAsPaidAndDismiss() {
        viewModelScope.launch {
            try {
                markCurrentCycleAsPaid()
            } finally {
                markAsPaidDialogVisibility.value = false
            }
        }
    }
}
