package com.deliverytech.delivery.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.deliverytech.delivery.dto.request.PedidoRequest;
import com.deliverytech.delivery.dto.response.ItemPedidoResponse;
import com.deliverytech.delivery.dto.response.PedidoResponse;
import com.deliverytech.delivery.model.Cliente;
import com.deliverytech.delivery.model.ItemPedido;
import com.deliverytech.delivery.model.Pedido;
import com.deliverytech.delivery.model.Produto;
import com.deliverytech.delivery.model.Restaurante;
import com.deliverytech.delivery.model.StatusPedido;
import com.deliverytech.delivery.service.ClienteService;
import com.deliverytech.delivery.service.PedidoService;
import com.deliverytech.delivery.service.ProdutoService;
import com.deliverytech.delivery.service.RestauranteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {
    private static final Logger logger = LoggerFactory.getLogger(PedidoController.class);
    
    private final PedidoService pedidoService;
    private final ClienteService clienteService;
    private final RestauranteService restauranteService;
    private final ProdutoService produtoService;

    /**
     * Cria um novo pedido no sistema
     * @param request Dados do pedido a ser criado
     * @return ResponseEntity com os dados do pedido criado
     */
    @PostMapping
    public ResponseEntity<PedidoResponse> criarPedido(@Valid @RequestBody PedidoRequest request) {
        Cliente cliente = clienteService.buscarPorId(request.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        Restaurante restaurante = restauranteService.buscarRestaurantePorId(request.getRestauranteId())
                .orElseThrow(() -> new RuntimeException("Restaurante não encontrado"));

        List<ItemPedido> itens = request.getItens().stream().map(item -> {
            Produto produto = produtoService.buscarProdutoPorId(item.getProdutoId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
            return ItemPedido.builder()
                    .produto(produto)
                    .quantidade(item.getQuantidade())
                    .precoUnitario(produto.getPreco())
                    .build();
        }).collect(Collectors.toList());

        BigDecimal total = itens.stream()
                .map(i -> i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .restaurante(restaurante)
                .status(StatusPedido.CRIADO)
                .total(total)
                .enderecoEntrega(request.getEnderecoEntrega())
                .itens(itens)
                .build();

        Pedido salvo = pedidoService.criarPedido(pedido);
        List<ItemPedidoResponse> itensResp = salvo.getItens().stream()
                .map(i -> new ItemPedidoResponse(i.getProduto().getId(), i.getProduto().getNome(), i.getQuantidade(), i.getPrecoUnitario()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new PedidoResponse(
                salvo.getId(),
                cliente.getId(),
                restaurante.getId(),
                salvo.getEnderecoEntrega(),
                salvo.getTotal(),
                salvo.getStatus(),
                salvo.getDataPedido(),
                itensResp
        ));
    }

    /**
     * Lista todos os pedidos de um cliente específico
     * @param clienteId ID do cliente cujos pedidos serão listados
     * @return Lista de pedidos do cliente especificado
     */
    //TODO: AJUSTAR ERRO
    @GetMapping("/cliente/{clienteId}")
    public List<PedidoResponse> listarPedidosPorCliente(@PathVariable Long clienteId) {
        return pedidoService.listarPedidosPorCliente(clienteId).stream()
                .map(p -> {
                    List<ItemPedidoResponse> itensResp = p.getItens().stream()
                            .map(i -> new ItemPedidoResponse(i.getProduto().getId(), i.getProduto().getNome(), i.getQuantidade(), i.getPrecoUnitario()))
                            .collect(Collectors.toList());
                    
                    return new PedidoResponse(
                            p.getId(),
                            p.getCliente().getId(),
                            p.getRestaurante().getId(),
                            p.getEnderecoEntrega(),
                            p.getTotal(),
                            p.getStatus(),
                            p.getDataPedido(),
                            itensResp
                    );
                })
                .collect(Collectors.toList());
    }


    /**
     * Altera o status de um pedido específico
     * @param id ID do pedido que terá o status alterado
     * @param status Novo status do pedido
     * @return ResponseEntity vazio com status 204 (No Content)
     */
    @PutMapping("/status/{id}")
    public ResponseEntity<Void> alterarStatus(@PathVariable Long id, @RequestParam StatusPedido status) {
        pedidoService.atualizarStatus(id, status);
        logger.info("Status do pedido com ID {} alterado para {}", id, status);
        return ResponseEntity.noContent().build();
    }

    /**
     * Cancela um pedido específico
     * @param id ID do pedido a ser cancelado
     * @return ResponseEntity vazio com status 204 (No Content)
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelarPedido(@PathVariable Long id) {
        logger.info("Cancelando pedido com ID: {}", id);
        pedidoService.cancelarPedido(id);
        return ResponseEntity.noContent().build();
    }

}
