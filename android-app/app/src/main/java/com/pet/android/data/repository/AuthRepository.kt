package com.pet.android.data.repository

import android.util.Log
import com.pet.android.data.api.AuthApi
import com.pet.android.data.model.LoginRequest
import com.pet.android.data.model.LoginResponse
import com.pet.android.data.preferences.UserPreferencesManager
import com.pet.android.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val userPreferencesManager: UserPreferencesManager
) {
    companion object {
        private const val TAG = "AuthRepository"
    }

    suspend fun login(username: String, password: String): Resource<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Attempting login for username: $username")

                // Check if account has changed and clear data if necessary
                val savedUsername = userPreferencesManager.username.first()
                if (savedUsername != null && savedUsername != username) {
                    Log.d(TAG, "Account changed from $savedUsername to $username, clearing old data")
                    userPreferencesManager.clearLoginData()
                }

                val response = authApi.login(LoginRequest(username, password))
                Log.d(TAG, "Login API response - success: ${response.success}, message: ${response.message}")
                Log.d(TAG, "Raw response data: ${response.data}")

                if (response.success && response.data != null) {
                    val loginData = response.data
                    Log.d(TAG, "Login successful - userId: ${loginData.userId}, roleId: ${loginData.roleId}, username: ${loginData.username}, role: ${loginData.role}")

                    // Get the appropriate ID based on role
                    val effectiveId = loginData.id
                    Log.d(TAG, "Effective ID for ${loginData.role}: $effectiveId")

                    // Check if id is null and warn
                    if (effectiveId == null) {
                        Log.w(TAG, "WARNING: Could not determine user ID! Using username as fallback")
                    }

                    // Save login data to DataStore
                    // Use id if available, otherwise fallback to username
                    val userIdToSave = effectiveId ?: loginData.username
                    Log.d(TAG, "Saving userId: $userIdToSave")

                    userPreferencesManager.saveLoginData(
                        username = loginData.username,
                        role = loginData.role,
                        userId = userIdToSave,
                        roleName = loginData.roleName
                    )

                    Log.d(TAG, "Login data saved to preferences")
                    Resource.Success(loginData)
                } else {
                    Log.e(TAG, "Login failed: ${response.message}")
                    Resource.Error(response.message ?: "登入失敗")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login error: ${e.message}", e)
                Resource.Error(e.message ?: "網路錯誤")
            }
        }
    }
}
