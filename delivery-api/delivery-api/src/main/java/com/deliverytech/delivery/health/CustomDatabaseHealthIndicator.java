package com.deliverytech.delivery.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class CustomDatabaseHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        // Simulação: substitua por lógica real de verificação do banco
        boolean dbUp = true; // Exemplo: use um ping real ao banco
        if (dbUp) {
            return Health.up().withDetail("database", "OK").build();
        } else {
            return Health.down().withDetail("database", "Falha de conexão").build();
        }
    }
}
