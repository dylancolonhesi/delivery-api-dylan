package com.deliverytech.delivery.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deliverytech.delivery.dto.request.PedidoRequest;
import com.deliverytech.delivery.model.Cliente;
import com.deliverytech.delivery.model.ItemPedido;
import com.deliverytech.delivery.model.Pedido;
import com.deliverytech.delivery.model.Produto;
import com.deliverytech.delivery.model.Restaurante;
import com.deliverytech.delivery.model.StatusPedido;
import com.deliverytech.delivery.repository.ClienteRepository;
import com.deliverytech.delivery.repository.PedidoRepository;
import com.deliverytech.delivery.repository.ProdutoRepository;
import com.deliverytech.delivery.repository.RestauranteRepository;
import com.deliverytech.delivery.service.PedidoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final RestauranteRepository restauranteRepository;
    private final ProdutoRepository produtoRepository;

    @Override
    public Pedido criarPedido(PedidoRequest dto) {
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        
        Restaurante restaurante = restauranteRepository.findById(dto.getRestauranteId())
                .orElseThrow(() -> new RuntimeException("Restaurante não encontrado"));

        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .restaurante(restaurante)
                .enderecoEntrega(dto.getEnderecoEntrega())
                .status(StatusPedido.CRIADO)
                .itens(new ArrayList<>())
                .build();

        // Criar os itens do pedido
        List<ItemPedido> itens = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (var itemRequest : dto.getItens()) {
            Produto produto = produtoRepository.findById(itemRequest.getProdutoId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + itemRequest.getProdutoId()));
            
            ItemPedido item = ItemPedido.builder()
                    .pedido(pedido)
                    .produto(produto)
                    .quantidade(itemRequest.getQuantidade())
                    .precoUnitario(produto.getPreco())
                    .build();
            
            itens.add(item);
            total = total.add(produto.getPreco().multiply(BigDecimal.valueOf(itemRequest.getQuantidade())));
        }

        pedido.setItens(itens);
        pedido.setTotal(total);
        
        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pedido> buscarPedidoPorId(Long id) {
        return pedidoRepository.findByIdWithItens(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> buscarPedidosPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteIdWithItens(clienteId);
    }

    @Override
    public Pedido atualizarStatusPedido(Long id, StatusPedido status) {
        return pedidoRepository.findById(id)
                .map(pedido -> {
                    if (pedido.getStatus() == StatusPedido.CANCELADO) {
                        throw new RuntimeException("Não é possível alterar status de pedido cancelado");
                    }
                    pedido.setStatus(status);
                    return pedidoRepository.save(pedido);
                }).orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
    }

    @Override
    public BigDecimal calcularTotalPedido(Long pedidoId) {
        return pedidoRepository.findById(pedidoId)
                .map(Pedido::getTotal)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
    }

    @Override
    public BigDecimal calcularTotalSemSalvar(PedidoRequest dto) {
        clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        
        restauranteRepository.findById(dto.getRestauranteId())
                .orElseThrow(() -> new RuntimeException("Restaurante não encontrado"));

        return dto.getItens().stream()
                .map(item -> {
                    Produto produto = produtoRepository.findById(item.getProdutoId())
                            .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + item.getProdutoId()));
                    return produto.getPreco().multiply(BigDecimal.valueOf(item.getQuantidade()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public void cancelarPedido(Long id) {
        pedidoRepository.findById(id)
                .map(pedido -> {
                    if (pedido.getStatus() == StatusPedido.ENTREGUE) {
                        throw new RuntimeException("Não é possível cancelar pedido já entregue");
                    }
                    pedido.setStatus(StatusPedido.CANCELADO);
                    return pedidoRepository.save(pedido);
                }).orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
    }
}
