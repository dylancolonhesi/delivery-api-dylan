package com.deliverytech.delivery.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.deliverytech.delivery.dto.request.PedidoRequest;
import com.deliverytech.delivery.model.Pedido;
import com.deliverytech.delivery.model.StatusPedido;

public interface PedidoService {

    Pedido criarPedido(PedidoRequest dto);
    Optional<Pedido> buscarPedidoPorId(Long id);
    List<Pedido> buscarPedidosPorCliente(Long clienteId);
    Pedido atualizarStatusPedido(Long id, StatusPedido status);
    BigDecimal calcularTotalPedido(Long pedidoId);
    BigDecimal calcularTotalSemSalvar(PedidoRequest dto);
    void cancelarPedido(Long id);
}
