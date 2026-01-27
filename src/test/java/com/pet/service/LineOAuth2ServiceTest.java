package com.pet.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.config.LineLoginConfig;
import com.pet.domain.*;
import com.pet.dto.JwtAuthenticationResponse;
import com.pet.dto.LineUserProfile;
import com.pet.repository.CustomerRepository;
import com.pet.repository.SitterRepository;
import com.pet.repository.UserRepository;
import com.pet.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LineOAuth2ServiceTest {

    @Mock private LineLoginConfig lineLoginConfig;
    @Mock private UserRepository userRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private SitterRepository sitterRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private ObjectMapper objectMapper;
    @Mock private HttpClient httpClient;

    @InjectMocks
    private LineOAuth2Service lineOAuth2Service;

    // ==================== buildAuthorizationUrl ====================

    @Test
    void shouldBuildAuthorizationUrl() {
        when(lineLoginConfig.getChannelId()).thenReturn("12345");
        when(lineLoginConfig.getCallbackUrl()).thenReturn("http://localhost:8080/api/auth/oauth2/callback/line");

        String url = lineOAuth2Service.buildAuthorizationUrl();

        assertTrue(url.startsWith("https://access.line.me/oauth2/v2.1/authorize"));
        assertTrue(url.contains("client_id=12345"));
        assertTrue(url.contains("response_type=code"));
        assertTrue(url.contains("scope=profile"));
        assertTrue(url.contains("state="));
    }

    @Test
    void shouldEncodeCallbackUrlInAuthorizationUrl() {
        when(lineLoginConfig.getChannelId()).thenReturn("12345");
        when(lineLoginConfig.getCallbackUrl()).thenReturn("http://localhost:8080/api/auth/oauth2/callback/line");

        String url = lineOAuth2Service.buildAuthorizationUrl();

        assertTrue(url.contains("redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fapi%2Fauth%2Foauth2%2Fcallback%2Fline"));
    }

    // ==================== validateState ====================

    @Test
    void shouldValidateState() {
        when(lineLoginConfig.getChannelId()).thenReturn("12345");
        when(lineLoginConfig.getCallbackUrl()).thenReturn("http://localhost:8080/callback");

        String url = lineOAuth2Service.buildAuthorizationUrl();
        String state = extractStateFromUrl(url);

        assertTrue(lineOAuth2Service.validateState(state));
    }

    @Test
    void shouldRejectStateOnSecondUse() {
        when(lineLoginConfig.getChannelId()).thenReturn("12345");
        when(lineLoginConfig.getCallbackUrl()).thenReturn("http://localhost:8080/callback");

        String url = lineOAuth2Service.buildAuthorizationUrl();
        String state = extractStateFromUrl(url);

        lineOAuth2Service.validateState(state);
        assertFalse(lineOAuth2Service.validateState(state));
    }

    @Test
    void shouldRejectInvalidState() {
        assertFalse(lineOAuth2Service.validateState("invalid-state"));
    }

    @Test
    void shouldRejectNullState() {
        assertFalse(lineOAuth2Service.validateState(null));
    }

    // ==================== exchangeCodeForAccessToken ====================

    @SuppressWarnings("unchecked")
    @Test
    void shouldExchangeCodeForAccessToken() throws Exception {
        when(lineLoginConfig.getCallbackUrl()).thenReturn("http://localhost:8080/callback");
        when(lineLoginConfig.getChannelId()).thenReturn("12345");
        when(lineLoginConfig.getChannelSecret()).thenReturn("secret");

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{\"access_token\":\"test-token\"}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        JsonNode jsonNode = mock(JsonNode.class);
        JsonNode tokenNode = mock(JsonNode.class);
        when(tokenNode.asText()).thenReturn("test-token");
        when(jsonNode.get("access_token")).thenReturn(tokenNode);
        when(objectMapper.readTree("{\"access_token\":\"test-token\"}")).thenReturn(jsonNode);

        String token = lineOAuth2Service.exchangeCodeForAccessToken("auth-code");

        assertEquals("test-token", token);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldThrowWhenTokenExchangeFails() throws Exception {
        when(lineLoginConfig.getCallbackUrl()).thenReturn("http://localhost:8080/callback");
        when(lineLoginConfig.getChannelId()).thenReturn("12345");
        when(lineLoginConfig.getChannelSecret()).thenReturn("secret");

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(400);
        when(mockResponse.body()).thenReturn("error");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        assertThrows(RuntimeException.class,
                () -> lineOAuth2Service.exchangeCodeForAccessToken("bad-code"));
    }

    // ==================== getUserProfile ====================

    @SuppressWarnings("unchecked")
    @Test
    void shouldGetUserProfile() throws Exception {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        String profileJson = "{\"userId\":\"U123\",\"displayName\":\"Test User\",\"pictureUrl\":\"http://pic.url\"}";
        when(mockResponse.body()).thenReturn(profileJson);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        JsonNode jsonNode = mock(JsonNode.class);
        JsonNode userIdNode = mock(JsonNode.class);
        JsonNode displayNameNode = mock(JsonNode.class);
        JsonNode pictureUrlNode = mock(JsonNode.class);
        when(userIdNode.asText()).thenReturn("U123");
        when(displayNameNode.asText()).thenReturn("Test User");
        when(pictureUrlNode.asText()).thenReturn("http://pic.url");
        when(jsonNode.get("userId")).thenReturn(userIdNode);
        when(jsonNode.has("displayName")).thenReturn(true);
        when(jsonNode.get("displayName")).thenReturn(displayNameNode);
        when(jsonNode.has("pictureUrl")).thenReturn(true);
        when(jsonNode.get("pictureUrl")).thenReturn(pictureUrlNode);
        when(objectMapper.readTree(profileJson)).thenReturn(jsonNode);

        LineUserProfile profile = lineOAuth2Service.getUserProfile("access-token");

        assertEquals("U123", profile.userId());
        assertEquals("Test User", profile.displayName());
        assertEquals("http://pic.url", profile.pictureUrl());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldThrowWhenProfileFetchFails() throws Exception {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(401);
        when(mockResponse.body()).thenReturn("unauthorized");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        assertThrows(RuntimeException.class,
                () -> lineOAuth2Service.getUserProfile("bad-token"));
    }

    // ==================== findExistingUser ====================

    @Test
    void shouldFindExistingUser() {
        Users user = new Users();
        user.setLineUserId("U123");
        when(userRepository.findByLineUserId("U123")).thenReturn(Optional.of(user));

        Optional<Users> result = lineOAuth2Service.findExistingUser("U123");

        assertTrue(result.isPresent());
    }

    @Test
    void shouldReturnEmptyWhenUserNotFound() {
        when(userRepository.findByLineUserId("U999")).thenReturn(Optional.empty());

        Optional<Users> result = lineOAuth2Service.findExistingUser("U999");

        assertTrue(result.isEmpty());
    }

    // ==================== loginExistingUser ====================

    @Test
    void shouldLoginExistingUser() {
        Users user = new Users();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setRole(UserRole.CUSTOMER);
        user.setLineUserId("U123");

        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");

        JwtAuthenticationResponse response = lineOAuth2Service.loginExistingUser(user);

        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("testuser", response.getUsername());
        assertEquals("CUSTOMER", response.getRole());
        assertEquals(user.getId(), response.getUserId());
    }

    @Test
    void shouldLoginExistingUserAsSitter() {
        Users user = new Users();
        user.setId(UUID.randomUUID());
        user.setUsername("sitter01");
        user.setRole(UserRole.SITTER);
        user.setLineUserId("U456");

        when(jwtService.generateAccessToken(user)).thenReturn("sitter-access");
        when(jwtService.generateRefreshToken(user)).thenReturn("sitter-refresh");

        JwtAuthenticationResponse response = lineOAuth2Service.loginExistingUser(user);

        assertEquals("SITTER", response.getRole());
        assertEquals("sitter01", response.getUsername());
    }

    // ==================== completeRegistration ====================

    @Test
    void shouldCompleteRegistrationAsCustomer() {
        when(jwtService.extractLineUserIdFromRegistrationToken("token")).thenReturn("U123456789abcd");
        when(userRepository.findByLineUserId("U123456789abcd")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        Users savedUser = new Users();
        savedUser.setId(UUID.randomUUID());
        savedUser.setUsername("line_123456789abc");
        savedUser.setRole(UserRole.CUSTOMER);
        when(userRepository.save(any(Users.class))).thenReturn(savedUser);

        Users result = lineOAuth2Service.completeRegistration("token", "CUSTOMER");

        assertNotNull(result);
        assertEquals(UserRole.CUSTOMER, result.getRole());
        verify(customerRepository).save(any(Customer.class));
        verify(sitterRepository, never()).save(any(Sitter.class));
    }

    @Test
    void shouldCompleteRegistrationAsSitter() {
        when(jwtService.extractLineUserIdFromRegistrationToken("token")).thenReturn("U123456789abcd");
        when(userRepository.findByLineUserId("U123456789abcd")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        Users savedUser = new Users();
        savedUser.setId(UUID.randomUUID());
        savedUser.setUsername("line_123456789abc");
        savedUser.setRole(UserRole.SITTER);
        when(userRepository.save(any(Users.class))).thenReturn(savedUser);

        Users result = lineOAuth2Service.completeRegistration("token", "SITTER");

        assertNotNull(result);
        assertEquals(UserRole.SITTER, result.getRole());
        verify(sitterRepository).save(any(Sitter.class));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void shouldThrowWhenLineAlreadyBound() {
        when(jwtService.extractLineUserIdFromRegistrationToken("token")).thenReturn("U123");
        when(userRepository.findByLineUserId("U123")).thenReturn(Optional.of(new Users()));

        assertThrows(IllegalStateException.class,
                () -> lineOAuth2Service.completeRegistration("token", "CUSTOMER"));
        verify(userRepository, never()).save(any(Users.class));
    }

    @Test
    void shouldSetLineUserIdOnNewUser() {
        when(jwtService.extractLineUserIdFromRegistrationToken("token")).thenReturn("Uabcdef12345");
        when(userRepository.findByLineUserId("Uabcdef12345")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        Users savedUser = new Users();
        savedUser.setId(UUID.randomUUID());
        savedUser.setRole(UserRole.CUSTOMER);
        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> {
            Users u = invocation.getArgument(0);
            assertEquals("Uabcdef12345", u.getLineUserId());
            assertEquals("line_abcdef12345", u.getUsername());
            savedUser.setUsername(u.getUsername());
            savedUser.setLineUserId(u.getLineUserId());
            return savedUser;
        });

        lineOAuth2Service.completeRegistration("token", "CUSTOMER");

        verify(userRepository).save(any(Users.class));
    }

    // ==================== generatePendingRegistrationToken ====================

    @Test
    void shouldGeneratePendingRegistrationToken() {
        LineUserProfile profile = new LineUserProfile("U123", "Test", null, null);
        when(jwtService.generateRegistrationToken("U123")).thenReturn("reg-token");

        String token = lineOAuth2Service.generatePendingRegistrationToken(profile);

        assertEquals("reg-token", token);
        verify(jwtService).generateRegistrationToken("U123");
    }

    // ==================== Helper ====================

    private String extractStateFromUrl(String url) {
        String state = url.substring(url.indexOf("state=") + 6);
        if (state.contains("&")) {
            state = state.substring(0, state.indexOf("&"));
        }
        return state;
    }
}
