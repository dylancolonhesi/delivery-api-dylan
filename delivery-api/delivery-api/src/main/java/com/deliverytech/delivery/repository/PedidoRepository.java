package com.deliverytech.delivery.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.deliverytech.delivery.model.Pedido;
import com.deliverytech.delivery.model.StatusPedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    
    // Buscar pedidos por cliente
    List<Pedido> findByClienteId(Long clienteId);
    
    // Buscar pedidos por cliente com itens carregados (evita LazyInitializationException)
    @Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.itens i LEFT JOIN FETCH i.produto WHERE p.cliente.id = :clienteId ORDER BY p.dataPedido DESC")
    List<Pedido> findByClienteIdWithItens(@Param("clienteId") Long clienteId);
    
    // Buscar pedido por ID com itens carregados
    @Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.itens i LEFT JOIN FETCH i.produto WHERE p.id = :id")
    Optional<Pedido> findByIdWithItens(@Param("id") Long id);
    
    // Buscar pedidos por restaurante
    List<Pedido> findByRestauranteId(Long restauranteId);
    
    // Buscar pedidos por status
    List<Pedido> findByStatus(StatusPedido status);
    
    // Top 10 pedidos mais recentes
    List<Pedido> findTop10ByOrderByDataPedidoDesc();
    
    // Buscar pedidos entre datas
    List<Pedido> findByDataPedidoBetween(LocalDateTime inicio, LocalDateTime fim);
    
    // === CONSULTAS CUSTOMIZADAS COM @Query ===
    
    /**
     * Total de vendas por restaurante
     */
    @Query("SELECT r.nome as restaurante, SUM(p.total) as totalVendas, COUNT(p) as quantidadePedidos " +
           "FROM Pedido p JOIN p.restaurante r " +
           "WHERE p.status IN ('ENTREGUE', 'CONFIRMADO') " +
           "GROUP BY r.id, r.nome " +
           "ORDER BY totalVendas DESC")
    List<Object[]> findTotalVendasPorRestaurante();
    
    /**
     * Pedidos com valor acima de X
     */
    @Query("SELECT p FROM Pedido p WHERE p.total > :valor ORDER BY p.total DESC")
    List<Pedido> findPedidosComValorAcimaDe(@Param("valor") BigDecimal valor);
    
    /**
     * Relatório por período e status
     */
    @Query("SELECT p.status as status, COUNT(p) as quantidade, SUM(p.total) as valorTotal, AVG(p.total) as valorMedio " +
           "FROM Pedido p " +
           "WHERE p.dataPedido BETWEEN :dataInicio AND :dataFim " +
           "AND p.status = :status " +
           "GROUP BY p.status")
    List<Object[]> findRelatorioPorPeriodoEStatus(
            @Param("dataInicio") LocalDateTime dataInicio, 
            @Param("dataFim") LocalDateTime dataFim, 
            @Param("status") StatusPedido status);
    
    /**
     * Faturamento total por período
     */
    @Query("SELECT SUM(p.total) FROM Pedido p " +
           "WHERE p.dataPedido BETWEEN :dataInicio AND :dataFim " +
           "AND p.status IN ('ENTREGUE', 'CONFIRMADO')")
    BigDecimal findFaturamentoPorPeriodo(@Param("dataInicio") LocalDateTime dataInicio, @Param("dataFim") LocalDateTime dataFim);
    
    /**
     * Pedidos por cliente com valor total
     */
    @Query("SELECT c.nome as cliente, COUNT(p) as totalPedidos, SUM(p.total) as valorTotal " +
           "FROM Pedido p JOIN p.cliente c " +
           "GROUP BY c.id, c.nome " +
           "ORDER BY totalPedidos DESC")
    List<Object[]> findRankingClientesPorPedidos();
    
    // === CONSULTAS NATIVAS (opcional) ===
    
    /**
     * Ranking de clientes por número de pedidos (Native Query)
     */
    @Query(value = "SELECT c.nome, COUNT(p.id) as total_pedidos, SUM(p.total) as valor_total " +
                   "FROM pedido p " +
                   "INNER JOIN cliente c ON p.cliente_id = c.id " +
                   "GROUP BY c.id, c.nome " +
                   "ORDER BY total_pedidos DESC " +
                   "LIMIT 10", nativeQuery = true)
    List<Object[]> findTop10ClientesPorPedidosNative();
    
    /**
     * Faturamento por categoria de restaurante (Native Query)
     */
    @Query(value = "SELECT r.categoria, SUM(p.total) as faturamento_total, COUNT(p.id) as total_pedidos " +
                   "FROM pedido p " +
                   "INNER JOIN restaurante r ON p.restaurante_id = r.id " +
                   "WHERE p.status IN ('ENTREGUE', 'CONFIRMADO') " +
                   "GROUP BY r.categoria " +
                   "ORDER BY faturamento_total DESC", nativeQuery = true)
    List<Object[]> findFaturamentoPorCategoriaNative();
    
    // Query customizada para relatório de pedidos
    @Query("SELECT p FROM Pedido p ORDER BY p.dataPedido DESC")
    List<Pedido> relatorioPedidos();
}