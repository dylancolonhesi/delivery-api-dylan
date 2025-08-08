package com.deliverytech.delivery.controller;


import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deliverytech.delivery.dto.request.ClienteRequest;
import com.deliverytech.delivery.dto.response.ClienteResponse;
import com.deliverytech.delivery.model.Cliente;
import com.deliverytech.delivery.service.ClienteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private static final Logger logger = LoggerFactory.getLogger(ClienteController.class);

    private final ClienteService clienteService;

    /**
     * Cadastra um novo cliente no sistema
     * @param request Dados do cliente a ser cadastrado
     * @return ResponseEntity com os dados do cliente criado
     */
    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrar(@RequestBody @Valid ClienteRequest request) {
        logger.info("Cadastro de cliente iniciado: {}", request.getEmail());
        
        Cliente cliente = Cliente.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .telefone(request.getTelefone())
                .endereco(request.getEndereco())
                .build();

        Cliente salvo = clienteService.cadastrar(cliente);

        logger.debug("Cliente salvo com ID {}", salvo.getId());

        return ResponseEntity.ok(
                new ClienteResponse(salvo.getId(), salvo.getNome(), salvo.getEmail(), salvo.getEndereco(), salvo.getAtivo()));
    
    }

    /**
     * Lista todos os clientes ativos do sistema
     * @return Lista de clientes ativos
     */
    @GetMapping
    public List<ClienteResponse> listar() {
        logger.info("Listando todos os clientes ativos");
        return clienteService.listarAtivos().stream()
                .map(c -> new ClienteResponse(c.getId(), c.getNome(), c.getEmail(), c.getEndereco(), c.getAtivo()))
                .collect(Collectors.toList());
    }

    /**
     * Busca um cliente específico pelo seu ID
     * @param id ID do cliente a ser buscado
     * @return ResponseEntity com os dados do cliente encontrado ou 404 se não encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscar(@PathVariable Long id) {
        logger.info("Buscando cliente com ID: {}", id);
        return clienteService.buscarPorId(id)
                .map(c -> new ClienteResponse(c.getId(), c.getNome(), c.getEmail(), c.getEndereco(), c.getAtivo()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.warn("Cliente com ID {} não encontrado", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Atualiza completamente os dados de um cliente existente
     * @param id ID do cliente a ser atualizado
     * @param request Novos dados do cliente
     * @return ResponseEntity com os dados atualizados do cliente
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> atualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        logger.info("Atualizando cliente ID: {}", id);

        Cliente atualizado = Cliente.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .telefone(request.getTelefone())
                .endereco(request.getEndereco())
                .build();

        Cliente salvo = clienteService.atualizar(id, atualizado);

        return ResponseEntity.ok(new ClienteResponse(salvo.getId(), salvo.getNome(), salvo.getEmail(), salvo.getEndereco(), salvo.getAtivo()));
    }

    /**
     * Alterna o status ativo/inativo de um cliente
     * @param id ID do cliente que terá o status alterado
     * @return ResponseEntity vazio com status 204 (No Content)
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> ativarDesativar(@PathVariable Long id) {
        logger.info("Alterando status do cliente ID: {}", id);
        clienteService.ativarDesativar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint de verificação de status da API e informações do sistema
     * @return ResponseEntity com mensagem de status da API
     */
    @GetMapping("/status")
    public ResponseEntity<String> status() {
        logger.debug("Status endpoint acessado");
        int cpuCores = Runtime.getRuntime().availableProcessors();
        logger.info("CPU cores disponíveis: {}", cpuCores);
        return ResponseEntity.ok("API está online");
    }

}
