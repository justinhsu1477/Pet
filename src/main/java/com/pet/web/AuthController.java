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
import com.pet.config.LineLoginConfig;
import com.pet.service.AuthenticationService;
import com.pet.service.LineOAuth2Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.pet.security.JwtProperties;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
     * Cookie Secure 屬性
     * - true: Cookie 只在 HTTPS 傳送（正式環境設 COOKIE_SECURE=true）
     * - false: 允許 HTTP 傳送（本機開發預設）
     */
    @org.springframework.beans.factory.annotation.Value("${app.cookie.secure:false}")
    private boolean cookieSecure;
    private final LineLoginConfig lineLoginConfig;

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
        issueTokens(authResponse, response, request);

        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    /**
     * 刷新 Access Token
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
        issueTokens(authResponse, response, request);

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
     * 統一的 Token 發放方法
     * 不管登入來源（帳密、LINE、未來的 Google），都在這裡：
     * 1. 設置 Refresh Token Cookie
     * 2. Web 端隱藏 body 中的 refreshToken
     */
    private void issueTokens(
            JwtAuthenticationResponse authResponse,
            HttpServletResponse httpResponse,
            HttpServletRequest httpRequest) {
        // 1. 把 Refresh Token 寫進 HttpOnly Cookie
        setRefreshTokenCookie(httpResponse, authResponse.getRefreshToken());

        // 2. Web 端把 body 裡的 refreshToken 清掉
        if (httpRequest != null) {
            String deviceType = httpRequest.getHeader("X-Device-Type");
            if (deviceType != null && deviceType.equalsIgnoreCase("WEB")) {
                authResponse.setRefreshToken(null);
            }
        }
    }

    /**
     * 設置 Refresh Token Cookie
     * 使用 HttpOnly 防止 XSS 攻擊
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)        // JavaScript 無法讀取
                .secure(cookieSecure)  // HTTPS 時設 true（環境變數 COOKIE_SECURE）
                .path("/")             // 所有路徑都可用
                .maxAge(jwtProperties.getRefreshTokenExpiration() / 1000)  // 7 天
                .sameSite("Lax")       // 允許同站點請求
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        log.debug("Set refresh token cookie - maxAge: {} seconds, secure: {}",
                jwtProperties.getRefreshTokenExpiration() / 1000, cookieSecure);
    }

    /**
     * 清除 Refresh Token Cookie
     */
    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(cookieSecure)
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
    public ResponseEntity<Void> lineOAuthLogin(HttpServletRequest httpRequest) {
        // 動態推導 callback URL，讓 ngrok/正式環境都能正確回呼
        String callbackUrl = resolveOAuthCallbackUrl(httpRequest);
        String authUrl = lineOAuth2Service.buildAuthorizationUrl(callbackUrl);
        return ResponseEntity.status(302)
                .header("Location", authUrl)
                .build();
    }

    /**
     * LINE OAuth 回調
     * LINE 授權完成後，帶著 code 和 state 導回這裡
     * 處理完成後 redirect 到前端 line-callback.html
     */
    @GetMapping("/oauth2/callback/line")
    public ResponseEntity<Void> lineOAuthCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        // 動態推導前端 URL：優先用請求的 origin（經 nginx 反向代理時正確）
        String frontendUrl = resolveFrontendCallbackUrl(httpRequest);
        try {
            if (error != null) {
                return redirectTo(frontendUrl + "?error="
                        + encode("您已取消 LINE 授權"));
            }

            // validateState 同時取回當初發起授權時的 callbackUrl（確保跟 LINE 一致）
            String callbackUrl = lineOAuth2Service.validateStateAndGetCallbackUrl(state);
            if (callbackUrl == null) {
                return redirectTo(frontendUrl + "?error="
                        + encode("授權已過期，請重新登入"));
            }

            String accessToken = lineOAuth2Service.exchangeCodeForAccessToken(code, callbackUrl);
            LineUserProfile profile = lineOAuth2Service.getUserProfile(accessToken);
            var existingUser = lineOAuth2Service.findExistingUser(profile.userId());

            if (existingUser.isPresent()) {
                JwtAuthenticationResponse authResponse =
                        lineOAuth2Service.loginExistingUser(existingUser.get());
                issueTokens(authResponse, httpResponse, null);
                String dn = profile.displayName() != null
                        ? profile.displayName() : "LINE 用戶";
                return redirectTo(frontendUrl + "?token=" + authResponse.getAccessToken()
                        + "&display_name=" + encode(dn));
            } else {
                String regToken =
                        lineOAuth2Service.generatePendingRegistrationToken(profile);
                String displayName = profile.displayName() != null
                        ? profile.displayName() : "LINE 用戶";
                return redirectTo(frontendUrl
                        + "?registration_token=" + regToken
                        + "&display_name=" + encode(displayName));
            }
        } catch (Exception e) {
            log.error("LINE OAuth 回調失敗", e);
            return redirectTo(frontendUrl + "?error="
                    + encode("LINE 登入失敗：" + e.getMessage()));
        }
    }

    /**
     * LINE OAuth 完成註冊（前端選擇角色後呼叫）
     * 回傳 JSON 格式的 JWT
     */
    @PostMapping("/oauth2/line/complete-registration")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>>
            lineOAuthCompleteRegistration(@RequestBody java.util.Map<String, String> body,
                                          HttpServletResponse httpResponse) {
        String token = body.get("token");
        String role = body.get("role");
        String displayName = body.get("displayName");
        Users user = lineOAuth2Service.completeRegistration(token, role, displayName);
        JwtAuthenticationResponse authResponse = lineOAuth2Service.loginExistingUser(user);
        issueTokens(authResponse, httpResponse, null);
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    /**
     * 動態推導 OAuth callback URL（後端接收 LINE 回呼的 URL）
     * 確保送給 LINE 的 redirect_uri 跟 LINE Console 註冊的一致
     * 經 nginx 反向代理時，使用 X-Forwarded-Proto/Host 還原真實 origin
     * 瀏覽器 → ngrok (HTTPS) → nginx (HTTP, port 3000) → backend (8080)
     */
    private String resolveOAuthCallbackUrl(HttpServletRequest request) {
        String configuredUrl = lineLoginConfig.getCallbackUrl();

        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        String forwardedHost = request.getHeader("X-Forwarded-Host");

        if (forwardedHost != null && !forwardedHost.isEmpty()) {
            String proto = (forwardedProto != null && !forwardedProto.isEmpty()) ? forwardedProto : "https";
            String origin = proto + "://" + forwardedHost;
            String callbackUrl = origin + "/api/auth/oauth2/callback/line";
            log.info("LINE OAuth login: 使用 forwarded callback URL={}", callbackUrl);
            return callbackUrl;
        }

        log.info("LINE OAuth login: 使用設定檔 callback URL={}", configuredUrl);
        return configuredUrl;
    }

    /**
     * 動態推導前端 callback URL
     * 當請求經過 nginx 反向代理時，使用 X-Forwarded-Proto/Host 還原真實 origin
     * 這樣不管是 localhost、ngrok 還是正式環境，都能正確 redirect
     */
    private String resolveFrontendCallbackUrl(HttpServletRequest request) {
        String configuredUrl = lineLoginConfig.getFrontendCallbackUrl();

        // 嘗試從 request header 推導 origin
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        String host = request.getHeader("Host");

        if (forwardedHost != null && !forwardedHost.isEmpty()) {
            String proto = (forwardedProto != null && !forwardedProto.isEmpty()) ? forwardedProto : "http";
            String origin = proto + "://" + forwardedHost;
            log.info("LINE OAuth callback: 使用 forwarded origin={} (X-Forwarded-Host={})", origin, forwardedHost);
            return origin + "/line-callback.html";
        }

        // fallback: 用設定檔的值
        log.info("LINE OAuth callback: 使用設定檔 frontendCallbackUrl={}", configuredUrl);
        return configuredUrl;
    }

    private ResponseEntity<Void> redirectTo(String url) {
        return ResponseEntity.status(302)
                .header("Location", url)
                .build();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
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
            default:
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
