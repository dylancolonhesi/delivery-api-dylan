package com.deliverytech.delivery.factory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.deliverytech.delivery.dto.request.ClienteRequest;
import com.deliverytech.delivery.dto.request.ItemPedidoRequest;
import com.deliverytech.delivery.dto.request.PedidoRequest;
import com.deliverytech.delivery.dto.request.ProdutoRequest;
import com.deliverytech.delivery.dto.request.RestauranteRequest;
import com.deliverytech.delivery.model.Cliente;
import com.deliverytech.delivery.model.Endereco;
import com.deliverytech.delivery.model.ItemPedido;
import com.deliverytech.delivery.model.Pedido;
import com.deliverytech.delivery.model.Produto;
import com.deliverytech.delivery.model.Restaurante;
import com.deliverytech.delivery.model.StatusPedido;

public class TestDataFactory {

    public static Cliente createCliente() {
        return Cliente.builder()
                .nome("João Silva")
                .email("joao.silva@test.com")
                .telefone("11999999999")
                .endereco(createEndereco())
                .ativo(true)
                .dataCriacao(LocalDateTime.now())
                .build();
    }

    public static ClienteRequest createClienteRequest() {
        ClienteRequest request = new ClienteRequest();
        request.setNome("João Silva");
        request.setEmail("joao.silva@test.com");
        request.setTelefone("11999999999");
        request.setEndereco(createEndereco());
        return request;
    }

    public static Restaurante createRestaurante() {
        return Restaurante.builder()
                .nome("Restaurante Test")
                .categoria("Italiana")
                .telefone("11888888888")
                .taxaEntrega(new BigDecimal("5.00"))
                .tempoEntregaMinutos(30)
                .avaliacao(5)
                .ativo(true)
                .build();
    }

    public static RestauranteRequest createRestauranteRequest() {
        RestauranteRequest request = new RestauranteRequest();
        request.setNome("Restaurante Test");
        request.setCategoria("Italiana");
        request.setTelefone("11888888888");
        request.setTaxaEntrega(new BigDecimal("5.00"));
        request.setTempoEntregaMinutos(30);
        return request;
    }

    public static Produto createProduto(Restaurante restaurante) {
        return Produto.builder()
                .nome("Pizza Margherita")
                .categoria("Pizza")
                .descricao("Pizza com molho de tomate, mussarela e manjericão")
                .preco(new BigDecimal("29.90"))
                .disponivel(true)
                .restaurante(restaurante)
                .build();
    }

    public static ProdutoRequest createProdutoRequest(Long restauranteId) {
        ProdutoRequest request = new ProdutoRequest();
        request.setNome("Pizza Margherita");
        request.setCategoria("Pizza");
        request.setDescricao("Pizza com molho de tomate, mussarela e manjericão");
        request.setPreco(new BigDecimal("29.90"));
        request.setRestauranteId(restauranteId);
        return request;
    }

    public static Pedido createPedido(Cliente cliente, Restaurante restaurante) {
        return Pedido.builder()
                .cliente(cliente)
                .restaurante(restaurante)
                .enderecoEntrega(createEndereco())
                .status(StatusPedido.CRIADO)
                .total(new BigDecimal("29.90"))
                .dataPedido(LocalDateTime.now())
                .build();
    }

    public static PedidoRequest createPedidoRequest(Long clienteId, Long restauranteId, Long produtoId) {
        PedidoRequest request = new PedidoRequest();
        request.setClienteId(clienteId);
        request.setRestauranteId(restauranteId);
        request.setEnderecoEntrega(createEndereco());
        
        ItemPedidoRequest item = new ItemPedidoRequest();
        item.setProdutoId(produtoId);
        item.setQuantidade(2);
        
        request.setItens(List.of(item));
        return request;
    }

    public static ItemPedido createItemPedido(Pedido pedido, Produto produto) {
        return ItemPedido.builder()
                .pedido(pedido)
                .produto(produto)
                .quantidade(2)
                .precoUnitario(produto.getPreco())
                .build();
    }

    public static Endereco createEndereco() {
        Endereco endereco = new Endereco();
        endereco.setRua("Rua das Flores");
        endereco.setNumero("123");
        endereco.setBairro("Jardim das Flores");
        endereco.setCidade("São Paulo");
        endereco.setCep("01234-567");
        endereco.setEstado("SP");
        return endereco;
    }

    public static Endereco createEnderecoAlternativo() {
        Endereco endereco = new Endereco();
        endereco.setRua("Av. Paulista");
        endereco.setNumero("1000");
        endereco.setBairro("Bela Vista");
        endereco.setCidade("São Paulo");
        endereco.setCep("01310-100");
        endereco.setEstado("SP");
        return endereco;
    }
}
