package com.deliverytech.delivery.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deliverytech.delivery.dto.request.ItemPedidoRequest;
import com.deliverytech.delivery.dto.request.PedidoRequest;
import com.deliverytech.delivery.exception.EntityNotFoundException;
import com.deliverytech.delivery.exception.ValidationException;
import com.deliverytech.delivery.model.Cliente;
import com.deliverytech.delivery.model.Endereco;
import com.deliverytech.delivery.model.ItemPedido;
import com.deliverytech.delivery.model.Pedido;
import com.deliverytech.delivery.model.Produto;
import com.deliverytech.delivery.model.Restaurante;
import com.deliverytech.delivery.model.StatusPedido;
import com.deliverytech.delivery.repository.ClienteRepository;
import com.deliverytech.delivery.repository.PedidoRepository;
import com.deliverytech.delivery.repository.ProdutoRepository;
import com.deliverytech.delivery.repository.RestauranteRepository;
import com.deliverytech.delivery.service.PedidoService;
import com.deliverytech.delivery.service.validation.EntregaValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final RestauranteRepository restauranteRepository;
    private final ProdutoRepository produtoRepository;
    private final EntregaValidator entregaValidator;

    @Override
    public Pedido criarPedido(PedidoRequest dto) {
        log.info("Iniciando criação de pedido para cliente ID: {}", dto.getClienteId());
        
        Cliente cliente = validarCliente(dto.getClienteId(), dto.getEnderecoEntrega());
        Restaurante restaurante = validarRestaurante(dto.getRestauranteId(), dto.getEnderecoEntrega());
        List<Produto> produtos = validarProdutos(dto.getItens(), restaurante);

        BigDecimal totalItens = calcularTotalItens(dto.getItens(), produtos);
        BigDecimal taxaEntrega = entregaValidator.calcularTaxaEntrega(restaurante, dto.getEnderecoEntrega());
        BigDecimal totalPedido = totalItens.add(taxaEntrega);

        return salvarPedidoCompleto(cliente, restaurante, dto, produtos, totalPedido);
    }
    
    private Cliente validarCliente(Long clienteId, Endereco enderecoEntrega) {
        log.debug("Validando cliente ID: {}", clienteId);
        
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente", clienteId));
        
        if (Boolean.FALSE.equals(cliente.getAtivo())) {
            throw new ValidationException("Cliente não está ativo");
        }
        
        validarEnderecoEntrega(enderecoEntrega);
        
        log.debug("Cliente validado com sucesso: {}", cliente.getNome());
        return cliente;
    }
    
    private void validarEnderecoEntrega(Endereco enderecoEntrega) {
        if (enderecoEntrega == null) {
            throw new ValidationException("enderecoEntrega", "Endereço de entrega é obrigatório");
        }
        
        if (isStringNullOrEmpty(enderecoEntrega.getRua())) {
            throw new ValidationException("enderecoEntrega.rua", "Rua é obrigatória");
        }
        
        if (isStringNullOrEmpty(enderecoEntrega.getCidade())) {
            throw new ValidationException("enderecoEntrega.cidade", "Cidade é obrigatória");
        }
        
        if (isStringNullOrEmpty(enderecoEntrega.getCep())) {
            throw new ValidationException("enderecoEntrega.cep", "CEP é obrigatório");
        }
    }
    
    private boolean isStringNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
    
    private Restaurante validarRestaurante(Long restauranteId, Endereco enderecoEntrega) {
        log.debug("Validando restaurante ID: {}", restauranteId);
        
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurante", restauranteId));
        
        if (Boolean.FALSE.equals(restaurante.getAtivo())) {
            throw new ValidationException("Restaurante não está ativo");
        }
        
        if (!entregaValidator.restauranteEntregaNoEndereco(restaurante, enderecoEntrega)) {
            throw new ValidationException("Restaurante não entrega no endereço informado");
        }
        
        log.debug("Restaurante validado com sucesso: {}", restaurante.getNome());
        return restaurante;
    }
    
    private Produto buscarEValidarProduto(Long produtoId, Restaurante restaurante, Integer quantidadeSolicitada) {
        Produto produto = produtoRepository.findByIdForUpdate(produtoId);
        if (produto == null) {
            throw new EntityNotFoundException("Produto", produtoId);
        }
        if (Boolean.FALSE.equals(produto.getDisponivel())) {
            throw new ValidationException(String.format("Produto '%s' não está disponível", produto.getNome()));
        }
        if (!produto.getRestaurante().getId().equals(restaurante.getId())) {
            throw new ValidationException(String.format("Produto '%s' não pertence ao restaurante selecionado", produto.getNome()));
        }
        if (produto.getEstoque() < quantidadeSolicitada) {
            throw new ValidationException(String.format("Produto '%s' não possui estoque suficiente. Disponível: %d", produto.getNome(), produto.getEstoque()));
        }

        produto.setEstoque(produto.getEstoque() - quantidadeSolicitada);
        produtoRepository.save(produto);
        return produto;
    }

    private List<Produto> validarProdutos(List<ItemPedidoRequest> itens, Restaurante restaurante) {
        log.debug("Validando {} produtos", itens.size());
        
        if (itens == null || itens.isEmpty()) {
            throw new ValidationException("itens", "Pelo menos um item deve ser informado");
        }
        
        List<Produto> produtos = new ArrayList<>();
        
        for (ItemPedidoRequest item : itens) {
            validarQuantidadeItem(item);
            Produto produto = buscarEValidarProduto(item.getProdutoId(), restaurante, item.getQuantidade());
            produtos.add(produto);
        }
        
        log.debug("Produtos validados com sucesso");
        return produtos;
    }
    
    private void validarQuantidadeItem(ItemPedidoRequest item) {
        if (item.getQuantidade() == null || item.getQuantidade() <= 0) {
            throw new ValidationException("quantidade", "Quantidade deve ser maior que zero");
        }
    }
    
    private BigDecimal calcularTotalItens(List<ItemPedidoRequest> itens, List<Produto> produtos) {
        BigDecimal total = BigDecimal.ZERO;
        
        for (int i = 0; i < itens.size(); i++) {
            ItemPedidoRequest item = itens.get(i);
            Produto produto = produtos.get(i);
            
            BigDecimal subtotal = produto.getPreco().multiply(BigDecimal.valueOf(item.getQuantidade()));
            total = total.add(subtotal);
        }
        
        log.debug("Total dos itens calculado: {}", total);
        return total;
    }
    
    private Pedido salvarPedidoCompleto(Cliente cliente, Restaurante restaurante, PedidoRequest dto, 
                                      List<Produto> produtos, BigDecimal totalPedido) {
        log.debug("Salvando pedido transacionalmente");
        
        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .restaurante(restaurante)
                .enderecoEntrega(dto.getEnderecoEntrega())
                .status(StatusPedido.CRIADO)
                .total(totalPedido)
                .itens(new ArrayList<>())
                .build();
        
        List<ItemPedido> itensPedido = criarItensPedido(dto.getItens(), produtos, pedido);
        pedido.setItens(itensPedido);
        
        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        
        log.info("Pedido criado com sucesso - ID: {}, Total: {}", pedidoSalvo.getId(), pedidoSalvo.getTotal());
        return pedidoSalvo;
    }
    
    private List<ItemPedido> criarItensPedido(List<ItemPedidoRequest> itensRequest, List<Produto> produtos, Pedido pedido) {
        List<ItemPedido> itensPedido = new ArrayList<>();
        
        for (int i = 0; i < itensRequest.size(); i++) {
            ItemPedidoRequest itemRequest = itensRequest.get(i);
            Produto produto = produtos.get(i);
            
            ItemPedido item = ItemPedido.builder()
                    .pedido(pedido)
                    .produto(produto)
                    .quantidade(itemRequest.getQuantidade())
                    .precoUnitario(produto.getPreco())
                    .build();
            
            itensPedido.add(item);
        }
        
        return itensPedido;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pedido> buscarPedidoPorId(Long id) {
        return pedidoRepository.findByIdWithItens(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> buscarPedidosPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteIdWithItens(clienteId);
    }

    @Override
    public Pedido atualizarStatusPedido(Long id, StatusPedido status) {
        return pedidoRepository.findById(id)
                .map(pedido -> {
                    if (StatusPedido.CANCELADO.equals(pedido.getStatus())) {
                        throw new ValidationException("Não é possível alterar status de pedido cancelado");
                    }
                    pedido.setStatus(status);
                    return pedidoRepository.save(pedido);
                }).orElseThrow(() -> new EntityNotFoundException("Pedido", id));
    }

    @Override
    public BigDecimal calcularTotalPedido(Long pedidoId) {
        return pedidoRepository.findById(pedidoId)
                .map(Pedido::getTotal)
                .orElseThrow(() -> new EntityNotFoundException("Pedido", pedidoId));
    }

    @Override
    public BigDecimal calcularTotalSemSalvar(PedidoRequest dto) {
        log.debug("Calculando total sem salvar pedido");
        
        validarCliente(dto.getClienteId(), dto.getEnderecoEntrega());
        Restaurante restaurante = validarRestaurante(dto.getRestauranteId(), dto.getEnderecoEntrega());
        List<Produto> produtos = validarProdutos(dto.getItens(), restaurante);
        
        BigDecimal totalItens = calcularTotalItens(dto.getItens(), produtos);
        BigDecimal taxaEntrega = entregaValidator.calcularTaxaEntrega(restaurante, dto.getEnderecoEntrega());
        BigDecimal totalFinal = totalItens.add(taxaEntrega);
        
        log.debug("Total calculado - Itens: {}, Taxa Entrega: {}, Total: {}", 
                 totalItens, taxaEntrega, totalFinal);
        
        return totalFinal;
    }

    @Override
    public void cancelarPedido(Long id) {
        pedidoRepository.findById(id)
                .map(pedido -> {
                    if (StatusPedido.ENTREGUE.equals(pedido.getStatus())) {
                        throw new ValidationException("Não é possível cancelar pedido já entregue");
                    }
                    pedido.setStatus(StatusPedido.CANCELADO);
                    return pedidoRepository.save(pedido);
                }).orElseThrow(() -> new EntityNotFoundException("Pedido", id));
    }
}
