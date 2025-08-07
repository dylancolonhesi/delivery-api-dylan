package com.deliverytech.delivery.service;

import java.util.List;
import java.util.Optional;

import com.deliverytech.delivery.model.Produto;

public interface ProdutoService {
    void adicionarProduto(Produto produto);
    Optional<Produto> buscarProdutoPorId(Long id);
    List<Produto> listarProdutos();
    List<Produto> listarProdutosPorRestaurante(Long restauranteId);
    List<Produto> buscarProdutosPorCategoria(String categoria);
    Produto atualizarProduto(Long id, Produto produto);
    void alterarDisponibilidadeProduto(Long id, boolean disponivel);
}
