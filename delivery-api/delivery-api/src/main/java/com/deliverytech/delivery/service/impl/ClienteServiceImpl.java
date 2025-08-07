package com.deliverytech.delivery.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deliverytech.delivery.model.Cliente;
import com.deliverytech.delivery.repository.ClienteRepository;
import com.deliverytech.delivery.service.ClienteService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ClienteServiceImpl implements ClienteService {
    
    private final ClienteRepository clienteRepository;
    
    @Override
    public Cliente cadastrar(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        return clienteRepository.findById(id);
    }

    @Override
    public List<Cliente> listarAtivos() {
        return clienteRepository.findAllAtivos();
    }
    
    @Override
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }
    
    @Override
    public Optional<Cliente> buscarPorEmail(String email) {
        return clienteRepository.findByEmail(email.toLowerCase());
    }

    @Override
    public Cliente atualizar(Long id, Cliente atualizado) {
        return clienteRepository.findById(id)
                .map(c -> {
                    c.setNome(atualizado.getNome());
                    return clienteRepository.save(c);
                }).orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
    }

    @Override
    public void ativarDesativar(Long id) {
        clienteRepository.findById(id).ifPresentOrElse(cliente -> {
            cliente.setAtivo(!cliente.getAtivo());
            clienteRepository.save(cliente);
        }, () -> {
            throw new RuntimeException("Cliente não encontrado com ID: " + id);
        });
    }        
}
