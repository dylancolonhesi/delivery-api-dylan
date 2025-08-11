package com.deliverytech.delivery.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import com.deliverytech.delivery.dto.request.ClienteRequest;
import com.deliverytech.delivery.factory.TestDataFactory;
import com.deliverytech.delivery.model.Cliente;
import com.deliverytech.delivery.repository.ClienteRepository;
import com.deliverytech.delivery.service.impl.ClienteServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ClienteService")
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private Cliente cliente;
    private ClienteRequest clienteRequest;

    @BeforeEach
    void setUp() {
        cliente = TestDataFactory.createCliente();
        clienteRequest = TestDataFactory.createClienteRequest();
    }

    @Test
    @DisplayName("Deve cadastrar cliente com sucesso")
    void deveCadastrarClienteComSucesso() {
        // Given
        when(modelMapper.map(clienteRequest, Cliente.class)).thenReturn(cliente);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        // When
        Cliente resultado = clienteService.cadastrarCliente(clienteRequest);

        // Then
        assertNotNull(resultado);
        assertEquals(cliente.getNome(), resultado.getNome());
        assertEquals(cliente.getEmail(), resultado.getEmail());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve buscar cliente por ID com sucesso")
    void deveBuscarClientePorIdComSucesso() {
        // Given
        Long clienteId = 1L;
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        // When
        Optional<Cliente> resultado = clienteService.buscarClientePorId(clienteId);

        // Then
        assertTrue(resultado.isPresent());
        assertEquals(cliente.getId(), resultado.get().getId());
        verify(clienteRepository, times(1)).findById(clienteId);
    }

    @Test
    @DisplayName("Deve listar todos os clientes")
    void deveListarTodosOsClientes() {
        // Given
        List<Cliente> clientes = Arrays.asList(cliente, TestDataFactory.createCliente());
        when(clienteRepository.findAll()).thenReturn(clientes);

        // When
        List<Cliente> resultado = clienteService.listarTodosClientes();

        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(clienteRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve listar apenas clientes ativos")
    void deveListarApenasClientesAtivos() {
        // Given
        List<Cliente> clientesAtivos = Arrays.asList(cliente);
        when(clienteRepository.findByAtivoTrue()).thenReturn(clientesAtivos);

        // When
        List<Cliente> resultado = clienteService.listarClientesAtivos();

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getAtivo());
        verify(clienteRepository, times(1)).findByAtivoTrue();
    }

    @Test
    @DisplayName("Deve buscar clientes por nome")
    void deveBuscarClientesPorNome() {
        // Given
        String nome = "João";
        List<Cliente> clientes = Arrays.asList(cliente);
        when(clienteRepository.findByNomeContainingIgnoreCase(nome)).thenReturn(clientes);

        // When
        List<Cliente> resultado = clienteService.buscarClientesPorNome(nome);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(clienteRepository, times(1)).findByNomeContainingIgnoreCase(nome);
    }

    @Test
    @DisplayName("Deve atualizar cliente com sucesso")
    void deveAtualizarClienteComSucesso() {
        // Given
        Long clienteId = 1L;
        ClienteRequest novosDados = TestDataFactory.createClienteRequest();
        novosDados.setNome("João Santos");
        
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        // When
        Cliente resultado = clienteService.atualizarCliente(clienteId, novosDados);

        // Then
        assertNotNull(resultado);
        verify(clienteRepository, times(1)).findById(clienteId);
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve ativar/desativar cliente com sucesso")
    void deveAtivarDesativarClienteComSucesso() {
        // Given
        Long clienteId = 1L;
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        // When
        clienteService.ativarDesativarCliente(clienteId);

        // Then
        verify(clienteRepository, times(1)).findById(clienteId);
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar cliente inexistente")
    void deveLancarExcecaoAoBuscarClienteInexistente() {
        // Given
        Long clienteId = 999L;
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            clienteService.atualizarCliente(clienteId, clienteRequest);
        });
        
        verify(clienteRepository, times(1)).findById(clienteId);
    }
}
