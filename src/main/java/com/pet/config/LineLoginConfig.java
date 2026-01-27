package com.pet.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "line.login")
@Getter
@Setter
public class LineLoginConfig {
    private String channelId;
    private String channelSecret;
    private String callbackUrl;

    public boolean isConfigured() {
        return channelId != null && !channelId.isEmpty()
                && channelSecret != null && !channelSecret.isEmpty();
    }
}
