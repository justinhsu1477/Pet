package com.pet.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 認證過濾器
 * 職責:
 * 1. 從 HTTP Header 中提取 JWT Token
 * 2. 驗證 Token 有效性
 * 3. 將用戶信息設置到 Spring Security Context
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. 從 Header 中提取 Token
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2. 提取 JWT Token
            final String jwt = authHeader.substring(7);

            // 3. 提取用戶名
            final String username = jwtService.extractUsername(jwt);

            // 4. 如果用戶名存在且尚未認證
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 5. 加載用戶詳情
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // 6. 驗證 Token
                if (jwtService.validateToken(jwt)) {

                    // 7. 創建認證對象
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        jwtService.extractAuthorities(jwt)
                    );

                    // 8. 設置請求詳情
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 9. 將認證對象設置到 Security Context
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Authenticated user: {} with role: {}",
                        username, jwtService.extractRole(jwt));
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
