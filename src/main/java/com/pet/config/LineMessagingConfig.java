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

    private String baseUrl;
    private String frontendUrl;

    public boolean hasBaseUrl() {
        return baseUrl != null && !baseUrl.isEmpty();
    }


    public boolean isConfigured() {
        return channelToken != null && !channelToken.isEmpty()
            && demoUserId != null && !demoUserId.isEmpty();
    }

    // Explicit getters for Kotlin interop
    public String getChannelToken() { return channelToken; }
    public String getChannelSecret() { return channelSecret; }
    public String getDemoUserId() { return demoUserId; }
    public String getBaseUrl() { return baseUrl; }
    public String getFrontendUrl() { return frontendUrl; }
}
