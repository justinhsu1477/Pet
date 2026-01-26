package com.pet.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

/**
 * Log Database SQL Initializer
 * 在 QAS 環境中執行 schema-mssql-log.sql 來初始化 Log 資料庫
 * 只在 qas profile 啟用時執行
 */
@Configuration
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "qas")
public class LogDatabaseInitializer {

    @Bean
    public DataSourceInitializer logDataSourceInitializer(@Qualifier("logDataSource") DataSource logDataSource) {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(logDataSource);

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("db/schema-mssql-log.sql"));
        populator.setContinueOnError(true);

        initializer.setDatabasePopulator(populator);
        return initializer;
    }
}
