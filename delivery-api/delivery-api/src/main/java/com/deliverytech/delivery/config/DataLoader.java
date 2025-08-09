package com.deliverytech.delivery.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.deliverytech.delivery.model.Cliente;
import com.deliverytech.delivery.model.Endereco;
import com.deliverytech.delivery.model.Pedido;
import com.deliverytech.delivery.model.Produto;
import com.deliverytech.delivery.model.Restaurante;
import com.deliverytech.delivery.model.StatusPedido;
import com.deliverytech.delivery.repository.ClienteRepository;
import com.deliverytech.delivery.repository.PedidoRepository;
import com.deliverytech.delivery.repository.ProdutoRepository;
import com.deliverytech.delivery.repository.RestauranteRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    private final ClienteRepository clienteRepository;
    private final RestauranteRepository restauranteRepository;
    private final ProdutoRepository produtoRepository;
    private final PedidoRepository pedidoRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("Iniciando carga de dados de teste...");

        pedidoRepository.deleteAll();
        produtoRepository.deleteAll();
        restauranteRepository.deleteAll();
        clienteRepository.deleteAll();

        List<Cliente> clientes = criarClientes();
        
        List<Restaurante> restaurantes = criarRestaurantes();
        
        List<Produto> produtos = criarProdutos(restaurantes);
        
        List<Pedido> pedidos = criarPedidos(clientes, restaurantes, produtos);
        
        criarPedidosAdicionais(clientes, restaurantes);

        logger.info("Dados de teste carregados com sucesso!");
        logger.info("Resumo: {} clientes, {} restaurantes, {} produtos, {} pedidos", 
                   clientes.size(), restaurantes.size(), produtos.size(), pedidos.size());

        validarConsultas();
    }

    private List<Cliente> criarClientes() {
        logger.info("👥 Criando clientes...");
        
        Cliente cliente1 = Cliente.builder()
                .nome("João Silva")
                .email("joao@email.com")
                .telefone("(11) 99999-1111")
                .endereco(Endereco.builder()
                        .rua("Rua das Flores, 123")
                        .bairro("Centro")
                        .cidade("São Paulo")
                        .estado("SP")
                        .cep("01234-567")
                        .build())
                .ativo(true)
                .build();

        Cliente cliente2 = Cliente.builder()
                .nome("Maria Santos")
                .email("maria.santos@email.com")
                .telefone("(11) 88888-2222")
                .endereco(Endereco.builder()
                        .rua("Av. Paulista, 456")
                        .bairro("Bela Vista")
                        .cidade("São Paulo")
                        .estado("SP")
                        .cep("01310-100")
                        .build())
                .ativo(true)
                .build();

        Cliente cliente3 = Cliente.builder()
                .nome("Pedro Costa")
                .email("pedro.costa@email.com")
                .telefone("(11) 77777-3333")
                .endereco(Endereco.builder()
                        .rua("Rua Augusta, 789")
                        .bairro("Vila Madalena")
                        .cidade("São Paulo")
                        .estado("SP")
                        .cep("05414-000")
                        .build())
                .ativo(false)
                .build();

        return clienteRepository.saveAll(Arrays.asList(cliente1, cliente2, cliente3));
    }

    private List<Restaurante> criarRestaurantes() {
        logger.info("Criando restaurantes...");
        
        Restaurante restaurante1 = Restaurante.builder()
                .nome("Pizza Express")
                .categoria("Italiana")
                .telefone("(11) 3333-1111")
                .taxaEntrega(new BigDecimal("8.50"))
                .tempoEntregaMinutos(30)
                .avaliacao(4)
                .ativo(true)
                .build();

        Restaurante restaurante2 = Restaurante.builder()
                .nome("Burger House")
                .categoria("Hamburguer")
                .telefone("(11) 3333-2222")
                .taxaEntrega(new BigDecimal("5.00"))
                .tempoEntregaMinutos(25)
                .avaliacao(5)
                .ativo(true)
                .build();

        return restauranteRepository.saveAll(Arrays.asList(restaurante1, restaurante2));
    }

    private List<Produto> criarProdutos(List<Restaurante> restaurantes) {
        logger.info("Criando produtos...");
        
        Restaurante pizzaExpress = restaurantes.get(0);
        Restaurante burgerHouse = restaurantes.get(1);

        Produto pizza1 = Produto.builder()
                .nome("Pizza Margherita")
                .descricao("Pizza clássica com molho de tomate, mussarela e manjericão")
                .preco(new BigDecimal("32.90"))
                .categoria("Pizza")
                .disponivel(true)
                .restaurante(pizzaExpress)
                .build();

        Produto pizza2 = Produto.builder()
                .nome("Pizza Pepperoni")
                .descricao("Pizza com molho de tomate, mussarela e pepperoni")
                .preco(new BigDecimal("38.90"))
                .categoria("Pizza")
                .disponivel(true)
                .restaurante(pizzaExpress)
                .build();

        Produto burger1 = Produto.builder()
                .nome("Burger Clássico")
                .descricao("Hambúrguer com carne bovina, alface, tomate e molho especial")
                .preco(new BigDecimal("25.90"))
                .categoria("Hamburguer")
                .disponivel(true)
                .restaurante(burgerHouse)
                .build();

        Produto burger2 = Produto.builder()
                .nome("Burger Bacon")
                .descricao("Hambúrguer com carne bovina, bacon, queijo e molho barbecue")
                .preco(new BigDecimal("29.90"))
                .categoria("Hamburguer")
                .disponivel(true)
                .restaurante(burgerHouse)
                .build();

        Produto bebida = Produto.builder()
                .nome("Refrigerante Lata")
                .descricao("Coca-Cola, Pepsi ou Guaraná 350ml")
                .preco(new BigDecimal("4.50"))
                .categoria("Bebida")
                .disponivel(false)
                .restaurante(burgerHouse)
                .build();

        return produtoRepository.saveAll(Arrays.asList(pizza1, pizza2, burger1, burger2, bebida));
    }

    private List<Pedido> criarPedidos(List<Cliente> clientes, List<Restaurante> restaurantes, List<Produto> produtos) {
        logger.info("Criando pedidos...");
        

        Pedido pedido1 = Pedido.builder()
                .cliente(clientes.get(0))
                .restaurante(restaurantes.get(0))
                .dataPedido(LocalDateTime.now().minusDays(2))
                .status(StatusPedido.ENTREGUE)
                .total(new BigDecimal("75.80"))
                .relatorioPedido("Sem cebola na margherita")
                .build();

        Pedido pedido2 = Pedido.builder()
                .cliente(clientes.get(1))
                .restaurante(restaurantes.get(1))
                .dataPedido(LocalDateTime.now().minusHours(3))
                .status(StatusPedido.CRIADO)
                .total(new BigDecimal("60.80"))
                .relatorioPedido("Ponto da carne ao ponto")
                .build();

        List<Pedido> pedidosSalvos = pedidoRepository.saveAll(Arrays.asList(pedido1, pedido2));

        logger.info("{} pedidos criados com sucesso!", pedidosSalvos.size());
        return pedidosSalvos;
    }
    
    private void criarPedidosAdicionais(List<Cliente> clientes, List<Restaurante> restaurantes) {
        logger.info("Criando pedidos adicionais para testes...");

        for (int i = 0; i < 5; i++) {
            Pedido pedido = Pedido.builder()
                    .cliente(clientes.get(0))
                    .restaurante(i % 2 == 0 ? restaurantes.get(0) : restaurantes.get(1))
                    .dataPedido(LocalDateTime.now().minusDays(i + 1))
                    .status(i % 3 == 0 ? StatusPedido.ENTREGUE : StatusPedido.CONFIRMADO)
                    .total(new BigDecimal(String.valueOf(30 + (i * 10))))
                    .relatorioPedido("Pedido adicional " + (i + 1))
                    .build();
            pedidoRepository.save(pedido);
        }
        
        for (int i = 0; i < 3; i++) {
            Pedido pedido = Pedido.builder()
                    .cliente(clientes.get(1))
                    .restaurante(restaurantes.get(i % 2))
                    .dataPedido(LocalDateTime.now().minusHours(i * 6))
                    .status(StatusPedido.ENTREGUE)
                    .total(new BigDecimal(String.valueOf(45 + (i * 15))))
                    .relatorioPedido("Pedido Maria " + (i + 1))
                    .build();
            pedidoRepository.save(pedido);
        }
        
        logger.info("Pedidos adicionais criados para melhorar os testes!");
    }

    private void validarConsultas() {
        logger.info("Iniciando validação dos cenários de teste...");

        logger.info("Busca de Cliente por Email");
        Cliente cliente = clienteRepository.findByEmail("joao.silva@email.com").orElse(null);
        if (cliente != null) {
            logger.info("Cliente encontrado: {} - {}", cliente.getNome(), cliente.getEmail());
            logger.info("Endereço: {}", cliente.getEndereco().getRua());
        } else {
            logger.error("Cliente não encontrado!");
        }

        logger.info("Produtos por Restaurante");
        List<Produto> produtos = produtoRepository.findByRestauranteId(1L);
        logger.info("Produtos encontrados para restaurante ID 1: {}", produtos.size());
        for (Produto produto : produtos) {
            logger.info("Produto: {} - R$ {} ({})", 
                       produto.getNome(), produto.getPreco(), 
                       produto.getDisponivel() ? "Disponível" : "Indisponível");
        }

        logger.info("Pedidos Recentes");
        List<Pedido> pedidos = pedidoRepository.findTop10ByOrderByDataPedidoDesc();
        logger.info("Top 10 pedidos mais recentes: {}", pedidos.size());
        for (Pedido pedido : pedidos) {
            logger.info("Pedido #{} - Cliente: {} - R$ {} - Status: {}", 
                       pedido.getId(), pedido.getCliente().getNome(), 
                       pedido.getTotal(), pedido.getStatus());
        }

        logger.info("Restaurantes por Taxa");
        List<Restaurante> restaurantes = restauranteRepository
            .findByTaxaEntregaLessThanEqual(new BigDecimal("5.00"));
        logger.info("Restaurantes com taxa ≤ R$ 5,00: {}", restaurantes.size());
        for (Restaurante restaurante : restaurantes) {
            logger.info("Restaurante: {} - Taxa: R$ {} - Categoria: {}", 
                       restaurante.getNome(), restaurante.getTaxaEntrega(), 
                       restaurante.getCategoria());
        }
    }
}
