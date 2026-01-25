package com.pet.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.domain.UserRole;
import com.pet.dto.response.ApiResponse;
import com.pet.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * Web 端訪問控制攔截器
 * 需求:
 * 1. Web 端登入:只允許 ADMIN 訪問
 * 2. APP 端登入:允許所有角色訪問
 * 3. 透過 User-Agent 或自定義 Header 判斷來源
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebAccessControlInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;
    private final JwtService jwtService;

    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler
    ) throws Exception {

        // 1. 獲取認證信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            // 未認證,放行 (由 Spring Security 處理)
            return true;
        }

        // 2. 判斷請求來源 (WEB or APP)
        String deviceType = determineDeviceType(request);

        // 3. 如果是 Web 端請求,檢查角色
        if ("WEB".equals(deviceType)) {
            UserRole userRole = extractUserRole(request);

            if (userRole != null && userRole != UserRole.ADMIN) {
                // Web 端非 ADMIN 用戶,返回 403
                log.warn("Web access denied for user with role: {}", userRole);
                sendForbiddenResponse(response);
                return false;
            }
        }

        // APP 端或 ADMIN 用戶,放行
        return true;
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
            // 簡單判斷:包含 Android/iOS 視為 APP
            if (ua.contains("android") || ua.contains("ios") || ua.contains("mobile")) {
                return "APP";
            }
        }

        // 默認為 WEB
        return "WEB";
    }

    /**
     * 從請求中提取用戶角色
     */
    private UserRole extractUserRole(HttpServletRequest request) {
        // 從 JWT Token 中提取角色
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String jwt = authHeader.substring(7);
                return jwtService.extractRole(jwt);
            } catch (Exception e) {
                log.error("Failed to extract role from token: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 發送 403 禁止訪問響應
     */
    private void sendForbiddenResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Void> apiResponse = ApiResponse.error(
            ErrorCode.WEB_ACCESS_RESTRICTED.getMessage()
        );

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
