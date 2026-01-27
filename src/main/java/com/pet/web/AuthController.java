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
import com.pet.dto.LineUserProfile;
import com.pet.service.AuthenticationService;
import com.pet.service.LineOAuth2Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.pet.security.JwtProperties;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    private final LineOAuth2Service lineOAuth2Service;

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

    /**
     * 設置 Refresh Token Cookie
     * 使用 HttpOnly 防止 XSS 攻擊，適合面試展示
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)        // JavaScript 無法讀取
                .secure(false)         // HTTP 環境（面試展示用）
                .path("/")             // 所有路徑都可用
                .maxAge(jwtProperties.getRefreshTokenExpiration() / 1000)  // 7 天
                .sameSite("Lax")       // 允許同站點請求
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        log.debug("Set refresh token cookie - maxAge: {} seconds",
                jwtProperties.getRefreshTokenExpiration() / 1000);
    }

    /**
     * 清除 Refresh Token Cookie
     */
    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)            // 立即過期
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        log.debug("Cleared refresh token cookie");
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

    // ==================== LINE OAuth 2.0 登入 ====================

    /**
     * LINE OAuth 登入入口
     * 重導向到 LINE 授權頁面
     */
    @GetMapping("/oauth2/line")
    public ResponseEntity<Void> lineOAuthLogin() {
        String authUrl = lineOAuth2Service.buildAuthorizationUrl();
        return ResponseEntity.status(302)
                .header("Location", authUrl)
                .build();
    }

    /**
     * LINE OAuth 回調
     * LINE 授權完成後，會帶著 code 和 state 導回這裡
     */
    @GetMapping(value = "/oauth2/callback/line", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> lineOAuthCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error) {
        try {
            if (error != null) {
                return ResponseEntity.ok(errorPage("您已取消 LINE 授權"));
            }

            if (state == null || !lineOAuth2Service.validateState(state)) {
                return ResponseEntity.ok(errorPage("授權已過期，請重新登入"));
            }

            String accessToken = lineOAuth2Service.exchangeCodeForAccessToken(code);
            LineUserProfile profile = lineOAuth2Service.getUserProfile(accessToken);
            var existingUser = lineOAuth2Service.findExistingUser(profile.userId());

            if (existingUser.isPresent()) {
                JwtAuthenticationResponse authResponse = lineOAuth2Service.loginExistingUser(existingUser.get());
                String html = """
                    <!DOCTYPE html>
                    <html lang="zh-TW">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>登入成功</title>
                        <style>
                            body { font-family: -apple-system, sans-serif; max-width: 400px; margin: 40px auto; padding: 20px; background: #f5f5f5; }
                            .card { background: white; border-radius: 12px; padding: 30px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); text-align: center; }
                            h2 { color: #06C755; }
                            .token-info { background: #f0f0f0; border-radius: 8px; padding: 15px; margin-top: 15px; word-break: break-all; font-size: 12px; text-align: left; }
                            .token-label { font-weight: bold; color: #333; }
                        </style>
                    </head>
                    <body>
                        <div class="card">
                            <h2>LINE 登入成功！</h2>
                            <p>歡迎回來，<strong>%s</strong></p>
                            <div class="token-info">
                                <p class="token-label">Access Token:</p>
                                <p id="token">%s</p>
                            </div>
                            <p style="color:#999; font-size:13px; margin-top:15px;">請將此 Token 複製到 App 中使用</p>
                        </div>
                    </body>
                    </html>
                    """.formatted(authResponse.getUsername(), authResponse.getAccessToken());
                return ResponseEntity.ok(html);
            } else {
                String registrationToken = lineOAuth2Service.generatePendingRegistrationToken(profile);
                String displayName = profile.displayName() != null ? profile.displayName() : "LINE 用戶";
                String html = """
                    <!DOCTYPE html>
                    <html lang="zh-TW">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>選擇角色</title>
                        <style>
                            body { font-family: -apple-system, sans-serif; max-width: 400px; margin: 40px auto; padding: 20px; background: #f5f5f5; }
                            .card { background: white; border-radius: 12px; padding: 30px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); text-align: center; }
                            h2 { color: #06C755; }
                            .role-btn { display: block; width: 100%%; padding: 18px; margin-top: 15px; border: 2px solid #ddd; border-radius: 12px; background: white; font-size: 18px; cursor: pointer; transition: all 0.2s; }
                            .role-btn:hover { border-color: #06C755; background: #f0fff5; }
                            .subtitle { color: #666; margin-bottom: 20px; }
                        </style>
                    </head>
                    <body>
                        <div class="card">
                            <h2>歡迎，%s！</h2>
                            <p class="subtitle">請選擇您的角色</p>
                            <form method="POST" action="/api/auth/oauth2/line/complete-registration">
                                <input type="hidden" name="token" value="%s">
                                <button type="submit" name="role" value="CUSTOMER" class="role-btn">
                                    我是飼主（找保母）
                                </button>
                                <button type="submit" name="role" value="SITTER" class="role-btn">
                                    我是保母（接案）
                                </button>
                            </form>
                        </div>
                    </body>
                    </html>
                    """.formatted(displayName, registrationToken);
                return ResponseEntity.ok(html);
            }
        } catch (Exception e) {
            log.error("LINE OAuth 回調失敗", e);
            return ResponseEntity.ok(errorPage("LINE 登入失敗：" + e.getMessage()));
        }
    }

    /**
     * LINE OAuth 完成註冊（角色選擇後）
     */
    @PostMapping(value = "/oauth2/line/complete-registration", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> lineOAuthCompleteRegistration(
            @RequestParam String token,
            @RequestParam String role) {
        try {
            Users user = lineOAuth2Service.completeRegistration(token, role);
            JwtAuthenticationResponse authResponse = lineOAuth2Service.loginExistingUser(user);
            String html = """
                <!DOCTYPE html>
                <html lang="zh-TW">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>註冊成功</title>
                    <style>
                        body { font-family: -apple-system, sans-serif; max-width: 400px; margin: 40px auto; padding: 20px; background: #f5f5f5; }
                        .card { background: white; border-radius: 12px; padding: 30px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); text-align: center; }
                        h2 { color: #06C755; }
                        .token-info { background: #f0f0f0; border-radius: 8px; padding: 15px; margin-top: 15px; word-break: break-all; font-size: 12px; text-align: left; }
                        .token-label { font-weight: bold; color: #333; }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <h2>註冊並登入成功！</h2>
                        <p>帳號：<strong>%s</strong></p>
                        <p>角色：<strong>%s</strong></p>
                        <div class="token-info">
                            <p class="token-label">Access Token:</p>
                            <p>%s</p>
                        </div>
                        <p style="color:#999; font-size:13px; margin-top:15px;">請將此 Token 複製到 App 中使用</p>
                    </div>
                </body>
                </html>
                """.formatted(
                    authResponse.getUsername(),
                    "SITTER".equalsIgnoreCase(role) ? "保母" : "飼主",
                    authResponse.getAccessToken());
            return ResponseEntity.ok(html);
        } catch (Exception e) {
            log.error("LINE OAuth 註冊失敗", e);
            return ResponseEntity.ok(errorPage(e.getMessage()));
        }
    }

    private String errorPage(String message) {
        return """
            <!DOCTYPE html>
            <html lang="zh-TW">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>錯誤</title>
                <style>
                    body { font-family: -apple-system, sans-serif; max-width: 400px; margin: 40px auto; padding: 20px; background: #f5f5f5; }
                    .card { background: white; border-radius: 12px; padding: 30px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); text-align: center; }
                    h2 { color: #e74c3c; }
                </style>
            </head>
            <body>
                <div class="card">
                    <h2>操作失敗</h2>
                    <p>%s</p>
                </div>
            </body>
            </html>
            """.formatted(message);
    }

    // ==================== 舊版登入 (向後兼容) ====================

    /**
     * 舊版登入端點 (向後兼容)
     *
     * @deprecated 此 API 已棄用，請使用 /api/auth/jwt/login
     * 此 API 將在未來版本中移除
     *
     * 舊版 API 的問題：
     * - 不返回 JWT Token，無法實現 stateless 認證
     * - 缺少 Refresh Token 機制
     * - 無法支援設備管理和登出所有設備
     *
     * 請遷移至新版 JWT API：
     * POST /api/auth/jwt/login
     */
    @PostMapping("/login")
    @Deprecated(since = "2.0", forRemoval = true)
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        log.warn("⚠️ 使用了已棄用的 API: /api/auth/login - 請遷移至 /api/auth/jwt/login");

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
