package com.barutdev.tullab.domain.usecase

import com.barutdev.tullab.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class SetOnboardingCompletedUseCase @Inject constructor(
    private val repo: UserPreferencesRepository
) {
    suspend operator fun invoke(completed: Boolean = true) {
        repo.setOnboardingCompleted(completed)
    }
}
