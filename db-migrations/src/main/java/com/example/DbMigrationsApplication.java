package com.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class DbMigrationsApplication implements CommandLineRunner {

    private final ApplicationContext context;

    public DbMigrationsApplication(ApplicationContext context) {
        this.context = context;
    }

    public static void main(String[] args) {
        SpringApplication.run(DbMigrationsApplication.class, args);
    }

    @Override
    public void run(String... args) {
        SpringApplication.exit(context, () -> 0);
    }
}