package com.deliverytech.delivery.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deliverytech.delivery.model.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // Buscar produtos por restaurante
    List<Produto> findByRestauranteId(Long restauranteId);
    
    // Buscar produtos disponíveis
    List<Produto> findByDisponivelTrue();
    
    // Buscar produtos por categoria
    List<Produto> findByCategoria(String categoria);
    
    // Buscar produtos com preço menor ou igual ao especificado
    List<Produto> findByPrecoLessThanEqual(BigDecimal preco);
    
    // Buscar produtos por nome (contém, ignora maiúsculas/minúsculas)
    List<Produto> findByNomeContainingIgnoreCase(String nome);
}
