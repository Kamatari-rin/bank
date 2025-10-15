package com.example.service;

import com.example.config.DatabaseMigrationProperties;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.core.io.DefaultResourceLoader;

import javax.sql.DataSource;

public class GenericMigrationService implements MigrationService {

    private final String dbName;
    private final DatabaseMigrationProperties.MigrationProperties properties;

    public GenericMigrationService(String dbName, DatabaseMigrationProperties.MigrationProperties properties) {
        this.dbName = dbName;
        this.properties = properties;
    }

    @Override
    public void runMigrations() {
        try {
            DataSource dataSource = DataSourceBuilder.create()
                    .url(properties.getUrl())
                    .username(properties.getUsername())
                    .password(properties.getPassword())
                    .driverClassName("org.postgresql.Driver")
                    .build();

            SpringLiquibase liquibase = new SpringLiquibase();
            liquibase.setDataSource(dataSource);
            liquibase.setChangeLog(properties.getChangelog());
            liquibase.setResourceLoader(new DefaultResourceLoader());
            liquibase.afterPropertiesSet();

            System.out.println(dbName + " DB migrations applied successfully.");
        } catch (Exception e) {
            throw new RuntimeException("Error applying " + dbName + " DB migrations", e);
        }
    }
}