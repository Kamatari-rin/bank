package com.example.store;

import com.example.dto.NotificationDto;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryNotificationsStore {

    private static final int MAX_PER_USER = 100;

    private final ConcurrentHashMap<UUID, Deque<NotificationDto>> data = new ConcurrentHashMap<>();

    public void add(UUID userId, NotificationDto n) {
        var q = data.computeIfAbsent(userId, __ -> new ArrayDeque<>());
        synchronized (q) {
            q.addFirst(n);
            while (q.size() > MAX_PER_USER) q.removeLast();
        }
    }

    public List<NotificationDto> list(UUID userId) {
        var q = data.get(userId);
        if (q == null) return List.of();
        synchronized (q) {
            return new ArrayList<>(q); // копия в актуальном порядке (новые сверху)
        }
    }
}
