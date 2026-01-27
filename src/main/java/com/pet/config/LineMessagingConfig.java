package com.pet.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "line.messaging")
@Getter
@Setter
public class LineMessagingConfig {

    private String channelToken;
    private String channelSecret;
    private String demoUserId;
    private boolean enabled = true;

    /**
     * 系統的公開 Base URL（用於產生行事曆連結）
     * 例如：http://192.168.1.100:8080 或 https://abc123.ngrok.io
     * 若未設定，LINE 訊息中將不包含行事曆連結
     */
    private String baseUrl;

    public boolean isConfigured() {
        return channelToken != null && !channelToken.isEmpty()
            && demoUserId != null && !demoUserId.isEmpty();
    }

    public boolean hasBaseUrl() {
        return baseUrl != null && !baseUrl.isEmpty();
    }
}
