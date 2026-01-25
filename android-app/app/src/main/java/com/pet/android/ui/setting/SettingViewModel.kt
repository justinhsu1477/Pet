package com.pet.android.ui.setting

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pet.android.data.preferences.EnvironmentManager
import com.pet.android.data.preferences.EnvironmentManager.Environment
import com.pet.android.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val environmentManager: EnvironmentManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val TAG = "SettingViewModel"
    }

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

    /**
     * 登出
     * 呼叫 AuthRepository 清除本地 Token 並通知後端
     */
    fun logout() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Performing logout")
                authRepository.logout()
                Log.d(TAG, "Logout completed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error during logout", e)
                // 即使出錯也繼續，因為 AuthRepository.logout() 已經保證清除本地數據
            }
        }
    }
}
