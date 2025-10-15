package com.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "db.migrations")
@Data
public class DatabaseMigrationProperties {
    private Map<String, MigrationProperties> databases = new HashMap<>();

    @Data
    public static class MigrationProperties {
        private String url;
        private String username;
        private String password;
        private String changelog;
    }
}