package com.pet.security;

import com.pet.domain.UserRole;
import com.pet.domain.Users;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret("test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm-minimum-256-bits");
        jwtProperties.setAccessTokenExpiration(900000L);
        jwtProperties.setRefreshTokenExpiration(604800000L);
        jwtProperties.setIssuer("test-issuer");
        jwtService = new JwtService(jwtProperties);
    }

    // ==================== generateAccessToken ====================

    @Test
    void shouldGenerateAccessToken() {
        Users user = createTestUser();

        String token = jwtService.generateAccessToken(user);

        assertNotNull(token);
        assertEquals("testuser", jwtService.extractUsername(token));
        assertEquals(user.getId(), jwtService.extractUserId(token));
        assertEquals(UserRole.CUSTOMER, jwtService.extractRole(token));
        assertTrue(jwtService.validateToken(token));
    }

    // ==================== generateRefreshToken ====================

    @Test
    void shouldGenerateRefreshToken() {
        Users user = createTestUser();

        String token = jwtService.generateRefreshToken(user);

        assertNotNull(token);
        assertTrue(jwtService.isRefreshToken(token));
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void shouldNotBeRefreshTokenForAccessToken() {
        Users user = createTestUser();
        String token = jwtService.generateAccessToken(user);

        assertFalse(jwtService.isRefreshToken(token));
    }

    // ==================== generateRegistrationToken ====================

    @Test
    void shouldGenerateRegistrationToken() {
        String lineUserId = "U123456";

        String token = jwtService.generateRegistrationToken(lineUserId);

        assertNotNull(token);
        assertEquals("line-registration", jwtService.extractUsername(token));
    }

    @Test
    void shouldExtractLineUserIdFromRegistrationToken() {
        String lineUserId = "U123456";
        String token = jwtService.generateRegistrationToken(lineUserId);

        String extracted = jwtService.extractLineUserIdFromRegistrationToken(token);

        assertEquals(lineUserId, extracted);
    }

    @Test
    void shouldRejectAccessTokenAsRegistrationToken() {
        Users user = createTestUser();
        String accessToken = jwtService.generateAccessToken(user);

        assertThrows(IllegalArgumentException.class,
                () -> jwtService.extractLineUserIdFromRegistrationToken(accessToken));
    }

    // ==================== validateToken ====================

    @Test
    void shouldRejectInvalidToken() {
        assertFalse(jwtService.validateToken("invalid-token"));
    }

    @Test
    void shouldRejectExpiredToken() {
        jwtProperties.setAccessTokenExpiration(0L);
        JwtService shortLivedService = new JwtService(jwtProperties);
        Users user = createTestUser();

        String token = shortLivedService.generateAccessToken(user);

        assertFalse(shortLivedService.validateToken(token));
    }

    // ==================== extractAuthorities ====================

    @Test
    void shouldExtractAuthorities() {
        Users user = createTestUser();
        String token = jwtService.generateAccessToken(user);

        var authorities = jwtService.extractAuthorities(token);

        assertEquals(1, authorities.size());
        assertEquals("ROLE_CUSTOMER", authorities.iterator().next().getAuthority());
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
