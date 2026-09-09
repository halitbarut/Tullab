package com.barutdev.tullab.ui.screens.settings

import com.barutdev.tullab.data.backup.DataBackupManager
import com.barutdev.tullab.domain.model.UserPreferences
import com.barutdev.tullab.domain.repository.LessonRepository
import com.barutdev.tullab.domain.repository.StudentRepository
import com.barutdev.tullab.domain.repository.UserPreferencesRepository
import com.barutdev.tullab.domain.usecase.notification.RescheduleAllNotificationAlarmsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var viewModel: SettingsViewModel
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var rescheduleAllNotificationAlarmsUseCase: RescheduleAllNotificationAlarmsUseCase
    private lateinit var dataBackupManager: DataBackupManager
    private lateinit var lessonRepository: LessonRepository
    private lateinit var studentRepository: StudentRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        userPreferencesRepository = mockk(relaxed = true)
        rescheduleAllNotificationAlarmsUseCase = mockk(relaxed = true)
        dataBackupManager = mockk(relaxed = true)
        lessonRepository = mockk(relaxed = true)
        studentRepository = mockk(relaxed = true)

        coEvery { userPreferencesRepository.userPreferences } returns flowOf(
            UserPreferences(
                isDarkMode = false,
                languageCode = "en",
                currencyCode = "USD",
                defaultHourlyRate = 0.0,
                lessonRemindersEnabled = false,
                logReminderEnabled = false,
                lessonReminderHour = 0,
                lessonReminderMinute = 0,
                logReminderHour = 0,
                logReminderMinute = 0
            )
        )

        viewModel = SettingsViewModel(
            userPreferencesRepository,
            rescheduleAllNotificationAlarmsUseCase,
            dataBackupManager,
            lessonRepository,
            studentRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `currency list is sanitized and does not contain test codes like XXX`() = runTest {
        val currencies = viewModel.filteredCurrencies.value
        
        assertTrue(currencies.isNotEmpty())
        assertFalse(currencies.any { it.code == "XXX" })
        assertFalse(currencies.any { it.code == "XTS" })
        assertFalse(currencies.any { it.code.startsWith("X") && it.code !in setOf("XAF", "XCD", "XOF", "XPF") })
    }

    @Test
    fun `pinned currencies appear at the top in specific order`() = runTest {
        val currencies = viewModel.filteredCurrencies.value
        
        // Pinned codes should be TRY, USD, EUR, GBP, CHF
        // Only if they exist in the available locales (they should)
        val expectedPinned = listOf("TRY", "USD", "EUR", "GBP", "CHF")
        val actualTopCodes = currencies.take(expectedPinned.size).map { it.code }
        
        assertEquals(expectedPinned, actualTopCodes)
    }

    @Test
    fun `search query filters currency list by code and display name`() = runTest {
        val collectJob = backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.filteredCurrencies.collect {}
        }

        // Search by code
        viewModel.updateCurrencySearchQuery("JPY")
        runCurrent()
        var filtered = viewModel.filteredCurrencies.value
        assertTrue(filtered.any { it.code == "JPY" })
        assertFalse(filtered.any { it.code == "USD" })

        // Search by name
        viewModel.updateCurrencySearchQuery("Lira")
        runCurrent()
        filtered = viewModel.filteredCurrencies.value
        assertTrue(filtered.any { it.displayName.contains("Lira", ignoreCase = true) })
        assertTrue(filtered.any { it.code == "TRY" }) // Turkish Lira
        
        collectJob.cancel()
    }

    @Test
    fun `updateHapticFeedbackEnabled delegates to repository with given value`() = runTest {
        viewModel.updateHapticFeedbackEnabled(false)
        runCurrent()
        coVerify { userPreferencesRepository.updateHapticFeedbackEnabled(false) }

        viewModel.updateHapticFeedbackEnabled(true)
        runCurrent()
        coVerify { userPreferencesRepository.updateHapticFeedbackEnabled(true) }
    }
}
