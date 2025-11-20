package com.example.kafka;

import com.example.dto.NotificationDto;
import com.example.dto.NotifyCommand;
import com.example.store.InMemoryNotificationsStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationsListener {

    private final InMemoryNotificationsStore store;

    @KafkaListener(
            topics = "#{'${app.kafka.notifications-topic}'}",
            groupId = "#{'${app.kafka.notifications-group-id}'}",
            containerFactory = "notifyKafkaListenerContainerFactory"
    )
    public void onNotify(NotifyCommand cmd) {
        log.info("Received notification from Kafka for userId={} title={}", cmd.userId(), cmd.title());

        NotificationDto dto = new NotificationDto(
                UUID.randomUUID(),
                Instant.now(),
                cmd.title(),
                cmd.message()
        );

        store.add(cmd.userId(), dto);
    }
}
