package com.deliverytech.delivery.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import com.deliverytech.delivery.model.Pedido;
import com.deliverytech.delivery.model.Restaurante;
import com.deliverytech.delivery.model.StatusPedido;
import com.deliverytech.delivery.service.ClienteService;
import com.deliverytech.delivery.service.PedidoService;
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

    @PostMapping
    public ResponseEntity<PedidoResponse> criarPedido(@Valid @RequestBody PedidoRequest request) {
        Cliente cliente = clienteService.buscarClientePorId(request.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        Restaurante restaurante = restauranteService.buscarRestaurantePorId(request.getRestauranteId())
                .orElseThrow(() -> new RuntimeException("Restaurante não encontrado"));

        Pedido salvo = pedidoService.criarPedido(request);
        
        List<ItemPedidoResponse> itensResp = salvo.getItens() != null ? 
                salvo.getItens().stream()
                    .map(i -> new ItemPedidoResponse(i.getProduto().getId(), i.getProduto().getNome(), i.getQuantidade(), i.getPrecoUnitario()))
                    .collect(Collectors.toList()) :
                new ArrayList<>();

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

    @PostMapping("/calcular")
    public ResponseEntity<BigDecimal> calcularTotal(@Valid @RequestBody PedidoRequest request) {
        logger.debug("Calculando total do pedido para cliente {} e restaurante {}", 
                    request.getClienteId(), request.getRestauranteId());
        try {
            BigDecimal total = pedidoService.calcularTotalSemSalvar(request);
            return ResponseEntity.ok(total);
        } catch (RuntimeException e) {
            logger.error("Erro ao calcular total do pedido: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/cliente/{clienteId}")
    public List<PedidoResponse> listarPedidosPorCliente(@PathVariable Long clienteId) {
        return pedidoService.buscarPedidosPorCliente(clienteId).stream()
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

    @GetMapping("/{id}")
    public List<ItemPedidoResponse> buscarPedidoPorId(@PathVariable Long id) {
        Pedido pedido = pedidoService.buscarPedidoPorId(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        
        return pedido.getItens().stream()
                .map(i -> new ItemPedidoResponse(i.getProduto().getId(), i.getProduto().getNome(), i.getQuantidade(), i.getPrecoUnitario()))
                .collect(Collectors.toList());
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<Void> alterarStatus(@PathVariable Long id, @RequestParam StatusPedido status) {
        pedidoService.atualizarStatusPedido(id, status);
        logger.info("Status do pedido com ID {} alterado para {}", id, status);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelarPedido(@PathVariable Long id) {
        logger.info("Cancelando pedido com ID: {}", id);
        pedidoService.cancelarPedido(id);
        return ResponseEntity.noContent().build();
    }

}
