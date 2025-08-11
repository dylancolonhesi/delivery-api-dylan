package com.deliverytech.delivery.service;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;

import com.deliverytech.delivery.dto.request.ClienteRequest;
import com.deliverytech.delivery.model.Cliente;

@Cacheable("clientes")
public interface ClienteService {
    Cliente cadastrarCliente(ClienteRequest dto);
    Optional<Cliente> buscarClientePorId(Long id);
    Optional<Cliente> buscarClientePorEmail(String email);
    Cliente atualizarCliente(Long id, ClienteRequest dto);
    void ativarDesativarCliente(Long id);
    List<Cliente> listarClientesAtivos();
    List<Cliente> listarTodosClientes();
    List<Cliente> buscarClientesPorNome(String nome);
    List<Cliente> buscarClientesPorEmail(String email);
}
