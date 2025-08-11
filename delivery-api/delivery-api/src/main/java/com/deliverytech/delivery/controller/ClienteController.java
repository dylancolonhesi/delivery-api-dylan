package com.deliverytech.delivery.controller;

import java.util.List;

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
import com.deliverytech.delivery.model.Cliente;
import com.deliverytech.delivery.service.ClienteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Validated
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    public ResponseEntity<Cliente> cadastrar(@Valid @RequestBody ClienteRequest dto) {
        Cliente cliente = clienteService.cadastrarCliente(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(cliente);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscarPorId(@PathVariable Long id) {
        return clienteService.buscarClientePorId(id)
                .map(cliente -> ResponseEntity.ok(cliente))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email")
    public ResponseEntity<Cliente> buscarPorEmail(@RequestParam String email) {
        return clienteService.buscarClientePorEmail(email)
                .map(cliente -> ResponseEntity.ok(cliente))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> atualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest dto) {
        try {
            Cliente cliente = clienteService.atualizarCliente(id, dto);
            return ResponseEntity.ok(cliente);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/ativar-desativar")
    public ResponseEntity<Void> ativarDesativar(@PathVariable Long id) {
        try {
            clienteService.ativarDesativarCliente(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<Cliente>> listarAtivos() {
        List<Cliente> clientes = clienteService.listarClientesAtivos();
        return ResponseEntity.ok(clientes);
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> listarTodos(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String email) {
        List<Cliente> clientes;
        
        if (nome != null && !nome.trim().isEmpty()) {
            clientes = clienteService.buscarClientesPorNome(nome.trim());
        } else if (email != null && !email.trim().isEmpty()) {
            clientes = clienteService.buscarClientesPorEmail(email.trim());
        } else {
            clientes = clienteService.listarTodosClientes();
        }
        
        return ResponseEntity.ok(clientes);
    }
}
