package com.deliverytech.delivery.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deliverytech.delivery.model.Restaurante;
import com.deliverytech.delivery.repository.RestauranteRepository;
import com.deliverytech.delivery.service.RestauranteService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RestauranteServiceImpl implements RestauranteService {

    private final RestauranteRepository restauranteRepository;

    @Override
    public Restaurante adicionarRestaurante(Restaurante restaurante) {
        return restauranteRepository.save(restaurante);
    }

    @Override
    public Optional<Restaurante> buscarRestaurantePorId(Long id) {
        return restauranteRepository.findById(id);
    }

    @Override
    public List<Restaurante> listarRestaurantes() {
        return restauranteRepository.findAll();
    }

    @Override
    public List<Restaurante> buscarRestaurantePorCategoria(String categoria) {
        return restauranteRepository.findByCategoria(categoria);
    }

    @Override
    public Restaurante atualizarRestaurante(Long id, Restaurante restaurante) {
        return restauranteRepository.findById(id)
                .map(r -> {
                    r.setNome(restaurante.getNome());
                    r.setCategoria(restaurante.getCategoria());
                    return restauranteRepository.save(r);
                }).orElseThrow(() -> new RuntimeException("Restaurante não encontrado"));
    }

    @Override
    public void removerRestaurante(Long id) {
        restauranteRepository.deleteById(id);
    }

}
