package com.pet.android.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pet.android.data.preferences.EnvironmentManager
import com.pet.android.data.preferences.EnvironmentManager.Environment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val environmentManager: EnvironmentManager
) : ViewModel() {

    // Expose current environment as a StateFlow for UI to observe
    val currentEnvironment: StateFlow<Environment> = environmentManager.currentEnvironment
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Environment.DEVELOPMENT // Default fallback
        )

    fun setEnvironment(environment: Environment) {
        viewModelScope.launch {
            environmentManager.setEnvironment(environment)
        }
    }

    fun logout() {
        // TODO: Clear user session/token here when TokenManager is available
        // For now, we just rely on the UI to navigate back to LoginActivity which
        // usually clears its stack or handles re-login.
        // If SharedPreferences has other keys, clear them here.
    }
}
