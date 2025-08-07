package com.deliverytech.delivery.service;

import java.util.List;
import java.util.Optional;

import com.deliverytech.delivery.model.Pedido;
import com.deliverytech.delivery.model.StatusPedido;

public interface PedidoService {
    Pedido criarPedido(Pedido pedido);
    Optional<Pedido> buscarPedidoPorId(Long id);
    List<Pedido> listarPedidos();
    List<Pedido> listarPedidosPorCliente(Long clienteId);
    List<Pedido> listarPedidosPorRestaurante(Long restauranteId);
    Pedido atualizarStatus(Long id, StatusPedido status);
    void cancelarPedido(Long id);
}
