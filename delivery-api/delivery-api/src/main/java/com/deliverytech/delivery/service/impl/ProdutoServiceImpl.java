package com.deliverytech.delivery.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deliverytech.delivery.model.Produto;
import com.deliverytech.delivery.repository.ProdutoRepository;

import lombok.RequiredArgsConstructor;
import com.deliverytech.delivery.service.ProdutoService;

@Service
@RequiredArgsConstructor
@Transactional
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository produtoRepository;

    @Override
    public Produto adicionarProduto(Produto produto) {
        return produtoRepository.save(produto);
    }

    @Override
    public Optional<Produto> buscarProdutoPorId(Long id) {
        return produtoRepository.findById(id);
    }

    @Override
    public List<Produto> listarProdutos() {
        return produtoRepository.findAll();
    }

    @Override
    public List<Produto> listarProdutosPorRestaurante(Long restauranteId) {
        return produtoRepository.findByRestauranteId(restauranteId);
    }

    @Override
    public List<Produto> buscarProdutosPorCategoria(String categoria) {
        return produtoRepository.findByCategoria(categoria);
    }

    @Override
    public Produto atualizarProduto(Long id, Produto produto) {
        return produtoRepository.findById(id)
                .map(p -> {
                    p.setNome(produto.getNome());
                    p.setDescricao(produto.getDescricao());
                    p.setPreco(produto.getPreco());
                    p.setCategoria(produto.getCategoria());
                    return produtoRepository.save(p);
                }).orElseThrow(() -> new RuntimeException("Produto não encontrado"));
    }

    @Override
    public void alterarDisponibilidadeProduto(Long id, boolean disponivel) {
        produtoRepository.findById(id).ifPresent(p -> {
            p.setDisponivel(disponivel);
            produtoRepository.save(p);
        });
    }
    
}
