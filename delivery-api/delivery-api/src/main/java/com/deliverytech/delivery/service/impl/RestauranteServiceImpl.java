package com.deliverytech.delivery.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deliverytech.delivery.dto.request.RestauranteRequest;
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
    public Restaurante cadastrarRestaurante(RestauranteRequest dto) {
        if (restauranteRepository.existsByNome(dto.getNome())) {
            throw new RuntimeException("Nome do restaurante já existe");
        }
        Restaurante restaurante = Restaurante.builder()
                .nome(dto.getNome())
                .categoria(dto.getCategoria())
                .telefone(dto.getTelefone())
                .taxaEntrega(dto.getTaxaEntrega())
                .tempoEntregaMinutos(dto.getTempoEntregaMinutos())
                .ativo(true)
                .build();
        return restauranteRepository.save(restaurante);
    }

    @Override
    public Optional<Restaurante> buscarRestaurantePorId(Long id) {
        return restauranteRepository.findById(id);
    }

    @Override
    public List<Restaurante> buscarRestaurantesPorCategoria(String categoria) {
        return restauranteRepository.findByCategoria(categoria);
    }

    @Override
    public List<Restaurante> buscarRestaurantesDisponiveis() {
        return restauranteRepository.findByAtivoTrue();
    }

    @Override
    public List<Restaurante> listarRestaurantes() {
        return restauranteRepository.findAll();
    }

    @Override
    public Restaurante atualizarRestaurante(Long id, RestauranteRequest dto) {
        return restauranteRepository.findById(id)
                .map(restaurante -> {
                    if (!restaurante.getNome().equals(dto.getNome()) &&
                        restauranteRepository.existsByNome(dto.getNome())) {
                        throw new RuntimeException("Nome do restaurante já existe");
                    }
                    restaurante.setNome(dto.getNome());
                    restaurante.setCategoria(dto.getCategoria());
                    restaurante.setTelefone(dto.getTelefone());
                    restaurante.setTaxaEntrega(dto.getTaxaEntrega());
                    restaurante.setTempoEntregaMinutos(dto.getTempoEntregaMinutos());
                    return restauranteRepository.save(restaurante);
                }).orElseThrow(() -> new RuntimeException("Restaurante não encontrado"));
    }

    @Override
    public void removerRestaurante(Long id) {
        if (!restauranteRepository.existsById(id)) {
            throw new RuntimeException("Restaurante não encontrado com ID: " + id);
        }
        restauranteRepository.deleteById(id);
    }

    @Override
    public BigDecimal calcularTaxaEntrega(Long restauranteId, String cepDestino) {
        return restauranteRepository.findById(restauranteId)
                .map(restaurante -> {
                    BigDecimal taxaBase = restaurante.getTaxaEntrega();
                    
                    String cepSemFormatacao = cepDestino.replaceAll("[^0-9]", "");
                    int primeirosDigitos = Integer.parseInt(cepSemFormatacao.substring(0, 2));
                    
                    if (primeirosDigitos >= 80 && primeirosDigitos <= 82) {
                        return taxaBase;
                    } else if (primeirosDigitos >= 83 && primeirosDigitos <= 85) {
                        return taxaBase.multiply(BigDecimal.valueOf(1.5));
                    } else {
                        return taxaBase.multiply(BigDecimal.valueOf(2.0));
                    }
                }).orElseThrow(() -> new RuntimeException("Restaurante não encontrado"));
    }
}
