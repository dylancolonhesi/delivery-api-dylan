package com.deliverytech.delivery.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class ExternalServiceHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        // Simulação: substitua por chamada real ao serviço externo
        boolean serviceUp = true; // Exemplo: use um ping HTTP real
        if (serviceUp) {
            return Health.up().withDetail("externalService", "OK").build();
        } else {
            return Health.down().withDetail("externalService", "Indisponível").build();
        }
    }
}
