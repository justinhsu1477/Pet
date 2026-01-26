package com.pet.service;

import com.pet.domain.RefreshToken;
import com.pet.domain.UserRole;
import com.pet.domain.Users;
import com.pet.dto.JwtAuthenticationResponse;
import com.pet.dto.LoginRequestDto;
import com.pet.dto.RefreshTokenRequest;
import com.pet.exception.BusinessException;
import com.pet.repository.UserRepository;
import com.pet.security.JwtProperties;
import com.pet.security.JwtService;
import com.pet.security.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService 測試")
class AuthenticationServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AuthenticationService authenticationService;

    private Users testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        testUser = new Users();
        testUser.setId(testUserId);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setPhone("0912345678");
        testUser.setRole(UserRole.ADMIN);
    }

    @Nested
    @DisplayName("登入測試")
    class LoginTests {

        @Test
        @DisplayName("應該成功登入並回傳 JWT")
        void shouldLoginSuccessfully() {
            // given
            LoginRequestDto loginRequest = new LoginRequestDto("testuser", "password123");

            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willReturn(new UsernamePasswordAuthenticationToken("testuser", "password123"));
            given(userRepository.findByUsername("testuser")).willReturn(Optional.of(testUser));
            given(jwtService.generateAccessToken(testUser)).willReturn("access-token");
            given(jwtService.generateRefreshToken(testUser)).willReturn("refresh-token");
            given(jwtProperties.getAccessTokenExpiration()).willReturn(900000L);
            given(httpServletRequest.getHeader("X-Device-Type")).willReturn("WEB");
            given(httpServletRequest.getHeader("User-Agent")).willReturn("Mozilla/5.0");
            given(httpServletRequest.getHeader("X-Forwarded-For")).willReturn(null);
            given(httpServletRequest.getHeader("X-Real-IP")).willReturn(null);
            given(httpServletRequest.getRemoteAddr()).willReturn("127.0.0.1");

            // when
            JwtAuthenticationResponse response = authenticationService.login(loginRequest, httpServletRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("access-token");
            assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
            assertThat(response.getUsername()).isEqualTo("testuser");
            verify(refreshTokenService).createRefreshToken(anyString(), any(Users.class), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("當用戶不存在時應該拋出例外")
        void shouldThrowExceptionWhenUserNotFound() {
            // given
            LoginRequestDto loginRequest = new LoginRequestDto("unknownuser", "password123");

            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willReturn(new UsernamePasswordAuthenticationToken("unknownuser", "password123"));
            given(userRepository.findByUsername("unknownuser")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authenticationService.login(loginRequest, httpServletRequest))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("刷新 Token 測試")
    class RefreshTokenTests {

        @Test
        @DisplayName("應該成功刷新 Access Token")
        void shouldRefreshTokenSuccessfully() {
            // given
            RefreshTokenRequest request = new RefreshTokenRequest();
            request.setRefreshToken("valid-refresh-token");

            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setToken("valid-refresh-token");
            refreshToken.setUser(testUser);

            given(refreshTokenService.validateRefreshToken("valid-refresh-token")).willReturn(refreshToken);
            given(jwtService.validateToken("valid-refresh-token")).willReturn(true);
            given(jwtService.isRefreshToken("valid-refresh-token")).willReturn(true);
            given(jwtService.generateAccessToken(testUser)).willReturn("new-access-token");
            given(jwtProperties.getAccessTokenExpiration()).willReturn(900000L);

            // when
            JwtAuthenticationResponse response = authenticationService.refreshToken(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("new-access-token");
            assertThat(response.getRefreshToken()).isEqualTo("valid-refresh-token");
        }

        @Test
        @DisplayName("當 Token 格式無效時應該拋出例外")
        void shouldThrowExceptionWhenTokenInvalid() {
            // given
            RefreshTokenRequest request = new RefreshTokenRequest();
            request.setRefreshToken("invalid-token");

            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setToken("invalid-token");
            refreshToken.setUser(testUser);

            given(refreshTokenService.validateRefreshToken("invalid-token")).willReturn(refreshToken);
            given(jwtService.validateToken("invalid-token")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authenticationService.refreshToken(request))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("當不是 Refresh Token 時應該拋出例外")
        void shouldThrowExceptionWhenNotRefreshToken() {
            // given
            RefreshTokenRequest request = new RefreshTokenRequest();
            request.setRefreshToken("access-token-not-refresh");

            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setToken("access-token-not-refresh");
            refreshToken.setUser(testUser);

            given(refreshTokenService.validateRefreshToken("access-token-not-refresh")).willReturn(refreshToken);
            given(jwtService.validateToken("access-token-not-refresh")).willReturn(true);
            given(jwtService.isRefreshToken("access-token-not-refresh")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authenticationService.refreshToken(request))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("登出測試")
    class LogoutTests {

        @Test
        @DisplayName("應該成功登出")
        void shouldLogoutSuccessfully() {
            // when
            authenticationService.logout("valid-refresh-token");

            // then
            verify(refreshTokenService).revokeRefreshToken("valid-refresh-token");
        }

        @Test
        @DisplayName("當 Token 為空時不應該呼叫撤銷方法")
        void shouldNotRevokeWhenTokenIsEmpty() {
            // when
            authenticationService.logout("");

            // then
            verify(refreshTokenService, never()).revokeRefreshToken(anyString());
        }

        @Test
        @DisplayName("當 Token 為 null 時不應該呼叫撤銷方法")
        void shouldNotRevokeWhenTokenIsNull() {
            // when
            authenticationService.logout(null);

            // then
            verify(refreshTokenService, never()).revokeRefreshToken(anyString());
        }
    }

    @Nested
    @DisplayName("登出所有設備測試")
    class LogoutAllDevicesTests {

        @Test
        @DisplayName("應該成功登出所有設備")
        void shouldLogoutAllDevices() {
            // when
            authenticationService.logoutAllDevices(testUserId);

            // then
            verify(refreshTokenService).revokeAllUserTokens(testUserId);
        }
    }

    @Nested
    @DisplayName("舊版認證方法測試")
    class LegacyAuthenticateTests {

        @Test
        @DisplayName("當密碼正確時應該回傳用戶")
        @SuppressWarnings("deprecation")
        void shouldReturnUserWhenPasswordMatches() {
            // given
            given(userRepository.findByUsername("testuser")).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willReturn(new UsernamePasswordAuthenticationToken("testuser", "password123"));

            // when
            Users result = authenticationService.authenticate("testuser", "password123");

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("當用戶不存在時應該回傳 null")
        @SuppressWarnings("deprecation")
        void shouldReturnNullWhenUserNotFound() {
            // given
            given(userRepository.findByUsername("unknownuser")).willReturn(Optional.empty());

            // when
            Users result = authenticationService.authenticate("unknownuser", "password123");

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("當密碼錯誤時應該回傳 null")
        @SuppressWarnings("deprecation")
        void shouldReturnNullWhenPasswordNotMatches() {
            // given
            given(userRepository.findByUsername("testuser")).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches("wrongpassword", "encodedPassword")).willReturn(false);

            // when
            Users result = authenticationService.authenticate("testuser", "wrongpassword");

            // then
            assertThat(result).isNull();
        }
    }
}
