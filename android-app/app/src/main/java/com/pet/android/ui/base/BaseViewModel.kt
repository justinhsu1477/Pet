package com.pet.android.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pet.android.data.repository.AuthRepository
import kotlinx.coroutines.launch

/**
 * Base ViewModel
 * 提供通用功能，如登出等
 */
abstract class BaseViewModel(
    private val authRepository: AuthRepository? = null
) : ViewModel() {

    /**
     * 執行登出
     * 子類可以調用此方法來登出用戶
     */
    fun logout(onComplete: (() -> Unit)? = null) {
        authRepository?.let { repo ->
            viewModelScope.launch {
                repo.logout()
                onComplete?.invoke()
            }
        }
    }

    /**
     * 檢查是否已登入
     */
    fun isLoggedIn(): Boolean {
        return authRepository?.isLoggedIn() ?: false
    }
}
