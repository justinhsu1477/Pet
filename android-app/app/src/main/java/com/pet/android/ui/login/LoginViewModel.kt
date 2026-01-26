package com.pet.android.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pet.android.data.model.JwtAuthenticationResponse
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

    // 保留舊版登入狀態（向後兼容）
    private val _loginState = MutableLiveData<Resource<LoginResponse>>()
    val loginState: LiveData<Resource<LoginResponse>> = _loginState

    // JWT 登入狀態
    private val _jwtLoginState = MutableLiveData<Resource<JwtAuthenticationResponse>>()
    val jwtLoginState: LiveData<Resource<JwtAuthenticationResponse>> = _jwtLoginState

    /**
     * 舊版登入方法（保留向後兼容）
     *
     * @deprecated 此方法已棄用，請使用 jwtLogin()
     * 此方法將在未來版本中移除
     */
    @Deprecated(
        message = "使用 jwtLogin() 代替，此方法將在未來版本中移除",
        replaceWith = ReplaceWith("jwtLogin(username, password)"),
        level = DeprecationLevel.WARNING
    )
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

    /**
     * JWT 登入方法
     * 使用 JWT 認證機制進行登入
     */
    fun jwtLogin(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _jwtLoginState.value = Resource.Error("請輸入帳號和密碼")
            return
        }

        viewModelScope.launch {
            _jwtLoginState.value = Resource.Loading
            _jwtLoginState.value = authRepository.jwtLogin(username, password)
        }
    }

    /**
     * 登出
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    /**
     * 檢查是否已登入
     */
    fun isLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }

    /**
     * 自動登入時，若缺少 userId/role，嘗試從 Access Token 補齊
     */
    suspend fun hydrateUserFromTokenIfNeeded() {
        authRepository.hydrateUserFromTokenIfNeeded()
    }

    suspend fun getUserRole(): String {
        return userPreferencesManager.userRole.first() ?: "還未有權限"
    }

    suspend fun getUserRoleEnum(): UserRole {
        return userPreferencesManager.userRoleEnum.first()
    }
}
