package com.pet.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "file.storage")
@Getter
@Setter
public class FileStorageConfig {

    /**
     * 檔案儲存根目錄
     */
    private String basePath = "./uploads/photos";

    /**
     * 最大檔案大小（bytes），預設 10MB
     */
    private long maxFileSize = 10485760;

    // Explicit getters for Kotlin interop
    public String getBasePath() { return basePath; }
    public long getMaxFileSize() { return maxFileSize; }
}
