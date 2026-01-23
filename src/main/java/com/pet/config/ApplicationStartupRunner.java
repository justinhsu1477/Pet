package com.pet.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.TimeZone;

/**
 * 應用程式啟動時記錄環境資訊
 */
@Slf4j
@Component
public class ApplicationStartupRunner implements CommandLineRunner {

    private final Environment environment;

    public ApplicationStartupRunner(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(String... args) throws Exception {
        logApplicationInfo();
    }

    private void logApplicationInfo() {
        String separator = "=".repeat(80);

        log.info("\n{}\n", separator);
        log.info("🚀 Application Started Successfully");
        log.info("{}\n", separator);

        // 應用程式基本資訊
        logSection("Application Information");
        log.info("Application Name    : {}", environment.getProperty("spring.application.name", "Pet System"));
        log.info("Startup Time        : {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        log.info("Time Zone           : {}", TimeZone.getDefault().getID());

        // 環境資訊
        logSection("Environment Configuration");
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length == 0) {
            log.info("Active Profile      : default");
        } else {
            log.info("Active Profile      : {}", Arrays.toString(activeProfiles));
        }

        // 伺服器資訊
        logSection("Server Information");
        String serverPort = environment.getProperty("server.port", "8080");
        String contextPath = environment.getProperty("server.servlet.context-path", "/");
        log.info("Server Port         : {}", serverPort);
        log.info("Context Path        : {}", contextPath);

        try {
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            String hostName = InetAddress.getLocalHost().getHostName();
            log.info("Host Name           : {}", hostName);
            log.info("Host Address        : {}", hostAddress);
            log.info("Local Access URL    : http://localhost:{}{}", serverPort, contextPath);
            log.info("Network Access URL  : http://{}:{}{}", hostAddress, serverPort, contextPath);
        } catch (UnknownHostException e) {
            log.warn("Unable to determine host information: {}", e.getMessage());
        }

        // 資料庫資訊
        logSection("Database Configuration");
        logDatabaseInfo("Primary", "spring.datasource.primary");
        logDatabaseInfo("Log", "spring.datasource.log");

        // Hibernate 設定
        String ddlAuto = environment.getProperty("spring.jpa.hibernate.ddl-auto");
        if (ddlAuto != null) {
            log.info("Hibernate DDL Auto  : {}", ddlAuto);
        }

        // H2 Console (如果啟用)
        String h2ConsoleEnabled = environment.getProperty("spring.h2.console.enabled");
        if ("true".equalsIgnoreCase(h2ConsoleEnabled)) {
            String h2ConsolePath = environment.getProperty("spring.h2.console.path", "/h2-console");
            log.info("H2 Console Enabled  : true");
            log.info("H2 Console URL      : http://localhost:{}{}", serverPort, h2ConsolePath);
        }

        log.info("\n{}", separator);
        log.info("✅ Application is ready to accept requests");
        log.info("{}\n", separator);
    }

    private void logSection(String title) {
        log.info("\n--- {} ---", title);
    }

    private void logDatabaseInfo(String name, String prefix) {
        String url = environment.getProperty(prefix + ".url");
        String driverClass = environment.getProperty(prefix + ".driver-class-name");
        String username = environment.getProperty(prefix + ".username");

        if (url != null) {
            log.info("{} DB URL      : {}", String.format("%-8s", name), url);
            if (driverClass != null) {
                log.info("{} DB Driver   : {}", String.format("%-8s", name), driverClass);
            }
            if (username != null) {
                log.info("{} DB Username : {}", String.format("%-8s", name), username);
            }
        }
    }
}
