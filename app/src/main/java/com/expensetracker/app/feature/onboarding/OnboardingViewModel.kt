package com.expensetracker.app.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    fun completeOnboarding(
        currency: String,
        monthStartDay: Int,
        dailyReminderEnabled: Boolean,
    ) {
        viewModelScope.launch {
            preferencesRepository.setCurrency(currency)
            preferencesRepository.setMonthStartDay(monthStartDay)
            preferencesRepository.setDailyReminderEnabled(dailyReminderEnabled)
            preferencesRepository.setHasCompletedOnboarding(true)
        }
    }
}
