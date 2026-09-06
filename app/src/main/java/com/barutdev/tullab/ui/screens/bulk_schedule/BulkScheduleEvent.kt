package com.barutdev.tullab.ui.screens.bulk_schedule

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import com.barutdev.tullab.domain.model.BulkScheduleMode

sealed interface BulkScheduleEvent {
    data class OnModeChanged(val mode: BulkScheduleMode) : BulkScheduleEvent
    
    // Calendar Grid specific
    data class OnDateToggled(val date: LocalDate) : BulkScheduleEvent
    
    // Weekly Routine specific
    data class OnDayOfWeekToggled(val dayOfWeek: DayOfWeek) : BulkScheduleEvent
    data class OnRoutineStartDateChanged(val date: LocalDate) : BulkScheduleEvent
    data class OnRoutineEndConditionChanged(val isByEndDate: Boolean) : BulkScheduleEvent
    data class OnRoutineEndDateChanged(val date: LocalDate) : BulkScheduleEvent
    data class OnRoutineTargetCountChanged(val count: String) : BulkScheduleEvent
    
    // Shared parameters
    data class OnDefaultStartTimeChanged(val time: LocalTime) : BulkScheduleEvent
    data class OnCustomDayTimeChanged(val date: LocalDate, val time: LocalTime?) : BulkScheduleEvent
    
    // Rate configuration
    data class OnUseCustomRateToggled(val useCustomRate: Boolean) : BulkScheduleEvent
    data class OnCustomRateChanged(val rate: String) : BulkScheduleEvent
    
    // Actions
    object GeneratePreview : BulkScheduleEvent
    object ConfirmSchedule : BulkScheduleEvent
    object DismissWarningDialog : BulkScheduleEvent
    object ProceedWithPastDates : BulkScheduleEvent
    object UndoBatch : BulkScheduleEvent
    object SnackbarDismissed : BulkScheduleEvent
}
