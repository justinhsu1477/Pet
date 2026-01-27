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
        // 設定允許的來源（從設定檔讀取）
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);

        // 允許的 HTTP 方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 允許的 Headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 允許帶上 Cookie/Session
        configuration.setAllowCredentials(true);

        // 預檢請求快取時間（秒）
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
