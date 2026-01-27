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

    public boolean hasBaseUrl() {
        return baseUrl != null && !baseUrl.isEmpty();
    }


    public boolean isConfigured() {
        return channelToken != null && !channelToken.isEmpty()
            && demoUserId != null && !demoUserId.isEmpty();
    }

    public boolean hasBaseUrl() {
        return baseUrl != null && !baseUrl.isEmpty();
    }
}
