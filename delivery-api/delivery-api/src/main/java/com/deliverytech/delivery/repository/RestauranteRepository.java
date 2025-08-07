package com.deliverytech.delivery.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deliverytech.delivery.model.Restaurante;

public interface RestauranteRepository extends JpaRepository<Restaurante, Long> {
    
    List<Restaurante> findByCategoria(String categoria);
    List<Restaurante> findByNomeContainingIgnoreCase(String nome);
    List<Restaurante> findByAtivoTrue();
    List<Restaurante> findAllByOrderByAvaliacaoDesc();
}
