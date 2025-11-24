package com.example.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class AuthMetrics {

    private final MeterRegistry registry;

    public AuthMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    /**
     * Успешный логин (успешный аутентифицированный запрос).
     * Группировка по логину — через tag "username".
     */
    public void loginSuccess(String username) {
        Counter.builder("auth.login.success.total")
                .description("Successful user logins")
                .tag("username", username)
                .register(registry)
                .increment();
    }

    /**
     * Неуспешный логин (401/403 по защищённому запросу).
     */
    public void loginFailure(String username) {
        Counter.builder("auth.login.failure.total")
                .description("Failed user logins")
                .tag("username", username)
                .register(registry)
                .increment();
    }
}
