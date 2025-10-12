package com.example.service;

import com.example.model.RateRow;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateStore {

    private final ConcurrentHashMap<String, RateRow> rates = new ConcurrentHashMap<>();

    public RateStore() {
        // дефолтные значения (можно поправить позже)
        put(new RateRow("RUB", BigDecimal.ONE, BigDecimal.ONE));
        put(new RateRow("USD", new BigDecimal("89.50"), new BigDecimal("90.50")));
        put(new RateRow("CNY", new BigDecimal("11.80"), new BigDecimal("12.20")));
    }

    public Map<String, RateRow> snapshot() {
        return Map.copyOf(rates);
    }

    public void put(RateRow row) {
        rates.put(row.currency().toUpperCase(), row);
    }
}
