package com.deliverytech.delivery.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.deliverytech.delivery.dto.request.ClienteRequest;
import com.deliverytech.delivery.dto.response.ApiResponseDTO;
import com.deliverytech.delivery.metrics.BusinessMetricsService;
import com.deliverytech.delivery.model.Cliente;
import com.deliverytech.delivery.service.ClienteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Validated
@Tag(name = "Clientes", description = "Operações relacionadas aos clientes")
public class ClienteController {
    private static final Logger logger = LoggerFactory.getLogger(ClienteController.class);

    private static final String MSG_CLIENTE_CADASTRADO = "Cliente cadastrado com sucesso";
    private static final String MSG_CLIENTE_ENCONTRADO = "Cliente encontrado";
    private static final String MSG_CLIENTE_NAO_ENCONTRADO = "Cliente não encontrado";
    private static final String MSG_CLIENTE_ATUALIZADO = "Cliente atualizado com sucesso";
    private static final String MSG_STATUS_ALTERADO = "Status do cliente alterado com sucesso";

    private final ClienteService clienteService;
    private final BusinessMetricsService metricsService;

    @PostMapping
    @Operation(summary = "Cadastrar cliente",
               description = "Cria um novo cliente no sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Cliente criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "Cliente já existe")
    })
    public ResponseEntity<ApiResponseDTO<Cliente>> cadastrar(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados do cliente a ser criado",
                required = true
            ) ClienteRequest dto) {
        logger.info("Cadastro de cliente iniciado: {}", dto.getNome());
    Cliente cliente = clienteService.cadastrarCliente(dto);
    metricsService.atualizarUsuariosAtivos(1);
        logger.debug("Cliente salvo com ID {}", cliente.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(cliente, MSG_CLIENTE_CADASTRADO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID",
               description = "Busca um cliente específico pelo seu identificador")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<ApiResponseDTO<Cliente>> buscarPorId(
            @PathVariable @Parameter(description = "ID do cliente") Long id) {
        logger.debug("Buscando cliente por ID {}", id);
        return clienteService.buscarClientePorId(id)
                .map(cliente -> ResponseEntity.ok(ApiResponseDTO.success(cliente, MSG_CLIENTE_ENCONTRADO)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error(MSG_CLIENTE_NAO_ENCONTRADO)));
    }

    @GetMapping("/email")
    @Operation(summary = "Buscar cliente por email",
               description = "Busca um cliente específico pelo seu endereço de email")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<ApiResponseDTO<Cliente>> buscarPorEmail(
            @RequestParam @Parameter(description = "Email do cliente") String email) {
        logger.debug("Buscando cliente por email {}", email);
        return clienteService.buscarClientePorEmail(email)
                .map(cliente -> ResponseEntity.ok(ApiResponseDTO.success(cliente, MSG_CLIENTE_ENCONTRADO)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error(MSG_CLIENTE_NAO_ENCONTRADO)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cliente",
               description = "Atualiza os dados de um cliente existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<ApiResponseDTO<Cliente>> atualizar(
            @PathVariable @Parameter(description = "ID do cliente") Long id, 
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados atualizados do cliente",
                required = true
            ) ClienteRequest dto) {
        try {
            logger.info("Atualização de cliente iniciada para ID {}", id);
            Cliente cliente = clienteService.atualizarCliente(id, dto);
            logger.debug("Cliente atualizado com ID {}", cliente.getId());
            
            return ResponseEntity.ok(ApiResponseDTO.success(cliente, MSG_CLIENTE_ATUALIZADO));
        } catch (RuntimeException e) {
            logger.error("Erro ao atualizar cliente: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponseDTO.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/ativar-desativar")
    @Operation(summary = "Ativar ou desativar cliente",
               description = "Alterna o status de ativo/inativo de um cliente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status alterado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<ApiResponseDTO<String>> ativarDesativar(
            @PathVariable @Parameter(description = "ID do cliente") Long id) {
        try {
            logger.info("Alterando status do cliente com ID {}", id);
            clienteService.ativarDesativarCliente(id);
            
            return ResponseEntity.ok(ApiResponseDTO.success(null, MSG_STATUS_ALTERADO));
        } catch (RuntimeException e) {
            logger.error("Erro ao alterar status do cliente: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error(MSG_CLIENTE_NAO_ENCONTRADO));
        }
    }

    @GetMapping("/ativos")
    @Operation(summary = "Listar clientes ativos",
               description = "Lista todos os clientes que estão ativos no sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Clientes ativos listados com sucesso")
    })
    public ResponseEntity<ApiResponseDTO<List<Cliente>>> listarAtivos() {
        logger.debug("Listando clientes ativos");
        List<Cliente> clientes = clienteService.listarClientesAtivos();
        
        String mensagem = String.format("Total de %d clientes ativos listados", clientes.size());
        return ResponseEntity.ok(ApiResponseDTO.success(clientes, mensagem));
    }

    @GetMapping
    @Operation(summary = "Listar clientes com filtros",
               description = "Lista clientes com filtros opcionais por nome ou email")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Clientes listados com sucesso")
    })
    public ResponseEntity<ApiResponseDTO<List<Cliente>>> listarTodos(
            @RequestParam(required = false) @Parameter(description = "Nome do cliente para filtro") String nome,
            @RequestParam(required = false) @Parameter(description = "Email do cliente para filtro") String email) {
        logger.debug("Listando clientes com filtros - nome: {}, email: {}", nome, email);
        List<Cliente> clientes;
        
        if (nome != null && !nome.trim().isEmpty()) {
            clientes = clienteService.buscarClientesPorNome(nome.trim());
        } else if (email != null && !email.trim().isEmpty()) {
            clientes = clienteService.buscarClientesPorEmail(email.trim());
        } else {
            clientes = clienteService.listarTodosClientes();
        }
        
        String mensagem = String.format("Total de %d clientes listados", clientes.size());
        return ResponseEntity.ok(ApiResponseDTO.success(clientes, mensagem));
    }
}
