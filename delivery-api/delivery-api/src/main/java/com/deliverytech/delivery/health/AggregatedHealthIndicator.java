package com.deliverytech.delivery.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class AggregatedHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        // Exemplo de agregação manual
        Health.Builder builder = Health.up();
        builder.withDetail("database", "OK");
        builder.withDetail("externalService", "OK");
        builder.withDetail("diskSpace", "OK");
        return builder.build();
    }
}
