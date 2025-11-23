package com.example.kafka;

import com.example.dto.NotificationDto;
import com.example.dto.NotifyCommand;
import com.example.store.InMemoryNotificationsStore;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationsListener {

    private final InMemoryNotificationsStore store;
    private final MeterRegistry registry;

    @KafkaListener(
            topics = "${app.notifications.topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onMessage(@Payload NotifyCommand cmd, ConsumerRecord<String, NotifyCommand> record) {
        UUID userId = cmd.userId();
        try {
            log.info("Notification received from Kafka: key={} partition={} offset={} userId={} title={}",
                    record.key(), record.partition(), record.offset(), userId, cmd.title());

            var dto = new NotificationDto(
                    UUID.randomUUID(),
                    Instant.now(),
                    cmd.title(),
                    cmd.message()
            );

            // "Доставка" уведомления — кладём в in-memory store
            store.add(userId, dto);

            recordSuccess(userId);
        } catch (Exception e) {
            log.error("Failed to process notification for user {}: {}", userId, e.toString(), e);
            recordFailure(userId, e.getClass().getSimpleName());
            // даём Kafka возможность ретраить, если настроено
            throw e;
        }
    }

    private void recordSuccess(UUID userId) {
        Counter.builder("notifications.send.success.total")
                .description("Successfully delivered notifications")
                .tag("user", userId.toString()) // здесь у нас есть userId, его и считаем ключом
                .register(registry)
                .increment();
    }

    private void recordFailure(UUID userId, String reason) {
        Counter.builder("notifications.send.failure.total")
                .description("Failed notification deliveries")
                .tag("user", userId != null ? userId.toString() : "unknown")
                .tag("reason", reason != null ? reason : "unknown")
                .register(registry)
                .increment();
    }
}
