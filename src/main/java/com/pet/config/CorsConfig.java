package com.pet.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS 設定
 * 允許前端跨來源存取 API
 * 可透過 application.yml 設定:
 * cors:
 *   allowed-origins: http://localhost:3000,http://example.com
 */
@Configuration
public class CorsConfig {

    /**
     * 允許的來源，可用逗號分隔多個
     * 預設允許 localhost:3000
     */
    @Value("${cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 使用 allowedOriginPatterns 統一管理（支援萬用字元，且與 allowCredentials 相容）
        List<String> patterns = new java.util.ArrayList<>(Arrays.asList(allowedOrigins.split(",")));
        // 允許所有 ngrok URL（開發用，避免每次重啟 ngrok 都要改設定）
        patterns.add("https://*.ngrok-free.app");
        patterns.add("https://*.ngrok-free.dev");
        configuration.setAllowedOriginPatterns(patterns);

        // 允許的 HTTP 方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 允許的 Headers
        configuration.setAllowedHeaders(List.of("*"));

        // 允許帶上 Cookie/Session
        configuration.setAllowCredentials(true);

        // 預檢請求快取時間（秒）
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
