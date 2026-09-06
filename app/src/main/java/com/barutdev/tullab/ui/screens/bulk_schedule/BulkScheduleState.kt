package com.barutdev.tullab.ui.screens.bulk_schedule

import com.barutdev.tullab.domain.model.BulkLessonCandidate
import com.barutdev.tullab.domain.model.BulkScheduleDraft
import com.barutdev.tullab.domain.model.Homework
import com.barutdev.tullab.domain.model.Lesson

data class BulkScheduleState(
    val draft: BulkScheduleDraft = BulkScheduleDraft(studentId = -1),
    val targetCountInput: String = "4",
    val customRateInput: String = "",
    val isLoading: Boolean = false,
    val previewCandidates: List<BulkLessonCandidate>? = null,
    val previewSkippedCount: Int = 0,
    val hasPastLessonsInPreview: Boolean = false,
    val isCapReached: Boolean = false,
    val showPastDateWarning: Boolean = false,
    val snackbarMessage: SnackbarState? = null,
    val existingLessons: List<Lesson> = emptyList(),
    val existingHomework: List<Homework> = emptyList()
)

sealed interface SnackbarState {
    data class Success(val createdCount: Int, val skippedCount: Int, val lessonIds: List<Int>, val isUndoable: Boolean = true) : SnackbarState
    data class Error(val messageResId: Int) : SnackbarState
    object UndoSuccess : SnackbarState
}
