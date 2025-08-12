package com.deliverytech.delivery.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.deliverytech.delivery.dto.request.RestauranteRequest;
import com.deliverytech.delivery.model.Restaurante;

public interface RestauranteService {

    Restaurante cadastrarRestaurante(RestauranteRequest dto);
    Optional<Restaurante> buscarRestaurantePorId(Long id);
    List<Restaurante> buscarRestaurantesPorCategoria(String categoria);
    List<Restaurante> buscarRestaurantesDisponiveis();
    List<Restaurante> listarRestaurantes();
    Page<Restaurante> listarRestaurantesPaginados(Pageable pageable);
    Restaurante atualizarRestaurante(Long id, RestauranteRequest dto);
    BigDecimal calcularTaxaEntrega(Long restauranteId, String cepDestino);
    void removerRestaurante(Long id);
}
