package com.deliverytech.delivery.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deliverytech.delivery.model.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByRestauranteId(Long restauranteId);
    List<Produto> findByCategoria(String categoria);
    List<Produto> findByDisponivelTrue();
}
