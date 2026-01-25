package com.pet.service

import com.pet.domain.UserRole
import com.pet.domain.Users
import com.pet.security.RefreshTokenService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.util.*

/**
 * AuthenticationService 登出功能測試
 * 測試登出業務邏輯
 */
@DisplayName("AuthenticationService 登出功能測試")
class AuthenticationServiceLogoutTest {

    private lateinit var authenticationService: AuthenticationService

    @Mock
    private lateinit var refreshTokenService: RefreshTokenService

    private val testRefreshToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token"
    private val testUserId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        authenticationService = AuthenticationService(
            mock(), // passwordEncoder
            mock(), // authenticationManager
            mock(), // userRepository
            mock(), // jwtService
            refreshTokenService,
            mock()  // jwtProperties
        )
    }

    @Test
    @DisplayName("應該成功撤銷 RefreshToken")
    fun `should revoke refresh token successfully`() {
        // Arrange
        doNothing().`when`(refreshTokenService).revokeRefreshToken(testRefreshToken)

        // Act
        authenticationService.logout(testRefreshToken)

        // Assert
        verify(refreshTokenService, times(1)).revokeRefreshToken(testRefreshToken)
    }

    @Test
    @DisplayName("當 RefreshToken 為 null 時不應該呼叫 revokeRefreshToken")
    fun `should not revoke token when refresh token is null`() {
        // Act
        authenticationService.logout(null)

        // Assert
        verify(refreshTokenService, never()).revokeRefreshToken(any())
    }

    @Test
    @DisplayName("當 RefreshToken 為空字串時不應該呼叫 revokeRefreshToken")
    fun `should not revoke token when refresh token is empty`() {
        // Act
        authenticationService.logout("")

        // Assert
        verify(refreshTokenService, never()).revokeRefreshToken(any())
    }

    @Test
    @DisplayName("當 RefreshTokenService 拋出異常時應該正常傳播")
    fun `should propagate exception from refresh token service`() {
        // Arrange
        val expectedException = RuntimeException("Token not found")
        doThrow(expectedException)
            .`when`(refreshTokenService).revokeRefreshToken(testRefreshToken)

        // Act & Assert
        assertThrows(RuntimeException::class.java) {
            authenticationService.logout(testRefreshToken)
        }

        verify(refreshTokenService, times(1)).revokeRefreshToken(testRefreshToken)
    }

    @Test
    @DisplayName("應該成功登出所有設備")
    fun `should logout all devices successfully`() {
        // Arrange
        doNothing().`when`(refreshTokenService).revokeAllUserTokens(testUserId)

        // Act
        authenticationService.logoutAllDevices(testUserId)

        // Assert
        verify(refreshTokenService, times(1)).revokeAllUserTokens(testUserId)
    }

    @Test
    @DisplayName("登出所有設備時應該撤銷該使用者的所有 Token")
    fun `should revoke all tokens for user when logging out all devices`() {
        // Arrange
        val userId = UUID.randomUUID()
        doNothing().`when`(refreshTokenService).revokeAllUserTokens(userId)

        // Act
        authenticationService.logoutAllDevices(userId)

        // Assert
        verify(refreshTokenService, times(1)).revokeAllUserTokens(userId)
        verifyNoMoreInteractions(refreshTokenService)
    }

    @Test
    @DisplayName("多次登出應該能夠正常處理")
    fun `should handle multiple logout calls`() {
        // Arrange
        doNothing().`when`(refreshTokenService).revokeRefreshToken(testRefreshToken)

        // Act
        authenticationService.logout(testRefreshToken)
        authenticationService.logout(testRefreshToken)

        // Assert
        verify(refreshTokenService, times(2)).revokeRefreshToken(testRefreshToken)
    }

    @Test
    @DisplayName("使用不同的 RefreshToken 應該分別撤銷")
    fun `should revoke different refresh tokens separately`() {
        // Arrange
        val token1 = "token1"
        val token2 = "token2"
        doNothing().`when`(refreshTokenService).revokeRefreshToken(anyString())

        // Act
        authenticationService.logout(token1)
        authenticationService.logout(token2)

        // Assert
        verify(refreshTokenService, times(1)).revokeRefreshToken(token1)
        verify(refreshTokenService, times(1)).revokeRefreshToken(token2)
    }

    @Test
    @DisplayName("登出不應該影響其他使用者的 Session")
    fun `logout should not affect other users sessions`() {
        // Arrange
        val token = "user-specific-token"
        doNothing().`when`(refreshTokenService).revokeRefreshToken(token)

        // Act
        authenticationService.logout(token)

        // Assert
        // 只應該撤銷指定的 token，不影響其他
        verify(refreshTokenService, times(1)).revokeRefreshToken(token)
        verify(refreshTokenService, never()).revokeAllUserTokens(any())
    }

    @Test
    @DisplayName("應該能處理特殊字元的 RefreshToken")
    fun `should handle refresh token with special characters`() {
        // Arrange
        val specialToken = "token.with-special_characters+/="
        doNothing().`when`(refreshTokenService).revokeRefreshToken(specialToken)

        // Act
        authenticationService.logout(specialToken)

        // Assert
        verify(refreshTokenService, times(1)).revokeRefreshToken(specialToken)
    }

    @Test
    @DisplayName("應該能處理超長的 RefreshToken")
    fun `should handle very long refresh token`() {
        // Arrange
        val longToken = "a".repeat(1000)
        doNothing().`when`(refreshTokenService).revokeRefreshToken(longToken)

        // Act
        authenticationService.logout(longToken)

        // Assert
        verify(refreshTokenService, times(1)).revokeRefreshToken(longToken)
    }
}
