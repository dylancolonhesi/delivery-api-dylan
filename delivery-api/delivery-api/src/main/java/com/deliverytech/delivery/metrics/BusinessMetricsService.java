package com.deliverytech.delivery.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class BusinessMetricsService {
    private final MeterRegistry meterRegistry;

    // Contador de pedidos por status
    private final Map<String, Counter> pedidosPorStatus = new ConcurrentHashMap<>();

    // Timer para latência de operações críticas
    private Timer operacaoCriticaTimer;

    // Gauge para número de usuários ativos
    private final AtomicInteger usuariosAtivos = new AtomicInteger(0);

    // Receita por hora
    private final AtomicReference<Double> receitaPorHora = new AtomicReference<>(0.0);

    // Produtos vendidos
    private final AtomicInteger produtosVendidos = new AtomicInteger(0);

    public BusinessMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @jakarta.annotation.PostConstruct
    public void initMetrics() {
        // Timer para latência
        operacaoCriticaTimer = Timer.builder("app.operacao.critica.latencia")
                .description("Latência de operações críticas")
                .register(meterRegistry);

        // Gauge para usuários ativos
        Gauge.builder("app.usuarios.ativos", usuariosAtivos, AtomicInteger::get)
                .description("Número de usuários ativos")
                .register(meterRegistry);

        // Gauge para receita por hora
        Gauge.builder("app.receita.por.hora", receitaPorHora, AtomicReference::get)
                .description("Receita por hora")
                .register(meterRegistry);

        // Gauge para produtos vendidos
        Gauge.builder("app.produtos.vendidos", produtosVendidos, AtomicInteger::get)
                .description("Produtos vendidos")
                .register(meterRegistry);
    }

    // Incrementa contador de pedidos por status
    public void incrementarPedidosPorStatus(String status) {
        pedidosPorStatus.computeIfAbsent(status, s -> Counter.builder("app.pedidos.status")
                .description("Pedidos processados por status")
                .tag("status", s)
                .register(meterRegistry)).increment();
    }

    // Registra latência de operação crítica
    public void registrarLatenciaOperacaoCritica(Runnable operacao) {
        operacaoCriticaTimer.record(operacao);
    }

    // Atualiza número de usuários ativos
    public void atualizarUsuariosAtivos(int quantidade) {
        usuariosAtivos.set(quantidade);
    }

    // Atualiza receita por hora
    public void atualizarReceitaPorHora(double valor) {
        receitaPorHora.set(valor);
    }

    // Atualiza produtos vendidos
    public void atualizarProdutosVendidos(int quantidade) {
        produtosVendidos.set(quantidade);
    }
}
