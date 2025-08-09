package com.deliverytech.delivery.service;

import java.util.List;
import java.util.Optional;

import com.deliverytech.delivery.dto.request.ProdutoRequest;
import com.deliverytech.delivery.model.Produto;

public interface ProdutoService {

    Produto cadastrarProduto(ProdutoRequest dto);
    List<Produto> buscarProdutosPorRestaurante(Long restauranteId);
    Optional<Produto> buscarProdutoPorId(Long id);
    Produto atualizarProduto(Long id, ProdutoRequest dto);
    void alterarDisponibilidade(Long id, boolean disponivel);
    List<Produto> buscarProdutosPorCategoria(String categoria);
}
