package com.example.web;

import com.example.dto.NotificationDto;
import com.example.dto.NotifyCommand;
import com.example.store.InMemoryNotificationsStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationsController {

    private final InMemoryNotificationsStore store;

    // ВНУТРЕННИЙ: пишет уведомление (нужна роль ROLE_notifications-writer)
    @PostMapping("/internal/notify")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void notifyInternal(@RequestBody NotifyCommand cmd) {
        var dto = new NotificationDto(
                UUID.randomUUID(),
                Instant.now(),
                cmd.title(),
                cmd.message()
        );
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        log.info("notifyInternal auth authorities={}", auth.getAuthorities());
        store.add(cmd.userId(), dto);
    }

    // ПУБЛИЧНЫЙ: читает уведомления текущего пользователя
    // Берём subject из JWT надёжно через @AuthenticationPrincipal
    @GetMapping("/public/mine")
    public List<NotificationDto> mine(@AuthenticationPrincipal Jwt jwt) {
        // sub = keycloak user id (UUID)
        UUID kcUserId = UUID.fromString(jwt.getSubject());
        return store.list(kcUserId);
    }
}
