package com.example.kafka;

import com.example.dto.exchange.ExchangeRateMessage;
import com.example.metrics.ExchangeMetrics;
import com.example.service.RateStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateListener {

    private final RateStore store;
    private final ExchangeMetrics metrics;

    @KafkaListener(
            topics = "${app.kafka.exchange-topic}",
            groupId = "${app.kafka.exchange-group}",
            containerFactory = "exchangeKafkaListenerContainerFactory"
    )
    public void onRate(ExchangeRateMessage msg) {
        try {
            msg.rates().values().forEach(row ->
                    store.put(new com.example.model.RateRow(row.currency(), row.buy(), row.sell()))
            );

            metrics.markSuccess();

            log.info("Updated rates from Kafka. base={} currencies={}",
                    msg.baseCurrency(),
                    msg.rates().keySet());

        } catch (Exception e) {
            metrics.markFailure();
            log.error("Failed to process exchange rates message: {}", e.toString(), e);
            throw e;
        }
    }
}
