package com.pet.service;

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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

    @InjectMocks
    private LineOAuth2Service lineOAuth2Service;

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
    void shouldValidateState() {
        when(lineLoginConfig.getChannelId()).thenReturn("12345");
        when(lineLoginConfig.getCallbackUrl()).thenReturn("http://localhost:8080/callback");

        String url = lineOAuth2Service.buildAuthorizationUrl();
        String state = url.substring(url.indexOf("state=") + 6);
        if (state.contains("&")) state = state.substring(0, state.indexOf("&"));

        assertTrue(lineOAuth2Service.validateState(state));
        assertFalse(lineOAuth2Service.validateState(state));
    }

    @Test
    void shouldRejectInvalidState() {
        assertFalse(lineOAuth2Service.validateState("invalid-state"));
    }

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
    }

    @Test
    void shouldFindExistingUser() {
        Users user = new Users();
        user.setLineUserId("U123");
        when(userRepository.findByLineUserId("U123")).thenReturn(Optional.of(user));

        Optional<Users> result = lineOAuth2Service.findExistingUser("U123");

        assertTrue(result.isPresent());
    }

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
        verify(sitterRepository).save(any(Sitter.class));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void shouldThrowWhenLineAlreadyBound() {
        when(jwtService.extractLineUserIdFromRegistrationToken("token")).thenReturn("U123");
        when(userRepository.findByLineUserId("U123")).thenReturn(Optional.of(new Users()));

        assertThrows(IllegalStateException.class,
                () -> lineOAuth2Service.completeRegistration("token", "CUSTOMER"));
    }

    @Test
    void shouldGeneratePendingRegistrationToken() {
        LineUserProfile profile = new LineUserProfile("U123", "Test", null, null);
        when(jwtService.generateRegistrationToken("U123")).thenReturn("reg-token");

        String token = lineOAuth2Service.generatePendingRegistrationToken(profile);

        assertEquals("reg-token", token);
    }
}
