package com.deliverytech.delivery.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deliverytech.delivery.model.Restaurante;

public interface RestauranteRepository extends JpaRepository<Restaurante, Long> {
    
    // Buscar por categoria
    List<Restaurante> findByCategoria(String categoria);
    
    // Buscar restaurantes ativos
    List<Restaurante> findByAtivoTrue();
    
    // Buscar por taxa de entrega menor ou igual
    List<Restaurante> findByTaxaEntregaLessThanEqual(BigDecimal taxa);
    
    // Top 5 restaurantes ordenados por nome (A-Z)
    List<Restaurante> findTop5ByOrderByNomeAsc();
    
    // Métodos adicionais existentes
    List<Restaurante> findByNomeContainingIgnoreCase(String nome);
    List<Restaurante> findAllByOrderByAvaliacaoDesc();
    boolean existsByNome(String nome);
}
