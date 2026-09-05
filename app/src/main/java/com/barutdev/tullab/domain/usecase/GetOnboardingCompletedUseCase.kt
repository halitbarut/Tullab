package com.barutdev.tullab.domain.usecase

import com.barutdev.tullab.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class GetOnboardingCompletedUseCase @Inject constructor(
    private val repo: UserPreferencesRepository
) {
    suspend operator fun invoke(): Boolean = repo.isOnboardingCompleted()
}
