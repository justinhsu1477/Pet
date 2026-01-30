package com.pet.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Log DataSource 配置
 * 專門存放 Booking Log，用於報表/分析
 * 掃描 com.pet.log.domain 和 com.pet.log.repository
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {"com.pet.log.repository"},
        entityManagerFactoryRef = "logEntityManagerFactory",
        transactionManagerRef = "logTransactionManager"
)
public class LogDataSourceConfig {

    @Value("${spring.jpa.log.hibernate.ddl-auto:update}")
    private String ddlAuto;

    @Value("${spring.jpa.log.database-platform:#{null}}")
    private String dialect;

    @Bean
    @ConfigurationProperties("spring.datasource.log")
    public DataSourceProperties logDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource logDataSource() {
        return logDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean logEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("logDataSource") DataSource dataSource) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", ddlAuto);
        if (dialect != null) {
            properties.put("hibernate.dialect", dialect);
        }

        return builder
                .dataSource(dataSource)
                .packages("com.pet.log.domain")
                .persistenceUnit("log")
                .properties(properties)
                .build();
    }

    @Bean
    public PlatformTransactionManager logTransactionManager(
            @Qualifier("logEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
