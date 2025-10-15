package com.example;

import com.example.config.BlockerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(BlockerProperties.class)
public class BlockerService {
    public static void main(String[] args) {
        SpringApplication.run(BlockerService.class, args);
    }
}