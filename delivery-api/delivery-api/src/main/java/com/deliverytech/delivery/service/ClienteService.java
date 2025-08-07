package com.deliverytech.delivery.service;

import com.deliverytech.delivery.model.Cliente;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;

@Cacheable("clientes")
public interface ClienteService {
    Cliente cadastrar(Cliente cliente);
    Optional<Cliente> buscarPorId(Long id);
    List<Cliente> listarAtivos();
    List<Cliente> listarTodos();
    Optional<Cliente> buscarPorEmail(String email);
    Cliente atualizar(Long id, Cliente atualizado);
    void ativarDesativar(Long id);
}
