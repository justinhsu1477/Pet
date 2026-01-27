package com.pet.controller;

import com.pet.config.LineLoginConfig;
import com.pet.domain.UserRole;
import com.pet.domain.Users;
import com.pet.dto.JwtAuthenticationResponse;
import com.pet.dto.LineUserProfile;
import com.pet.dto.response.ApiResponse;
import com.pet.security.JwtProperties;
import com.pet.service.AuthenticationService;
import com.pet.service.LineOAuth2Service;
import com.pet.web.AuthController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerLineOAuthTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private LineOAuth2Service lineOAuth2Service;

    @Mock
    private LineLoginConfig lineLoginConfig;

    @InjectMocks
    private AuthController authController;

    @Mock
    private HttpServletResponse httpServletResponse;

    private static final String FRONTEND_URL = "http://localhost:3000/line-callback.html";

    @BeforeEach
    void setUp() {
        lenient().when(lineLoginConfig.getFrontendCallbackUrl()).thenReturn(FRONTEND_URL);
    }

    // ==================== lineOAuthLogin ====================

    @Test
    void shouldRedirectToLineAuthorizationUrl() {
        when(lineOAuth2Service.buildAuthorizationUrl())
                .thenReturn("https://access.line.me/oauth2/v2.1/authorize?test=1");

        ResponseEntity<Void> response = authController.lineOAuthLogin();

        assertEquals(302, response.getStatusCode().value());
        assertEquals("https://access.line.me/oauth2/v2.1/authorize?test=1",
                response.getHeaders().getFirst("Location"));
    }

    // ==================== lineOAuthCallback ====================

    @Test
    void shouldRedirectWithErrorWhenErrorParam() {
        ResponseEntity<Void> response = authController.lineOAuthCallback(null, null, "access_denied", httpServletResponse);

        assertEquals(302, response.getStatusCode().value());
        String location = response.getHeaders().getFirst("Location");
        assertTrue(location.startsWith(FRONTEND_URL + "?error="));
    }

    @Test
    void shouldRedirectWithErrorWhenInvalidState() {
        when(lineOAuth2Service.validateState("bad-state")).thenReturn(false);

        ResponseEntity<Void> response = authController.lineOAuthCallback("code123", "bad-state", null, httpServletResponse);

        assertEquals(302, response.getStatusCode().value());
        String location = response.getHeaders().getFirst("Location");
        assertTrue(location.contains("error="));
    }

    @Test
    void shouldRedirectWithErrorWhenNullState() {
        ResponseEntity<Void> response = authController.lineOAuthCallback("code123", null, null, httpServletResponse);

        assertEquals(302, response.getStatusCode().value());
        String location = response.getHeaders().getFirst("Location");
        assertTrue(location.contains("error="));
    }

    @Test
    void shouldRedirectWithTokenForExistingUser() throws Exception {
        when(lineOAuth2Service.validateState("valid-state")).thenReturn(true);
        when(lineOAuth2Service.exchangeCodeForAccessToken("code123")).thenReturn("access-token");

        LineUserProfile profile = new LineUserProfile("U123", "TestUser", null, null);
        when(lineOAuth2Service.getUserProfile("access-token")).thenReturn(profile);

        Users existingUser = createTestUser();
        when(lineOAuth2Service.findExistingUser("U123")).thenReturn(Optional.of(existingUser));

        JwtAuthenticationResponse authResponse = JwtAuthenticationResponse.builder()
                .accessToken("jwt-token")
                .username("testuser")
                .build();
        when(lineOAuth2Service.loginExistingUser(existingUser)).thenReturn(authResponse);

        ResponseEntity<Void> response = authController.lineOAuthCallback("code123", "valid-state", null, httpServletResponse);

        assertEquals(302, response.getStatusCode().value());
        String location = response.getHeaders().getFirst("Location");
        assertTrue(location.contains("token=jwt-token"));
        assertFalse(location.contains("registration_token"));
    }

    @Test
    void shouldRedirectWithRegistrationTokenForNewUser() throws Exception {
        when(lineOAuth2Service.validateState("valid-state")).thenReturn(true);
        when(lineOAuth2Service.exchangeCodeForAccessToken("code123")).thenReturn("access-token");

        LineUserProfile profile = new LineUserProfile("U999", "NewUser", null, null);
        when(lineOAuth2Service.getUserProfile("access-token")).thenReturn(profile);
        when(lineOAuth2Service.findExistingUser("U999")).thenReturn(Optional.empty());
        when(lineOAuth2Service.generatePendingRegistrationToken(profile)).thenReturn("reg-token");

        ResponseEntity<Void> response = authController.lineOAuthCallback("code123", "valid-state", null, httpServletResponse);

        assertEquals(302, response.getStatusCode().value());
        String location = response.getHeaders().getFirst("Location");
        assertTrue(location.contains("registration_token=reg-token"));
        assertTrue(location.contains("display_name=NewUser"));
    }

    @Test
    void shouldRedirectWithErrorOnException() throws Exception {
        when(lineOAuth2Service.validateState("valid-state")).thenReturn(true);
        when(lineOAuth2Service.exchangeCodeForAccessToken("code123"))
                .thenThrow(new RuntimeException("Token exchange failed"));

        ResponseEntity<Void> response = authController.lineOAuthCallback("code123", "valid-state", null, httpServletResponse);

        assertEquals(302, response.getStatusCode().value());
        String location = response.getHeaders().getFirst("Location");
        assertTrue(location.contains("error="));
    }

    // ==================== lineOAuthCompleteRegistration ====================

    @Test
    void shouldCompleteRegistrationAndReturnJson() {
        Users newUser = createTestUser();
        when(lineOAuth2Service.completeRegistration("reg-token", "CUSTOMER")).thenReturn(newUser);

        JwtAuthenticationResponse authResponse = JwtAuthenticationResponse.builder()
                .accessToken("new-jwt")
                .username("testuser")
                .build();
        when(lineOAuth2Service.loginExistingUser(newUser)).thenReturn(authResponse);

        Map<String, String> body = Map.of("token", "reg-token", "role", "CUSTOMER");
        ResponseEntity<ApiResponse<JwtAuthenticationResponse>> response =
                authController.lineOAuthCompleteRegistration(body, httpServletResponse);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("new-jwt", response.getBody().data().getAccessToken());
    }

    @Test
    void shouldCompleteRegistrationAsSitter() {
        Users newUser = createTestUser();
        when(lineOAuth2Service.completeRegistration("reg-token", "SITTER")).thenReturn(newUser);

        JwtAuthenticationResponse authResponse = JwtAuthenticationResponse.builder()
                .accessToken("sitter-jwt")
                .username("testuser")
                .build();
        when(lineOAuth2Service.loginExistingUser(newUser)).thenReturn(authResponse);

        Map<String, String> body = Map.of("token", "reg-token", "role", "SITTER");
        ResponseEntity<ApiResponse<JwtAuthenticationResponse>> response =
                authController.lineOAuthCompleteRegistration(body, httpServletResponse);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("sitter-jwt", response.getBody().data().getAccessToken());
    }

    // ==================== helper ====================

    private Users createTestUser() {
        Users user = new Users();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole(UserRole.CUSTOMER);
        return user;
    }
}
