package com.deliverytech.delivery.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.deliverytech.delivery.controller.ClienteController;
import com.deliverytech.delivery.dto.request.ClienteRequest;
import com.deliverytech.delivery.model.Cliente;
import com.deliverytech.delivery.model.Endereco;
import com.deliverytech.delivery.service.ClienteService;

public class ClienteControllerTest {

    @InjectMocks
    private ClienteController clienteController;

    @Mock
    private ClienteService clienteService;

    public ClienteControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCadastrarCliente() {
        ClienteRequest clienteRequest = new ClienteRequest();
        clienteRequest.setNome("João Silva");
        clienteRequest.setEmail("joao.silva@gmail.com");
        clienteRequest.setTelefone("11999999999");
        Endereco endereco = new Endereco();
        endereco.setBairro("Jardim das Flores");
        endereco.setNumero("123");
        clienteRequest.setEndereco(endereco);

        when(clienteService.cadastrarCliente(any(ClienteRequest.class))).thenReturn(new Cliente());

        clienteController.cadastrar(clienteRequest);

        verify(clienteService, times(1)).cadastrarCliente(clienteRequest);
    }

}
