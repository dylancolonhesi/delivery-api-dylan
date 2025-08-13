package com.deliverytech.delivery.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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
import com.deliverytech.delivery.dto.response.ApiResponseDTO;
import com.deliverytech.delivery.dto.response.ItemPedidoResponse;
import com.deliverytech.delivery.dto.response.PedidoResponse;
import com.deliverytech.delivery.model.Cliente;
import com.deliverytech.delivery.model.Pedido;
import com.deliverytech.delivery.model.Restaurante;
import com.deliverytech.delivery.model.StatusPedido;
import com.deliverytech.delivery.service.ClienteService;
import com.deliverytech.delivery.service.PedidoService;
import com.deliverytech.delivery.service.RestauranteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Operações relacionadas aos pedidos")
public class PedidoController {
    private static final Logger logger = LoggerFactory.getLogger(PedidoController.class);
    
    private static final String MSG_PEDIDO_CRIADO = "Pedido criado com sucesso";
    private static final String MSG_TOTAL_CALCULADO = "Total do pedido calculado com sucesso";
    private static final String MSG_PEDIDO_ENCONTRADO = "Pedido encontrado";
    private static final String MSG_PEDIDO_CANCELADO = "Pedido cancelado com sucesso";
    private static final String MSG_CLIENTE_NAO_ENCONTRADO = "Cliente não encontrado";
    private static final String MSG_RESTAURANTE_NAO_ENCONTRADO = "Restaurante não encontrado";
    private static final String MSG_PEDIDO_NAO_ENCONTRADO = "Pedido não encontrado";
    
    private final PedidoService pedidoService;
    private final ClienteService clienteService;
    private final RestauranteService restauranteService;

    @PostMapping
    @Operation(summary = "Criar pedido",
               description = "Cria um novo pedido no sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Cliente ou restaurante não encontrado")
    })
    public ResponseEntity<ApiResponseDTO<PedidoResponse>> criarPedido(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados do pedido a ser criado",
                required = true
            ) PedidoRequest request) {
        Cliente cliente = clienteService.buscarClientePorId(request.getClienteId())
                .orElseThrow(() -> new RuntimeException(MSG_CLIENTE_NAO_ENCONTRADO));
        Restaurante restaurante = restauranteService.buscarRestaurantePorId(request.getRestauranteId())
                .orElseThrow(() -> new RuntimeException(MSG_RESTAURANTE_NAO_ENCONTRADO));

        Pedido salvo = pedidoService.criarPedido(request);
        
        List<ItemPedidoResponse> itensResp = salvo.getItens() != null ? 
                salvo.getItens().stream()
                    .map(i -> new ItemPedidoResponse(i.getProduto().getId(), i.getProduto().getNome(), i.getQuantidade(), i.getPrecoUnitario()))
                    .collect(Collectors.toList()) :
                new ArrayList<>();

        PedidoResponse response = new PedidoResponse(
                salvo.getId(),
                cliente.getId(),
                restaurante.getId(),
                salvo.getEnderecoEntrega(),
                salvo.getTotal(),
                salvo.getStatus(),
                salvo.getDataPedido(),
                itensResp
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(response, MSG_PEDIDO_CRIADO));
    }

    @PostMapping("/calcular")
    @Operation(summary = "Calcular total do pedido",
               description = "Calcula o total de um pedido sem salvá-lo no sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Total calculado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos para cálculo")
    })
    public ResponseEntity<ApiResponseDTO<BigDecimal>> calcularTotal(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados do pedido para cálculo",
                required = true
            ) PedidoRequest request) {
        logger.debug("Calculando total do pedido para cliente {} e restaurante {}", 
                    request.getClienteId(), request.getRestauranteId());
        try {
            BigDecimal total = pedidoService.calcularTotalSemSalvar(request);
            return ResponseEntity.ok(ApiResponseDTO.success(total, MSG_TOTAL_CALCULADO));
        } catch (RuntimeException e) {
            logger.error("Erro ao calcular total do pedido: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponseDTO.error(e.getMessage()));
        }
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Listar pedidos por cliente",
               description = "Lista todos os pedidos de um cliente específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedidos do cliente listados com sucesso"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<ApiResponseDTO<List<PedidoResponse>>> listarPedidosPorCliente(
            @PathVariable @Parameter(description = "ID do cliente") Long clienteId) {
        List<PedidoResponse> pedidos = pedidoService.buscarPedidosPorCliente(clienteId).stream()
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
        
        String mensagem = String.format("Pedidos do cliente ID %d listados com sucesso", clienteId);
        return ResponseEntity.ok(ApiResponseDTO.success(pedidos, mensagem));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar itens do pedido por ID",
               description = "Busca os itens de um pedido específico pelo seu identificador")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Itens do pedido encontrados"),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    public ResponseEntity<ApiResponseDTO<List<ItemPedidoResponse>>> buscarPedidoPorId(
            @PathVariable @Parameter(description = "ID do pedido") Long id) {
        Pedido pedido = pedidoService.buscarPedidoPorId(id)
                .orElseThrow(() -> new RuntimeException(MSG_PEDIDO_NAO_ENCONTRADO));
        
        List<ItemPedidoResponse> itens = pedido.getItens().stream()
                .map(i -> new ItemPedidoResponse(i.getProduto().getId(), i.getProduto().getNome(), i.getQuantidade(), i.getPrecoUnitario()))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponseDTO.success(itens, MSG_PEDIDO_ENCONTRADO));
    }

    @PutMapping("/status/{id}")
    @Operation(summary = "Alterar status do pedido",
               description = "Altera o status de um pedido específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status alterado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    public ResponseEntity<ApiResponseDTO<String>> alterarStatus(
            @PathVariable @Parameter(description = "ID do pedido") Long id, 
            @RequestParam @Parameter(description = "Novo status do pedido") StatusPedido status) {
        pedidoService.atualizarStatusPedido(id, status);
        logger.info("Status do pedido com ID {} alterado para {}", id, status);
        
        String mensagem = String.format("Status do pedido alterado para %s", status);
        return ResponseEntity.ok(ApiResponseDTO.success(null, mensagem));
    }


    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar pedido",
               description = "Cancela um pedido específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido cancelado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    public ResponseEntity<ApiResponseDTO<String>> cancelarPedido(
            @PathVariable @Parameter(description = "ID do pedido") Long id) {
        logger.info("Cancelando pedido com ID: {}", id);
        pedidoService.cancelarPedido(id);
        
        return ResponseEntity.ok(ApiResponseDTO.success(null, MSG_PEDIDO_CANCELADO));
    }

}
