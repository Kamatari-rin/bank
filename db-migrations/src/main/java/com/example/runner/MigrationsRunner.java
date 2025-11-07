package com.example.runner;

import com.example.config.DatabaseMigrationProperties;
import com.example.service.GenericMigrationService;
import com.example.service.MigrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Slf4j
@Component
@Order(0)
public class MigrationsRunner implements CommandLineRunner {

    private final DatabaseMigrationProperties properties;

    public MigrationsRunner(DatabaseMigrationProperties properties) {
        this.properties = properties;
    }

    @Override
    public void run(String... args) {
        log.info("Starting DB migrations for {} datasources", properties.getDatabases().size());
        List<MigrationService> migrationServices = new ArrayList<>();
        properties.getDatabases().forEach((dbName, dbProperties) -> {
            migrationServices.add(new com.example.service.GenericMigrationService(dbName, dbProperties));
        });
        migrationServices.forEach(MigrationService::runMigrations);
        log.info("All migrations finished.");
    }
}