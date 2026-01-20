package com.pet.shared.auth

import com.pet.shared.network.LoginResponse
import com.pet.shared.util.Resource

class AuthRepository(private val service: AuthService) {
    suspend fun login(username: String, password: String): Resource<LoginResponse> = try {
        val resp = service.login(username, password)
        if (resp.success && resp.data != null) {
            Resource.Success(resp.data)
        } else {
            Resource.Error(resp.message ?: "Login failed")
        }
    } catch (t: Throwable) {
        Resource.Error(t.message ?: "Network error", t)
    }
}
