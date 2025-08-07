package com.deliverytech.delivery.service;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;

import com.deliverytech.delivery.model.Restaurante;


@Cacheable("restaurantes")
public interface RestauranteService {

    void adicionarRestaurante(Restaurante restaurante);
    Optional<Restaurante> buscarRestaurantePorId(Long id);
    List<Restaurante> listarRestaurantes();
    List<Restaurante> buscarRestaurantePorCategoria(String categoria);
    Restaurante atualizarRestaurante(Long id, Restaurante restaurante);
    void removerRestaurante(Long id);
}
