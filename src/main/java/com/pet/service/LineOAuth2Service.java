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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class LineOAuth2Service {

    private final LineLoginConfig lineLoginConfig;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final SitterRepository sitterRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final LineRichMenuService lineRichMenuService;

    private static final String LINE_AUTH_URL = "https://access.line.me/oauth2/v2.1/authorize";
    private static final String LINE_TOKEN_URL = "https://api.line.me/oauth2/v2.1/token";
    private static final String LINE_PROFILE_URL = "https://api.line.me/v2/profile";

    private final HttpClient httpClient;

    // state -> (timestamp, callbackUrl) for CSRF protection (production should use Redis)
    private record StateInfo(long timestamp, String callbackUrl) {}
    private final Map<String, StateInfo> stateStore = new ConcurrentHashMap<>();

    /**
     * 產生 LINE 授權 URL
     * @param callbackUrl 動態推導的 callback URL（經 nginx/ngrok 反向代理時使用真實 origin）
     */
    public String buildAuthorizationUrl(String callbackUrl) {
        String state = UUID.randomUUID().toString();
        stateStore.put(state, new StateInfo(System.currentTimeMillis(), callbackUrl));
        cleanExpiredStates();

        log.info("LINE OAuth: 產生授權 URL, redirect_uri={}", callbackUrl);

        return LINE_AUTH_URL
                + "?response_type=code"
                + "&client_id=" + lineLoginConfig.getChannelId()
                + "&redirect_uri=" + URLEncoder.encode(callbackUrl, StandardCharsets.UTF_8)
                + "&state=" + state
                + "&scope=profile%20openid%20email";
    }

    /**
     * 驗證 state 參數（防 CSRF），並回傳當時的 callbackUrl
     * @return callbackUrl if valid, null if invalid
     */
    public String validateStateAndGetCallbackUrl(String state) {
        if (state == null) {
            return null;
        }
        StateInfo info = stateStore.remove(state);
        if (info == null) {
            return null;
        }
        if ((System.currentTimeMillis() - info.timestamp()) >= 600_000) {
            return null;
        }
        return info.callbackUrl();
    }

    /**
     * 用 authorization code 換取 access token
     * @param callbackUrl 必須跟 buildAuthorizationUrl 時用的一模一樣
     */
    public String exchangeCodeForAccessToken(String code, String callbackUrl) throws Exception {
        String body = "grant_type=authorization_code"
                + "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(callbackUrl, StandardCharsets.UTF_8)
                + "&client_id=" + lineLoginConfig.getChannelId()
                + "&client_secret=" + lineLoginConfig.getChannelSecret();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(LINE_TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("LINE token exchange failed: {}", response.body());
            throw new RuntimeException("LINE 授權失敗，請重試");
        }

        JsonNode json = objectMapper.readTree(response.body());
        return json.get("access_token").asText();
    }

    /**
     * 用 access token 取得 LINE 用戶資料
     */
    public LineUserProfile getUserProfile(String accessToken) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(LINE_PROFILE_URL))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("LINE profile fetch failed: {}", response.body());
            throw new RuntimeException("無法取得 LINE 用戶資料");
        }

        JsonNode json = objectMapper.readTree(response.body());
        LineUserProfile profile = new LineUserProfile(
                json.get("userId").asText(),
                json.has("displayName") ? json.get("displayName").asText() : null,
                json.has("pictureUrl") ? json.get("pictureUrl").asText() : null,
                json.has("email") ? json.get("email").asText() : null
        );

        log.info("[LINE Profile] 成功取得用戶資料: userId={}, displayName={}", profile.userId(), profile.displayName());
        return profile;

    }

    /**
     * 查詢 LINE 用戶是否已註冊
     */
    public Optional<Users> findExistingUser(String lineUserId) {
        return userRepository.findByLineUserId(lineUserId);
    }

    /**
     * 為已存在的用戶產生 JWT Token
     */
    public JwtAuthenticationResponse loginExistingUser(Users user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        JwtAuthenticationResponse response = new JwtAuthenticationResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole().name());

        log.info("LINE OAuth 登入成功: username={}, lineUserId={}", user.getUsername(), user.getLineUserId());

        // 綁定角色對應的 Rich Menu
        try {
            lineRichMenuService.assignMenuToUser(user.getLineUserId(), user.getRole());
        } catch (Exception e) {
            log.debug("Rich Menu 綁定失敗（不影響登入）: {}", e.getMessage());
        }

        return response;
    }

    /**
     * 產生臨時 token，用於角色選擇頁面
     */
    public String generatePendingRegistrationToken(LineUserProfile profile) {
        return jwtService.generateRegistrationToken(profile.userId());
    }

    /**
     * 完成 OAuth 註冊（選角色後建帳號）
     */
    @Transactional
    public Users completeRegistration(String registrationToken, String role, String displayName) {
        String lineUserId = jwtService.extractLineUserIdFromRegistrationToken(registrationToken);

        if (userRepository.findByLineUserId(lineUserId).isPresent()) {
            throw new IllegalStateException("此 LINE 帳號已綁定帳號");
        }

        UserRole userRole = "SITTER".equalsIgnoreCase(role) ? UserRole.SITTER : UserRole.CUSTOMER;

        String username = "line_" + lineUserId.substring(1, Math.min(lineUserId.length(), 13));
        String randomPassword = UUID.randomUUID().toString();

        Users user = new Users();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(randomPassword));
        user.setRole(userRole);
        user.setLineUserId(lineUserId);
        Users savedUser = userRepository.save(user);

        String entityName = (displayName != null && !displayName.isBlank()) ? displayName : username;

        if (userRole == UserRole.SITTER) {
            Sitter sitter = new Sitter();
            sitter.setUser(savedUser);
            sitter.setName(entityName);
            sitterRepository.save(sitter);
        } else {
            Customer customer = new Customer();
            customer.setUser(savedUser);
            customer.setName(entityName);
            customerRepository.save(customer);
        }

        log.info("LINE OAuth 註冊成功: username={}, role={}, lineUserId={}", username, userRole, lineUserId);

        // 註冊後綁定角色對應的 Rich Menu
        try {
            lineRichMenuService.assignMenuToUser(lineUserId, userRole);
        } catch (Exception e) {
            log.debug("Rich Menu 綁定失敗（不影響註冊）: {}", e.getMessage());
        }

        return savedUser;
    }

    private void cleanExpiredStates() {
        long now = System.currentTimeMillis();
        stateStore.entrySet().removeIf(e -> (now - e.getValue().timestamp()) > 600_000);
    }
}
