package com.pet.web;

import com.pet.domain.Users;
import java.util.UUID;
import com.pet.dto.JwtAuthenticationResponse;
import com.pet.dto.LoginRequestDto;
import com.pet.dto.LoginResponseDto;
import com.pet.dto.RefreshTokenRequest;
import com.pet.dto.response.ApiResponse;
import com.pet.exception.AuthenticationException;
import com.pet.exception.ErrorCode;
import com.pet.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.pet.security.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 認證控制器

 * 提供兩種登入方式:
 * 1. /api/auth/login - 舊版登入 (向後兼容)
 * 2. /api/auth/jwt/login - JWT 登入
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationService authenticationService;
    private final JwtProperties jwtProperties;

    /**
     * JWT 登入端點
     * 特點:
     * - 返回 Access Token 和 Refresh Token (Cookie)
     * - 支援 Web/App 設備類型自動識別
     * - 記錄登入設備和 IP
     * Headers:
     * - X-Device-Type: WEB 或 APP (可選,會自動判斷)
     */
    @PostMapping("/jwt/login")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> jwtLogin(
        @Valid @RequestBody LoginRequestDto loginRequest,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        log.info("JWT login attempt for user: {}", loginRequest.username());

        JwtAuthenticationResponse authResponse = authenticationService.login(loginRequest, request);
        
        // 將 Refresh Token 寫入 HttpOnly Cookie
        setRefreshTokenCookie(response, authResponse.getRefreshToken());
        
        // 安全起見，Web 端不應從 body 獲取 refreshToken
        if (request.getHeader("X-Device-Type") != null && request.getHeader("X-Device-Type").equalsIgnoreCase("WEB")) {
            authResponse.setRefreshToken(null);
        }

        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    /**
     * 刷新 Access Token
     *
     * 當 Access Token 過期時,使用 Refresh Token 獲取新的 Access Token
     */
    @PostMapping("/jwt/refresh")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> refreshToken(
        HttpServletRequest request,
        @RequestBody(required = false) RefreshTokenRequest requestDto,
        @CookieValue(name = "refreshToken", required = false) String cookieRefreshToken,
        HttpServletResponse response
    ) {
        log.info("Refresh token request");

        String refreshToken = cookieRefreshToken;
        if (refreshToken == null && requestDto != null) {
            refreshToken = requestDto.getRefreshToken();
        }

        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new AuthenticationException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(refreshToken);
        
        JwtAuthenticationResponse authResponse = authenticationService.refreshToken(refreshRequest);
        
        // 更新 Cookie
        setRefreshTokenCookie(response, authResponse.getRefreshToken());

        // 安全起見，Web 端不應從 body 獲取 refreshToken
        if (request.getHeader("X-Device-Type") != null && request.getHeader("X-Device-Type").equalsIgnoreCase("WEB")) {
            authResponse.setRefreshToken(null);
        }

        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    /**
     * 登出
     *
     * 撤銷當前的 Refresh Token
     */
    @PostMapping("/jwt/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
        @RequestBody(required = false) RefreshTokenRequest requestDto,
        @CookieValue(name = "refreshToken", required = false) String cookieRefreshToken,
        HttpServletResponse response
    ) {
        log.info("Logout request");

        String refreshToken = cookieRefreshToken;
        if (refreshToken == null && requestDto != null) {
            refreshToken = requestDto.getRefreshToken();
        }

        authenticationService.logout(refreshToken);
        
        // 清除 Cookie
        clearRefreshTokenCookie(response);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // 開發環境先設為 false，正式環境應為 true
                .path("/")
                .maxAge(jwtProperties.getRefreshTokenExpiration() / 1000)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * 登出所有設備
     *
     * 撤銷用戶的所有 Refresh Token
     */
    @PostMapping("/jwt/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAllDevices(
        @RequestParam UUID userId
    ) {
        log.info("Logout all devices for user: {}", userId);

        authenticationService.logoutAllDevices(userId);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ==================== 舊版登入 (向後兼容) ====================

    /**
     * 舊版登入端點 (向後兼容)
     *
     * @deprecated 請使用 /api/auth/jwt/login
     */
    @PostMapping("/login")
    @Deprecated
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        Users users = authenticationService.authenticate(loginRequest.username(), loginRequest.password());

        if (users == null) {
            throw new AuthenticationException(ErrorCode.INVALID_PASSWORD);
        }

        // 根據角色取得對應的 ID 和名稱
        UUID roleId = null;
        String roleName = null;

        switch (users.getRole()) {
            case CUSTOMER:
                if (users.getCustomer() != null) {
                    roleId = users.getCustomer().getId();
                    roleName = users.getCustomer().getName();
                }
                break;
            case SITTER:
                if (users.getSitter() != null) {
                    roleId = users.getSitter().getId();
                    roleName = users.getSitter().getName();
                }
                break;
            case ADMIN:
                // 管理員可能沒有對應的角色資料
                roleId = users.getId();
                roleName = users.getUsername();
                break;
        }

        LoginResponseDto loginResponse = new LoginResponseDto(
                users.getId(),
                users.getUsername(),
                users.getEmail(),
                users.getPhone(),
                users.getRole().name(),
                roleId,
                roleName,
                "登入成功"
        );

        return ResponseEntity.ok(ApiResponse.success(loginResponse));
    }
}
