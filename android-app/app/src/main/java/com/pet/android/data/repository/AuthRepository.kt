package com.pet.android.data.repository

import com.pet.android.data.api.AuthApi
import com.pet.android.data.model.LoginRequest
import com.pet.android.data.model.LoginResponse
import com.pet.android.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi
) {
    suspend fun login(username: String, password: String): Resource<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApi.login(LoginRequest(username, password))
                if (response.success && response.data != null) {
                    Resource.Success(response.data)
                } else {
                    Resource.Error(response.message ?: "登入失敗")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "網路錯誤")
            }
        }
    }
}
