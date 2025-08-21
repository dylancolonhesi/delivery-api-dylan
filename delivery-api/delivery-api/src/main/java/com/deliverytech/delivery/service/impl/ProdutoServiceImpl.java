package com.deliverytech.delivery.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deliverytech.delivery.dto.request.ProdutoRequest;
import com.deliverytech.delivery.model.Produto;
import com.deliverytech.delivery.model.Restaurante;
import com.deliverytech.delivery.repository.ProdutoRepository;
import com.deliverytech.delivery.repository.RestauranteRepository;
import com.deliverytech.delivery.service.ProdutoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final RestauranteRepository restauranteRepository;

    @Override
    public Produto cadastrarProduto(ProdutoRequest dto) {
        Restaurante restaurante = restauranteRepository.findById(dto.getRestauranteId())
                .orElseThrow(() -> new RuntimeException("Restaurante não encontrado"));
        
        Produto produto = Produto.builder()
                .nome(dto.getNome())
                .descricao(dto.getDescricao())
                .preco(dto.getPreco())
                .categoria(dto.getCategoria())
                .restaurante(restaurante)
                .disponivel(true)
                .build();
        
        return produtoRepository.save(produto);
    }

    @Override
    public List<Produto> buscarProdutosPorRestaurante(Long restauranteId) {
        return produtoRepository.findByRestauranteId(restauranteId);
    }

    @Override
    public Optional<Produto> buscarProdutoPorId(Long id) {
        return produtoRepository.findById(id);
    }

    @Override
    public Produto atualizarProduto(Long id, ProdutoRequest dto) {
        return produtoRepository.findById(id)
                .map(produto -> {
                    produto.setNome(dto.getNome());
                    produto.setDescricao(dto.getDescricao());
                    produto.setPreco(dto.getPreco());
                    produto.setCategoria(dto.getCategoria());
                    return produtoRepository.save(produto);
                }).orElseThrow(() -> new RuntimeException("Produto não encontrado"));
    }

    @Override
    public void alterarDisponibilidade(Long id, boolean disponivel) {
        produtoRepository.findById(id).ifPresentOrElse(produto -> {
            produto.setDisponivel(disponivel);
            produtoRepository.save(produto);
        }, () -> {
            throw new RuntimeException("Produto não encontrado com ID: " + id);
        });
    }

    @Override
    public List<Produto> buscarProdutosPorCategoria(String categoria) {
        return produtoRepository.findByCategoria(categoria);
    }

    @Override
    public boolean podeAcessarProdutosRestaurante(Long usuarioRestauranteId, Long restauranteId) {
        if (usuarioRestauranteId == null) {
            return false;
        }
        return usuarioRestauranteId.equals(restauranteId);
    }
}
