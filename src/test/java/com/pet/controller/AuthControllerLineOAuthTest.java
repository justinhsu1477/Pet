package com.pet.controller;

import com.pet.domain.UserRole;
import com.pet.domain.Users;
import com.pet.dto.JwtAuthenticationResponse;
import com.pet.dto.LineUserProfile;
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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerLineOAuthTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private LineOAuth2Service lineOAuth2Service;

    @InjectMocks
    private AuthController authController;

    // ==================== lineOAuthLogin ====================

    @Test
    void shouldRedirectToLineAuthorizationUrl() {
        when(lineOAuth2Service.buildAuthorizationUrl()).thenReturn("https://access.line.me/oauth2/v2.1/authorize?test=1");

        ResponseEntity<Void> response = authController.lineOAuthLogin();

        assertEquals(302, response.getStatusCode().value());
        assertEquals("https://access.line.me/oauth2/v2.1/authorize?test=1",
                response.getHeaders().getFirst("Location"));
    }

    // ==================== lineOAuthCallback ====================

    @Test
    void shouldReturnErrorPageWhenErrorParam() {
        ResponseEntity<String> response = authController.lineOAuthCallback(null, null, "access_denied");

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("您已取消 LINE 授權"));
    }

    @Test
    void shouldReturnErrorPageWhenInvalidState() {
        when(lineOAuth2Service.validateState("bad-state")).thenReturn(false);

        ResponseEntity<String> response = authController.lineOAuthCallback("code123", "bad-state", null);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("授權已過期"));
    }

    @Test
    void shouldReturnErrorPageWhenNullState() {
        ResponseEntity<String> response = authController.lineOAuthCallback("code123", null, null);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("授權已過期"));
    }

    @Test
    void shouldReturnLoginSuccessPageForExistingUser() throws Exception {
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

        ResponseEntity<String> response = authController.lineOAuthCallback("code123", "valid-state", null);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("LINE 登入成功"));
        assertTrue(response.getBody().contains("testuser"));
        assertTrue(response.getBody().contains("jwt-token"));
    }

    @Test
    void shouldReturnRoleSelectionPageForNewUser() throws Exception {
        when(lineOAuth2Service.validateState("valid-state")).thenReturn(true);
        when(lineOAuth2Service.exchangeCodeForAccessToken("code123")).thenReturn("access-token");

        LineUserProfile profile = new LineUserProfile("U999", "NewUser", null, null);
        when(lineOAuth2Service.getUserProfile("access-token")).thenReturn(profile);
        when(lineOAuth2Service.findExistingUser("U999")).thenReturn(Optional.empty());
        when(lineOAuth2Service.generatePendingRegistrationToken(profile)).thenReturn("reg-token");

        ResponseEntity<String> response = authController.lineOAuthCallback("code123", "valid-state", null);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("選擇角色"));
        assertTrue(response.getBody().contains("NewUser"));
        assertTrue(response.getBody().contains("reg-token"));
    }

    @Test
    void shouldReturnErrorPageOnException() throws Exception {
        when(lineOAuth2Service.validateState("valid-state")).thenReturn(true);
        when(lineOAuth2Service.exchangeCodeForAccessToken("code123"))
                .thenThrow(new RuntimeException("Token exchange failed"));

        ResponseEntity<String> response = authController.lineOAuthCallback("code123", "valid-state", null);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Token exchange failed"));
    }

    // ==================== lineOAuthCompleteRegistration ====================

    @Test
    void shouldCompleteRegistrationAsCustomer() {
        Users newUser = createTestUser();
        when(lineOAuth2Service.completeRegistration("reg-token", "CUSTOMER")).thenReturn(newUser);

        JwtAuthenticationResponse authResponse = JwtAuthenticationResponse.builder()
                .accessToken("new-jwt")
                .username("testuser")
                .build();
        when(lineOAuth2Service.loginExistingUser(newUser)).thenReturn(authResponse);

        ResponseEntity<String> response = authController.lineOAuthCompleteRegistration("reg-token", "CUSTOMER");

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("註冊並登入成功"));
        assertTrue(response.getBody().contains("飼主"));
        assertTrue(response.getBody().contains("new-jwt"));
    }

    @Test
    void shouldCompleteRegistrationAsSitter() {
        Users newUser = createTestUser();
        when(lineOAuth2Service.completeRegistration("reg-token", "SITTER")).thenReturn(newUser);

        JwtAuthenticationResponse authResponse = JwtAuthenticationResponse.builder()
                .accessToken("new-jwt")
                .username("testuser")
                .build();
        when(lineOAuth2Service.loginExistingUser(newUser)).thenReturn(authResponse);

        ResponseEntity<String> response = authController.lineOAuthCompleteRegistration("reg-token", "SITTER");

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("保母"));
    }

    @Test
    void shouldReturnErrorPageOnRegistrationFailure() {
        when(lineOAuth2Service.completeRegistration("bad-token", "CUSTOMER"))
                .thenThrow(new RuntimeException("無效的註冊 Token"));

        ResponseEntity<String> response = authController.lineOAuthCompleteRegistration("bad-token", "CUSTOMER");

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("無效的註冊 Token"));
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
