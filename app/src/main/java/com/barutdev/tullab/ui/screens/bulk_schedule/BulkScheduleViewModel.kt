package com.barutdev.tullab.ui.screens.bulk_schedule

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barutdev.tullab.R
import com.barutdev.tullab.domain.model.BatchUndoSession
import com.barutdev.tullab.domain.model.BulkScheduleDraft
import com.barutdev.tullab.domain.model.BulkScheduleMode
import com.barutdev.tullab.domain.model.WeeklyRoutineEndCondition
import com.barutdev.tullab.domain.usecase.lesson.CalculateBulkLessonCandidatesUseCase
import com.barutdev.tullab.domain.usecase.lesson.CreateBulkLessonsUseCase
import com.barutdev.tullab.domain.usecase.lesson.UndoBulkLessonsUseCase
import com.barutdev.tullab.domain.repository.LessonRepository
import com.barutdev.tullab.domain.repository.HomeworkRepository
import com.barutdev.tullab.navigation.STUDENT_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BulkScheduleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val calculateCandidatesUseCase: CalculateBulkLessonCandidatesUseCase,
    private val createLessonsUseCase: CreateBulkLessonsUseCase,
    private val undoUseCase: UndoBulkLessonsUseCase,
    private val lessonRepository: LessonRepository,
    private val homeworkRepository: HomeworkRepository
) : ViewModel() {

    private val studentId: Int = checkNotNull(savedStateHandle[STUDENT_ID_ARG])

    private val _state = MutableStateFlow(
        BulkScheduleState(
            draft = BulkScheduleDraft(studentId = studentId)
        )
    )
    val state: StateFlow<BulkScheduleState> = _state.asStateFlow()

    private var activeUndoSession: BatchUndoSession? = null

    init {
        generatePreview()
        observeExistingEvents()
    }

    private fun observeExistingEvents() {
        viewModelScope.launch {
            lessonRepository.getLessonsForStudent(studentId).collect { lessons ->
                _state.update { it.copy(existingLessons = lessons) }
            }
        }
        viewModelScope.launch {
            homeworkRepository.getHomeworkForStudent(studentId).collect { homework ->
                _state.update { it.copy(existingHomework = homework) }
            }
        }
    }

    fun onEvent(event: BulkScheduleEvent) {
        when (event) {
            is BulkScheduleEvent.OnModeChanged -> updateDraft { copy(mode = event.mode) }
            is BulkScheduleEvent.OnDateToggled -> updateDraft {
                val newDates = if (selectedDates.contains(event.date)) {
                    selectedDates - event.date
                } else {
                    selectedDates + event.date
                }
                copy(selectedDates = newDates)
            }
            is BulkScheduleEvent.OnDayOfWeekToggled -> updateDraft {
                val newDays = if (selectedDaysOfWeek.contains(event.dayOfWeek)) {
                    selectedDaysOfWeek - event.dayOfWeek
                } else {
                    selectedDaysOfWeek + event.dayOfWeek
                }
                copy(selectedDaysOfWeek = newDays)
            }
            is BulkScheduleEvent.OnRoutineStartDateChanged -> updateDraft { copy(routineStartDate = event.date) }
            is BulkScheduleEvent.OnRoutineEndConditionChanged -> {
                val trimmed = _state.value.targetCountInput.trim()
                val targetCount = trimmed.toIntOrNull() ?: if (trimmed.isNotEmpty()) 100 else 4
                if (trimmed.isEmpty()) {
                    _state.update { it.copy(targetCountInput = "4") }
                }
                updateDraft {
                    val newCondition = if (event.isByEndDate) {
                        WeeklyRoutineEndCondition.ByEndDate(routineStartDate.plusMonths(1))
                    } else {
                        WeeklyRoutineEndCondition.ByTargetCount(targetCount)
                    }
                    copy(endCondition = newCondition)
                }
            }
            is BulkScheduleEvent.OnRoutineEndDateChanged -> updateDraft {
                copy(endCondition = WeeklyRoutineEndCondition.ByEndDate(event.date))
            }
            is BulkScheduleEvent.OnRoutineTargetCountChanged -> {
                val digitsOnly = event.count.filter { it.isDigit() }
                _state.update { it.copy(targetCountInput = digitsOnly) }
                val count = digitsOnly.toIntOrNull() ?: if (digitsOnly.isNotEmpty()) 100 else null
                if (count != null && count > 0) {
                    updateDraft { copy(endCondition = WeeklyRoutineEndCondition.ByTargetCount(count)) }
                } else {
                    updateDraft { copy(endCondition = WeeklyRoutineEndCondition.ByTargetCount(0)) }
                }
            }
            is BulkScheduleEvent.OnDefaultStartTimeChanged -> updateDraft { copy(defaultStartTime = event.time) }
            is BulkScheduleEvent.OnCustomDayTimeChanged -> updateDraft {
                val newCustomTimes = customDayTimes.toMutableMap()
                if (event.time == null) {
                    newCustomTimes.remove(event.date)
                } else {
                    newCustomTimes[event.date] = event.time
                }
                copy(customDayTimes = newCustomTimes)
            }
            is BulkScheduleEvent.OnUseCustomRateToggled -> updateDraft { copy(useCustomRate = event.useCustomRate) }
            is BulkScheduleEvent.OnCustomRateChanged -> {
                _state.update { it.copy(customRateInput = event.rate) }
                val parsedRate = event.rate.toDoubleOrNull()
                updateDraft { copy(customRate = parsedRate) }
            }
            is BulkScheduleEvent.GeneratePreview -> generatePreview()
            is BulkScheduleEvent.ConfirmSchedule -> confirmSchedule()
            is BulkScheduleEvent.DismissWarningDialog -> {
                _state.update { it.copy(showPastDateWarning = false) }
            }
            is BulkScheduleEvent.ProceedWithPastDates -> {
                _state.update { it.copy(showPastDateWarning = false) }
                createLessons()
            }
            is BulkScheduleEvent.UndoBatch -> undoBatch()
            is BulkScheduleEvent.SnackbarDismissed -> {
                _state.update { it.copy(snackbarMessage = null) }
                activeUndoSession = null // Clear undo session when snackbar is dismissed
            }
        }
    }

    private fun updateDraft(update: BulkScheduleDraft.() -> BulkScheduleDraft) {
        _state.update { it.copy(draft = it.draft.update()) }
        generatePreview()
    }

    private fun generatePreview() {
        val draft = _state.value.draft
        if ((draft.mode == BulkScheduleMode.CALENDAR_GRID && draft.selectedDates.isEmpty()) ||
            (draft.mode == BulkScheduleMode.WEEKLY_ROUTINE && draft.selectedDaysOfWeek.isEmpty())) {
            _state.update { 
                it.copy(
                    previewCandidates = null,
                    previewSkippedCount = 0,
                    hasPastLessonsInPreview = false,
                    isCapReached = false
                ) 
            }
            return
        }

        viewModelScope.launch {
            val candidates = calculateCandidatesUseCase(draft)
            val validCandidates = candidates.filter { !it.isConflict }
            val exceedsTarget = draft.mode == BulkScheduleMode.WEEKLY_ROUTINE &&
                draft.endCondition is WeeklyRoutineEndCondition.ByTargetCount &&
                draft.endCondition.targetCount > 30
            _state.update { 
                it.copy(
                    previewCandidates = candidates,
                    previewSkippedCount = candidates.size - validCandidates.size,
                    hasPastLessonsInPreview = validCandidates.any { candidate -> candidate.isPast },
                    isCapReached = candidates.size > 30 || exceedsTarget
                ) 
            }
        }
    }

    private fun confirmSchedule() {
        val currentState = _state.value
        val draft = currentState.draft
        val candidateCount = currentState.previewCandidates?.size ?: 0
        val validCandidatesCount = currentState.previewCandidates?.count { !it.isConflict } ?: 0

        if (candidateCount > 30 || currentState.isCapReached) {
            _state.update { it.copy(snackbarMessage = SnackbarState.Error(R.string.bulk_schedule_max_limit_error)) }
            return
        }

        if (validCandidatesCount == 0) {
            _state.update { it.copy(snackbarMessage = SnackbarState.Error(R.string.bulk_schedule_empty_selection_error)) }
            return
        }

        if (currentState.hasPastLessonsInPreview) {
            _state.update { it.copy(showPastDateWarning = true) }
        } else {
            createLessons()
        }
    }

    private fun createLessons() {
        val currentState = _state.value
        val draft = currentState.draft
        val candidates = currentState.previewCandidates ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = createLessonsUseCase(draft, candidates)
            
            result.onSuccess { bulkResult ->
                if (bulkResult.createdCount > 0) {
                    activeUndoSession = BatchUndoSession(
                        studentId = studentId,
                        lessonIds = bulkResult.createdLessonIds
                    )
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            snackbarMessage = SnackbarState.Success(
                                createdCount = bulkResult.createdCount,
                                skippedCount = bulkResult.skippedCount,
                                lessonIds = bulkResult.createdLessonIds,
                                isUndoable = true
                            )
                        )
                    }
                    resetDraft()
                } else {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            snackbarMessage = SnackbarState.Success(
                                createdCount = 0,
                                skippedCount = bulkResult.skippedCount,
                                lessonIds = emptyList(),
                                isUndoable = false
                            )
                        )
                    }
                }
            }.onFailure {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        snackbarMessage = SnackbarState.Error(R.string.student_profile_save_error) // Fallback error message
                    ) 
                }
            }
        }
    }

    private fun undoBatch() {
        val session = activeUndoSession ?: return
        viewModelScope.launch {
            val result = undoUseCase(session)
            result.onSuccess {
                activeUndoSession = null
                _state.update { 
                    it.copy(
                        snackbarMessage = SnackbarState.UndoSuccess
                    ) 
                }
                generatePreview() // Re-generate preview to clear conflicts that were just undone
            }
        }
    }

    private fun resetDraft() {
        _state.update {
            it.copy(
                draft = BulkScheduleDraft(studentId = studentId),
                targetCountInput = "4",
                customRateInput = "",
                previewCandidates = null,
                previewSkippedCount = 0,
                hasPastLessonsInPreview = false,
                isCapReached = false
            )
        }
    }
}
