package com.pet.service;

import com.pet.domain.*;
import com.pet.dto.JwtAuthenticationResponse;
import com.pet.dto.LoginRequestDto;
import com.pet.dto.RefreshTokenRequest;
import com.pet.exception.BusinessException;
import com.pet.exception.ErrorCode;
import com.pet.repository.UserRepository;
import com.pet.security.JwtProperties;
import com.pet.security.JwtService;
import com.pet.security.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 認證服務 - 處理登入、Token 刷新等
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final JwtProperties jwtProperties;

    /**
     * JWT 登入
     */
    @Transactional
    public JwtAuthenticationResponse login(
            LoginRequestDto request,
            HttpServletRequest httpRequest
    ) {
        // 1. 使用 Spring Security 認證
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        // 2. 查找用戶
        Users user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 3. 生成 Tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // 4. 判斷設備類型
        String deviceType = determineDeviceType(httpRequest);
        String deviceInfo = extractDeviceInfo(httpRequest);
        String ipAddress = extractClientIp(httpRequest);

        // 5. 保存 Refresh Token 到資料庫
        refreshTokenService.createRefreshToken(
                refreshToken,
                user,
                deviceType,
                deviceInfo,
                ipAddress
        );

        log.info("User {} logged in successfully from device: {}",
                user.getUsername(), deviceType);

        // 6. 構建響應
        return buildAuthResponse(user, accessToken, refreshToken);
    }

    /**
     * 刷新 Access Token
     */
    @Transactional
    public JwtAuthenticationResponse refreshToken(RefreshTokenRequest request) {
        String refreshTokenValue = request.getRefreshToken();

        // 1. 驗證 Refresh Token (從資料庫)
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenValue);

        // 2. 驗證 JWT 本身
        if (!jwtService.validateToken(refreshTokenValue)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN, "Token 格式無效");
        }

        if (!jwtService.isRefreshToken(refreshTokenValue)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN, "不是有效的 Refresh Token");
        }

        // 3. 獲取用戶
        Users user = refreshToken.getUser();

        // 4. 生成新的 Access Token
        String newAccessToken = jwtService.generateAccessToken(user);

        log.info("Refresh token used for user: {}", user.getUsername());

        // 5. 構建響應 (不返回新的 Refresh Token,沿用舊的)
        return buildAuthResponse(user, newAccessToken, refreshTokenValue);
    }

    /**
     * 登出 - 撤銷 Refresh Token
     */
    @Transactional
    public void logout(String refreshTokenValue) {
        if (refreshTokenValue != null && !refreshTokenValue.isEmpty()) {
            refreshTokenService.revokeRefreshToken(refreshTokenValue);
            log.info("User logged out successfully");
        }
    }

    /**
     * 登出所有設備
     */
    @Transactional
    public void logoutAllDevices(UUID userId) {
        refreshTokenService.revokeAllUserTokens(userId);
        log.info("User {} logged out from all devices", userId);
    }

    /**
     * 舊版認證方法 (保留向後兼容)
     */
    @Deprecated
    public Users authenticate(String username, String password) {
        Users users = userRepository.findByUsername(username).orElse(null);
        if (users == null) {
            return null;
        }

        if (passwordEncoder.matches(password, users.getPassword())) {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(username, password);
            authenticationManager.authenticate(authenticationToken);
            return users;
        } else {
            return null;
        }
    }

    /**
     * 構建認證響應
     */
    private JwtAuthenticationResponse buildAuthResponse(
            Users user,
            String accessToken,
            String refreshToken
    ) {
        // 獲取角色相關信息
        UUID roleId = null;
        String roleName = null;

        if (user.getRole() == UserRole.CUSTOMER && user.getCustomer() != null) {
            Customer customer = user.getCustomer();
            roleId = customer.getId();
            roleName = customer.getName();
        } else if (user.getRole() == UserRole.SITTER && user.getSitter() != null) {
            Sitter sitter = user.getSitter();
            roleId = sitter.getId();
            roleName = sitter.getName();
        } else if (user.getRole() == UserRole.ADMIN) {
            roleId = user.getId();
            roleName = user.getUsername();
        }

        return JwtAuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenExpiration() / 1000) // 轉換為秒
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .roleId(roleId)
                .roleName(roleName)
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }

    /**
     * 判斷設備類型
     */
    private String determineDeviceType(HttpServletRequest request) {
        // 優先使用自定義 Header
        String deviceType = request.getHeader("X-Device-Type");
        if (deviceType != null) {
            return deviceType.toUpperCase();
        }

        // 根據 User-Agent 判斷
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null) {
            String ua = userAgent.toLowerCase();
            if (ua.contains("android") || ua.contains("ios") || ua.contains("mobile")) {
                return "APP";
            }
        }

        return "WEB";
    }

    /**
     * 提取設備信息
     */
    private String extractDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && userAgent.length() > 200) {
            return userAgent.substring(0, 200);
        }
        return userAgent;
    }

    /**
     * 提取客戶端 IP
     */
    private String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果有多個 IP (經過多個代理),取第一個
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        // 限制長度
        if (ip != null && ip.length() > 45) {
            ip = ip.substring(0, 45);
        }
        return ip;
    }
}
