package com.pet.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.pet.dto.RefreshTokenRequest
import com.pet.dto.response.ApiResponse
import com.pet.service.AuthenticationService
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * AuthController 登出功能測試
 * 測試 POST /api/auth/jwt/logout endpoint
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("登出功能測試")
class AuthControllerLogoutTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var authenticationService: AuthenticationService

    private val testRefreshToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token"

    @BeforeEach
    fun setUp() {
        reset(authenticationService)
    }

    @Test
    @DisplayName("應該成功登出 - 使用 Request Body 中的 RefreshToken")
    fun `should logout successfully with refresh token in request body`() {
        // Arrange
        val request = RefreshTokenRequest().apply {
            refreshToken = testRefreshToken
        }

        doNothing().`when`(authenticationService).logout(testRefreshToken)

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/jwt/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isEmpty)
            .andExpect(header().exists("Set-Cookie"))

        // Verify
        verify(authenticationService, times(1)).logout(testRefreshToken)
    }

    @Test
    @DisplayName("應該成功登出 - 使用 Cookie 中的 RefreshToken")
    fun `should logout successfully with refresh token in cookie`() {
        // Arrange
        val cookie = Cookie("refreshToken", testRefreshToken)
        doNothing().`when`(authenticationService).logout(testRefreshToken)

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/jwt/logout")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isEmpty)
            .andExpect(header().exists("Set-Cookie"))

        // Verify
        verify(authenticationService, times(1)).logout(testRefreshToken)
    }

    @Test
    @DisplayName("應該成功登出 - Cookie 優先於 Request Body")
    fun `should logout with cookie token when both cookie and body exist`() {
        // Arrange
        val cookieToken = "cookie-token"
        val bodyToken = "body-token"
        val cookie = Cookie("refreshToken", cookieToken)
        val request = RefreshTokenRequest().apply {
            refreshToken = bodyToken
        }

        doNothing().`when`(authenticationService).logout(cookieToken)

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/jwt/logout")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))

        // Verify - 應該使用 Cookie 中的 token
        verify(authenticationService, times(1)).logout(cookieToken)
        verify(authenticationService, never()).logout(bodyToken)
    }

    @Test
    @DisplayName("應該成功登出 - 沒有 RefreshToken 也不會報錯")
    fun `should logout successfully even without refresh token`() {
        // Arrange
        doNothing().`when`(authenticationService).logout(null)

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/jwt/logout")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))

        // Verify
        verify(authenticationService, times(1)).logout(null)
    }

    @Test
    @DisplayName("應該清除 RefreshToken Cookie")
    fun `should clear refresh token cookie after logout`() {
        // Arrange
        val cookie = Cookie("refreshToken", testRefreshToken)
        doNothing().`when`(authenticationService).logout(testRefreshToken)

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/jwt/logout")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(header().exists("Set-Cookie"))
            .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refreshToken=")))
            .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")))
    }

    @Test
    @DisplayName("當 AuthenticationService 拋出異常時應該返回錯誤")
    fun `should return error when authentication service throws exception`() {
        // Arrange
        val request = RefreshTokenRequest().apply {
            refreshToken = testRefreshToken
        }

        doThrow(RuntimeException("Token invalid"))
            .`when`(authenticationService).logout(testRefreshToken)

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/jwt/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().is5xxServerError)

        // Verify
        verify(authenticationService, times(1)).logout(testRefreshToken)
    }

    @Test
    @DisplayName("應該接受空的 Request Body")
    fun `should accept empty request body`() {
        // Arrange
        doNothing().`when`(authenticationService).logout(null)

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/jwt/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))

        // Verify
        verify(authenticationService, times(1)).logout(null)
    }

    @Test
    @DisplayName("應該正確處理空字串 RefreshToken")
    fun `should handle empty string refresh token`() {
        // Arrange
        val request = RefreshTokenRequest().apply {
            refreshToken = ""
        }

        doNothing().`when`(authenticationService).logout(null)

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/jwt/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)

        // Verify - 空字串直接傳入 logout
        verify(authenticationService, times(1)).logout("")
    }
}
