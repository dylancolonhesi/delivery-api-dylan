package com.deliverytech.delivery.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.deliverytech.delivery.model.Pedido;
import com.deliverytech.delivery.model.StatusPedido;
import com.deliverytech.delivery.repository.PedidoRepository;
import com.deliverytech.delivery.service.PedidoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;

    @Override
    public Pedido criarPedido(Pedido pedido) {
        pedido.setStatus(StatusPedido.CRIADO);
        return pedidoRepository.save(pedido);
    }

    @Override
    public Optional<Pedido> buscarPedidoPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    @Override
    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    @Override
    public List<Pedido> listarPedidosPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId);
    }

    @Override
    public List<Pedido> listarPedidosPorRestaurante(Long restauranteId) {
        return pedidoRepository.findByRestauranteId(restauranteId);
    }

    @Override
    public Pedido atualizarStatus(Long id, StatusPedido status) {
        return pedidoRepository.findById(id)
                .map(p -> {
                    p.setStatus(status);
                    return pedidoRepository.save(p);
                }).orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
    }

    @Override
    public void cancelarPedido(Long id) {
        pedidoRepository.findById(id)
                .map(p -> {
                    p.setStatus(StatusPedido.CANCELADO);
                    return pedidoRepository.save(p);
                }).orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
    }
}
