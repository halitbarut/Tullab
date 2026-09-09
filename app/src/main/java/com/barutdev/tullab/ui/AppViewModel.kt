package com.barutdev.tullab.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barutdev.tullab.domain.model.UserPreferences
import com.barutdev.tullab.domain.repository.UserPreferencesRepository
import com.barutdev.tullab.domain.usecase.InitializeSmartDefaultsUseCase
import com.barutdev.tullab.util.SmartLocaleDefaults
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AppViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val initializeSmartDefaults: InitializeSmartDefaultsUseCase
) : ViewModel() {

    private val smartDefaults = SmartLocaleDefaults.resolveSmartDefaultsFromDevice()

    private val defaultPreferences = UserPreferences(
        isDarkMode = false,
        languageCode = smartDefaults.languageCode,
        currencyCode = smartDefaults.currencyCode,
        defaultHourlyRate = 0.0,
        lessonRemindersEnabled = false,
        logReminderEnabled = false,
        lessonReminderHour = 9,
        lessonReminderMinute = 0,
        logReminderHour = 20,
        logReminderMinute = 0,
        hapticFeedbackEnabled = true
    )

    val userPreferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = defaultPreferences
        )

    init {
        viewModelScope.launch {
            initializeSmartDefaults()
        }
    }
}
