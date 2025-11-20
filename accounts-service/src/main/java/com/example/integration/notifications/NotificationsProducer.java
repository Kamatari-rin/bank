package com.example.integration.notifications;

import com.example.dto.NotifyCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationsProducer {

    private final KafkaTemplate<String, NotifyCommand> kafkaTemplate;

    @Value("${app.kafka.notifications-topic}")
    private String topic;

    public void send(UUID userKeycloakId, String title, String message) {
        NotifyCommand cmd = new NotifyCommand(userKeycloakId, title, message);
        kafkaTemplate
                .send(topic, userKeycloakId.toString(), cmd)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("Failed to send notification to Kafka userId={} title={}: {}",
                                userKeycloakId, title, ex.getMessage(), ex);
                    } else {
                        log.debug("Notification sent to Kafka topic={} partition={} offset={} userId={} title={}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset(),
                                userKeycloakId, title);
                    }
                });
    }
}
