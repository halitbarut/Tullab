package com.barutdev.tullab.ui.screens.reports

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barutdev.tullab.domain.model.reports.ReportRange
import com.barutdev.tullab.domain.usecase.reports.GetMonthlyEarningsUseCase
import com.barutdev.tullab.domain.usecase.reports.GetReportSummaryUseCase
import com.barutdev.tullab.domain.usecase.reports.GetTopStudentsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.CancellationException
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val getReportSummaryUseCase: GetReportSummaryUseCase,
    private val getMonthlyEarningsUseCase: GetMonthlyEarningsUseCase,
    private val getTopStudentsUseCase: GetTopStudentsUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState.initial())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        val initialRange = restoreSelectedRange()
        _uiState.value = ReportsUiState.initial(initialRange)
        saveSelectedRange(initialRange)
        load(initialRange, persistSelection = false)
    }

    fun onRangeSelected(range: ReportRange) {
        if (range == _uiState.value.selectedRange && !_uiState.value.isLoading) {
            return
        }
        load(range, persistSelection = true)
    }

    fun refresh() {
        load(_uiState.value.selectedRange, persistSelection = false)
    }

    private fun load(range: ReportRange, persistSelection: Boolean) {
        if (persistSelection) {
            saveSelectedRange(range)
        }
        val rangeToLoad = range
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    selectedRange = rangeToLoad,
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                val summaryDeferred = async { getReportSummaryUseCase(rangeToLoad) }
                val monthlyDeferred = async { getMonthlyEarningsUseCase() }
                val topStudentsDeferred = async { getTopStudentsUseCase(rangeToLoad) }

                val summary = summaryDeferred.await()
                val monthly = monthlyDeferred.await()
                val topStudents = topStudentsDeferred.await()

                _uiState.update { current ->
                    current.copy(
                        selectedRange = rangeToLoad,
                        summary = summary,
                        monthlyEarnings = monthly,
                        topStudents = topStudents,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Throwable) {
                Log.e(TAG, "Failed to load reports for range=$rangeToLoad", error)
                _uiState.update { current ->
                    current.copy(
                        selectedRange = rangeToLoad,
                        isLoading = false,
                        errorMessage = ReportsUiMessage.loadFailed()
                    )
                }
            }
        }
    }

    private fun restoreSelectedRange(): ReportRange {
        val index = savedStateHandle.get<Int>(SELECTED_RANGE_KEY)
        return index?.let { ReportRange.presets.getOrNull(it) } ?: ReportRange.default
    }

    private fun saveSelectedRange(range: ReportRange) {
        val index = ReportRange.presets.indexOf(range)
        if (index >= 0) {
            savedStateHandle[SELECTED_RANGE_KEY] = index
        }
    }

    companion object {
        internal const val SELECTED_RANGE_KEY = "reports_selected_range_index"
        private const val TAG = "ReportsViewModel"
    }
}
