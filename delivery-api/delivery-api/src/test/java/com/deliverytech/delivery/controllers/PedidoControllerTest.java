package com.deliverytech.delivery.controllers;

import com.deliverytech.delivery.controller.PedidoController;
import com.deliverytech.delivery.dto.request.PedidoRequest;
import com.deliverytech.delivery.dto.request.ItemPedidoRequest;
import com.deliverytech.delivery.metrics.BusinessMetricsService;
import com.deliverytech.delivery.model.Pedido;
import com.deliverytech.delivery.model.StatusPedido;
import com.deliverytech.delivery.model.Cliente;
import com.deliverytech.delivery.model.Restaurante;
import com.deliverytech.delivery.model.Produto;
import com.deliverytech.delivery.model.ItemPedido;
import com.deliverytech.delivery.security.JwtAuthenticationFilter;
import com.deliverytech.delivery.security.JwtUtil;
import com.deliverytech.delivery.service.ClienteService;
import com.deliverytech.delivery.service.PedidoService;
import com.deliverytech.delivery.service.RestauranteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PedidoController.class)
@AutoConfigureMockMvc(addFilters = false)
class PedidoControllerTest {

    private static final Long CLIENTE_ID = 1L;
    private static final Long RESTAURANTE_ID = 1L;
    private static final Long PRODUTO_ID = 1L;
    private static final String PRODUTO_NOME = "Pizza Margherita";
    private static final String RESTAURANTE_NOME = "Pizza Express";
    private static final String CLIENTE_NOME = "João Silva";
    private static final String ENDERECO_CEP = "01234-567";
    private static final String ENDERECO_RUA = "Rua das Flores";
    private static final String ENDERECO_NUMERO = "123";
    private static final String ENDERECO_BAIRRO = "Centro";
    private static final String ENDERECO_CIDADE = "São Paulo";
    private static final String ENDERECO_ESTADO = "SP";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoService pedidoService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClienteService clienteService;

    @MockBean
    private RestauranteService restauranteService;

    @MockBean
    private BusinessMetricsService metricsService;

    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private PedidoRequest buildValidPedidoRequest() {
        PedidoRequest request = new PedidoRequest();
        request.setClienteId(CLIENTE_ID);
        request.setRestauranteId(RESTAURANTE_ID);
        request.setEnderecoEntrega(
            com.deliverytech.delivery.model.Endereco.builder()
                .rua(ENDERECO_RUA)
                .numero(ENDERECO_NUMERO)
                .bairro(ENDERECO_BAIRRO)
                .cidade(ENDERECO_CIDADE)
                .estado(ENDERECO_ESTADO)
                .cep(ENDERECO_CEP)
                .build()
        );
        request.setItens(List.of(new ItemPedidoRequest(PRODUTO_ID, 2)));
        return request;
    }

    private Pedido buildValidPedido() {
        Cliente cliente = new Cliente();
        cliente.setId(CLIENTE_ID);
        cliente.setNome(CLIENTE_NOME);
        Restaurante restaurante = new Restaurante();
        restaurante.setId(RESTAURANTE_ID);
        restaurante.setNome(RESTAURANTE_NOME);
        Produto produto = new Produto();
        produto.setId(PRODUTO_ID);
        produto.setNome(PRODUTO_NOME);
        ItemPedido item = new ItemPedido();
        item.setProduto(produto);
        item.setQuantidade(2);
        item.setPrecoUnitario(new BigDecimal("29.90"));
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setStatus(StatusPedido.CRIADO);
        pedido.setTotal(new BigDecimal("99.99"));
        pedido.setEnderecoEntrega(
            com.deliverytech.delivery.model.Endereco.builder()
                .rua(ENDERECO_RUA)
                .numero(ENDERECO_NUMERO)
                .bairro(ENDERECO_BAIRRO)
                .cidade(ENDERECO_CIDADE)
                .estado(ENDERECO_ESTADO)
                .cep(ENDERECO_CEP)
                .build()
        );
        pedido.setItens(List.of(item));
        pedido.setCliente(cliente);
        pedido.setRestaurante(restaurante);
        pedido.setDataPedido(java.time.LocalDateTime.now());
        return pedido;
    }

    @Test
    @DisplayName("Deve criar pedido com sucesso")
    void deveCriarPedidoComSucesso() throws Exception {
        PedidoRequest request = buildValidPedidoRequest();
        Pedido response = buildValidPedido();
        Mockito.when(pedidoService.criarPedido(any(PedidoRequest.class))).thenReturn(response);
        Mockito.when(clienteService.buscarClientePorId(any(Long.class))).thenReturn(Optional.of(response.getCliente()));
        Mockito.when(restauranteService.buscarRestaurantePorId(any(Long.class))).thenReturn(Optional.of(response.getRestaurante()));

        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.status").value("CRIADO"));
    }

    @Test
    @DisplayName("Deve retornar erro ao criar pedido com dados inválidos")
    void deveRetornarErroAoCriarPedidoComDadosInvalidos() throws Exception {
        PedidoRequest request = new PedidoRequest(); // campos obrigatórios ausentes
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve calcular total do pedido")
    void deveCalcularTotalDoPedido() throws Exception {
        PedidoRequest request = buildValidPedidoRequest();
        BigDecimal total = new BigDecimal("150.00");
        Mockito.when(pedidoService.calcularTotalSemSalvar(any(PedidoRequest.class))).thenReturn(total);
        mockMvc.perform(post("/api/pedidos/calcular")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(150.00));
    }

    @Test
    @Disabled("Ignorado conforme solicitado pelo usuário")
    @DisplayName("Deve listar pedidos por cliente")
    void deveListarPedidosPorCliente() throws Exception {
        // Ignorado
    }

    @Test
    @Disabled("Ignorado conforme solicitado pelo usuário")
    @DisplayName("Deve buscar pedido por ID")
    void deveBuscarPedidoPorId() throws Exception {
        // Ignorado
    }

    @Test
    @DisplayName("Deve alterar status do pedido")
    void deveAlterarStatusDoPedido() throws Exception {
        Mockito.when(pedidoService.atualizarStatusPedido(eq(1L), eq(StatusPedido.CONFIRMADO))).thenReturn(null);
        mockMvc.perform(put("/api/pedidos/status/1")
                .param("status", "CONFIRMADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").value("Status do pedido alterado para CONFIRMADO"));
    }
}
