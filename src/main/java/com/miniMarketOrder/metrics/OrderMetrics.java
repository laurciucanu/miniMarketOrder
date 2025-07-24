package com.miniMarketOrder.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class OrderMetrics {
    private final MeterRegistry meterRegistry;

    public OrderMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void incrementOrderCounter() {
        meterRegistry.counter("orders.total").increment();
    }
}