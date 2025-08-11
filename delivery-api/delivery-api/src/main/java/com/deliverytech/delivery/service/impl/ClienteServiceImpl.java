package com.deliverytech.delivery.service.impl;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deliverytech.delivery.dto.request.ClienteRequest;
import com.deliverytech.delivery.model.Cliente;
import com.deliverytech.delivery.repository.ClienteRepository;
import com.deliverytech.delivery.service.ClienteService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ClienteServiceImpl implements ClienteService {
    
    private final ClienteRepository clienteRepository;
    private final ModelMapper modelMapper;
    
    @Override
    public Cliente cadastrarCliente(ClienteRequest dto) {
        if (clienteRepository.existsByEmail(dto.getEmail().toLowerCase())) {
            throw new RuntimeException("Email já cadastrado");
        }
        
        Cliente cliente = modelMapper.map(dto, Cliente.class);
        cliente.setEmail(dto.getEmail().toLowerCase());
        cliente.setAtivo(true);
        
        return clienteRepository.save(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cliente> buscarClientePorId(Long id) {
        return clienteRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cliente> buscarClientePorEmail(String email) {
        return clienteRepository.findByEmail(email.toLowerCase());
    }

    @Override
    public Cliente atualizarCliente(Long id, ClienteRequest dto) {
        return clienteRepository.findById(id)
                .map(cliente -> {
                    if (!cliente.getEmail().equals(dto.getEmail().toLowerCase()) &&
                        clienteRepository.existsByEmail(dto.getEmail().toLowerCase())) {
                        throw new RuntimeException("Email já cadastrado");
                    }
                    
                    modelMapper.map(dto, cliente);
                    cliente.setEmail(dto.getEmail().toLowerCase());
                    
                    return clienteRepository.save(cliente);
                }).orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
    }

    @Override
    public void ativarDesativarCliente(Long id) {
        clienteRepository.findById(id).ifPresentOrElse(cliente -> {
            Boolean ativoAtual = cliente.getAtivo() != null ? cliente.getAtivo() : false;
            cliente.setAtivo(!ativoAtual);
            clienteRepository.save(cliente);
        }, () -> {
            throw new RuntimeException("Cliente não encontrado com ID: " + id);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> listarClientesAtivos() {
        return clienteRepository.findByAtivoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> listarTodosClientes() {
        return clienteRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> buscarClientesPorNome(String nome) {
        return clienteRepository.findByNomeContainingIgnoreCase(nome);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> buscarClientesPorEmail(String email) {
        return clienteRepository.findByEmailContainingIgnoreCase(email);
    }
}
