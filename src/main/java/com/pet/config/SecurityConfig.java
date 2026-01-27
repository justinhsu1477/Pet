package com.pet.config;

import com.pet.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 啟用 @PreAuthorize、@PostAuthorize 等方法層級權限控管
@RequiredArgsConstructor
public class SecurityConfig {

    // CORS 設定來源（允許哪些網域、Header、Method）
    private final CorsConfigurationSource corsConfigurationSource;

    // 自訂 JWT 驗證 Filter（每次 request 進來都會先驗 JWT）
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 載入使用者資料（從 DB 撈帳號、角色、狀態）
    private final UserDetailsService userDetailsService;

    // 密碼加密與比對（BCrypt 等）
    private final PasswordEncoder passwordEncoder;

    /**
     * 帳號密碼驗證的 Provider
     * 告訴 Spring Security：
     * - 使用哪個 UserDetailsService 取得使用者
     * - 使用哪個 PasswordEncoder 驗證密碼
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * AuthenticationManager
     * 實際負責「觸發驗證流程」
     * 由 Spring Security 依設定自動組裝（包含 AuthenticationProvider）
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Spring Security 的核心設定
     * 定義整個系統的安全規則、Filter 順序、Session 策略
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 啟用 CORS，讓前後端不同 domain 可以呼叫 API
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // 關閉 CSRF（適用於 JWT、無 Session 的 API 系統）
                .csrf(AbstractHttpConfigurer::disable)

                // 不使用 Http Session，每次請求都靠 JWT 驗證
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // API 授權規則
                .authorizeHttpRequests(auth -> auth
                                // 登入、註冊、Refresh Token 等 API 不需要先登入
                                .requestMatchers("/api/auth/**").permitAll()

                                // 健康檢查端點（給 K8s probe / 監控系統用）
                                .requestMatchers("/api/health/**").permitAll()

                                // 行事曆頁面與下載（LINE 訊息連結，不需登入）
                                .requestMatchers("/api/bookings/*/calendar", "/api/bookings/*/calendar/download").permitAll()

                                // H2 Console（僅開發環境用）
//                        .requestMatchers("/h2-console/**").permitAll()

                                // 其他 API 一律需要驗證（必須帶有效 JWT）
                                .anyRequest().authenticated()
                )

                // 指定使用自訂的 AuthenticationProvider（帳密登入用）
                .authenticationProvider(authenticationProvider())

                // JWT 驗證 Filter
                // 放在 UsernamePasswordAuthenticationFilter 之前，
                // 讓每個 request 先驗 JWT，而不是只在登入時驗證
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                // 關閉 frameOptions（主要給 H2 Console 用，避免被 iframe 擋住）
                .headers(headers ->
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
                );

        return http.build();
    }
}
