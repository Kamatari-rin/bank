package com.example.service;

import com.example.config.BlockerProperties;
import com.example.dto.BlockerDtos.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class BlockerServiceImpl {
    private static final Logger log = LoggerFactory.getLogger(BlockerServiceImpl.class);
    private final BlockerProperties props;

    public CheckResponse check(CheckRequest r) {
        // 0) Технические граничные проверки
        if (r.amount().scale() > 2) {
            return CheckResponse.deny("too_many_fraction_digits", 100);
        }
        if (r.amount().compareTo(BigDecimal.ZERO) <= 0) {
            return CheckResponse.deny("non_positive_amount", 100);
        }
        if (r.amount().compareTo(BigDecimal.valueOf(props.maxAmount())) > 0) {
            return CheckResponse.deny("amount_exceeds_max", 100);
        }

        int score = 0;

        // 1) Ночью крупные суммы — подозрительнее
        int hour = r.timestampUtc().atZone(ZoneOffset.UTC).getHour();
        boolean isNight = inRangeCircular(hour, props.nightStartHour(), props.nightEndHour());
        if (isNight && r.amount().compareTo(BigDecimal.valueOf(props.nightAmountThreshold())) >= 0) {
            score += 60;
        }

        // 2) Случайный шум, чтобы имитировать антифрод
        int rnd = ThreadLocalRandom.current().nextInt(100);
        if (rnd < props.randomBlockPercent()) {
            score += 50; // дотолкнёт к deny
        }

        boolean allowed = score < 70;
        var reason = allowed ? "ok" : "suspicious_operation";
        log.info("blocker.check op={} amount={} night={} rnd={} score={} -> {}",
                r.operation(), r.amount(), isNight, rnd, score, allowed ? "ALLOW" : "DENY");

        return allowed ? CheckResponse.ok() : CheckResponse.deny(reason, score);
    }

    private static boolean inRangeCircular(int h, int start, int end) {
        // [start, end) по кругу 0..24
        if (start == end) return true; // 24/7
        if (start < end) return h >= start && h < end;
        return h >= start || h < end;
    }
}
