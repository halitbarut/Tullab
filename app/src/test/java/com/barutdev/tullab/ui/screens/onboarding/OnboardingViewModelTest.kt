package com.barutdev.tullab.ui.screens.onboarding

import com.barutdev.tullab.MainDispatcherRule
import com.barutdev.tullab.domain.usecase.SetOnboardingCompletedUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setup() {
        setOnboardingCompletedUseCase = mockk()
        coEvery { setOnboardingCompletedUseCase(any()) } returns Unit
        
        viewModel = OnboardingViewModel(setOnboardingCompletedUseCase)
    }

    @Test
    fun `initial consent state is false`() {
        assertEquals(false, viewModel.consentChecked.value)
    }

    @Test
    fun `onConsentCheckedChange updates consent state`() {
        viewModel.onConsentCheckedChange(true)
        assertEquals(true, viewModel.consentChecked.value)

        viewModel.onConsentCheckedChange(false)
        assertEquals(false, viewModel.consentChecked.value)
    }

    @Test
    fun `completeOnboarding sets onboarding completed to true and invokes callback`() = runTest {
        var callbackInvoked = false

        viewModel.completeOnboarding {
            callbackInvoked = true
        }
        
        advanceUntilIdle()

        coVerify(exactly = 1) { setOnboardingCompletedUseCase(true) }
        assertEquals(true, callbackInvoked)
    }
}
