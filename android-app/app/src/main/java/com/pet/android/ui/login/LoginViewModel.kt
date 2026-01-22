package com.pet.android.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pet.android.data.model.LoginResponse
import com.pet.android.data.model.UserRole
import com.pet.android.data.preferences.UserPreferencesManager
import com.pet.android.data.repository.AuthRepository
import com.pet.android.util.Resource
import kotlinx.coroutines.flow.Flow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    val savedUsername: Flow<String?> = userPreferencesManager.username

    private val _loginState = MutableLiveData<Resource<LoginResponse>>()
    val loginState: LiveData<Resource<LoginResponse>> = _loginState

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _loginState.value = Resource.Error("請輸入帳號和密碼")
            return
        }

        viewModelScope.launch {
            _loginState.value = Resource.Loading
            _loginState.value = authRepository.login(username, password)
        }
    }

    suspend fun getUserRole(): String {
        return userPreferencesManager.userRole.first() ?: "還未有權限"
    }

    suspend fun getUserRoleEnum(): UserRole {
        return userPreferencesManager.userRoleEnum.first()
    }
}
