package com.pet.shared.auth

import com.pet.shared.network.LoginResponse
import com.pet.shared.network.createHttpClient
import com.pet.shared.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Minimal SDK surface callable from Swift.
 */
class AuthSdk(private val baseUrl: String) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val repository: AuthRepository by lazy {
        val client = createHttpClient(baseUrl)
        val service = AuthService(client, baseUrl)
        AuthRepository(service)
    }

    data class LoginResult(
        val success: Boolean,
        val token: String? = null,
        val message: String? = null
    )

    /**
     * Swift-friendly: use callback instead of suspend.
     */
    fun login(
        username: String,
        password: String,
        completion: (LoginResult) -> Unit
    ) {
        scope.launch {
            when (val res = repository.login(username, password)) {
                is Resource.Success<LoginResponse> -> {
                    completion(LoginResult(success = true, token = res.data.token, message = null))
                }
                is Resource.Error -> {
                    completion(LoginResult(success = false, token = null, message = res.message))
                }
                is Resource.Loading -> {
                    // not emitted here
                }
            }
        }
    }
}
