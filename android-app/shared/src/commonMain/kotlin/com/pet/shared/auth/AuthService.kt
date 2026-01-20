package com.pet.shared.auth

import com.pet.shared.network.ApiResponse
import com.pet.shared.network.LoginRequest
import com.pet.shared.network.LoginResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthService(
    private val httpClient: HttpClient,
    private val baseUrl: String
) {
    private fun endpoint(path: String) = if (baseUrl.endsWith("/")) baseUrl.dropLast(1) + path else baseUrl + path

    suspend fun login(username: String, password: String): ApiResponse<LoginResponse> {
        val request = LoginRequest(username = username, password = password)
        return httpClient.post(endpoint("/api/auth/login")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
