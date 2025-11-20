package com.example.integration;

import com.example.dto.NotifyCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationsClient {

    private final KafkaTemplate<String, NotifyCommand> kafkaTemplate;

    @Value("${app.kafka.notifications-topic}")
    private String topic;

    public void notify(UUID userId, String type, String title, String message) {
        String enrichedTitle = "[%s] %s".formatted(type, title);

        NotifyCommand cmd = new NotifyCommand(userId, enrichedTitle, message);

        kafkaTemplate
                .send(topic, userId.toString(), cmd)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("Failed to send notification to Kafka userId={} type={} title={} error={}",
                                userId, type, title, ex.toString(), ex);
                    } else {
                        log.info("Notification sent to Kafka topic={} partition={} offset={} userId={} type={} title={}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset(),
                                userId, type, title);
                    }
                });
    }
}
