package com.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public record KafkaProps(
        String bootstrapServers,
        String notificationsTopic,
        String notificationsGroupId
) {}
