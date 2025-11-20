package com.example.kafka;

import com.example.dto.exchange.ExchangeRateMessage;
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

    @KafkaListener(
            topics = "${app.kafka.exchange-topic}",
            groupId = "${app.kafka.exchange-group}",
            containerFactory = "exchangeKafkaListenerContainerFactory"
    )
    public void onRate(ExchangeRateMessage msg) {
        msg.rates().values().forEach(row ->
                store.put(new com.example.model.RateRow(row.currency(), row.buy(), row.sell()))
        );

        log.info("Updated rates from Kafka. base={} currencies={}",
                msg.baseCurrency(),
                msg.rates().keySet());
    }
}
