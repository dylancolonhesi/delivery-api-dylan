package com.deliverytech.delivery.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deliverytech.delivery.dto.request.RestauranteRequest;
import com.deliverytech.delivery.dto.response.RestauranteResponse;
import com.deliverytech.delivery.model.Restaurante;
import com.deliverytech.delivery.service.RestauranteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/restaurantes")
@RequiredArgsConstructor
public class RestauranteController {
    private static final Logger logger = LoggerFactory.getLogger(RestauranteController.class);

    private final RestauranteService restauranteService;

    /**
     * Cadastra um novo restaurante no sistema
     * @param request Dados do restaurante a ser cadastrado
     * @return ResponseEntity com os dados do restaurante criado
     */
    @PostMapping
    public ResponseEntity<RestauranteResponse> cadastrar(@Valid @RequestBody RestauranteRequest request) {
        logger.info("Cadastro de restaurante iniciado: {}", request.getNome());
        Restaurante restaurante = Restaurante.builder()
                .nome(request.getNome())
                .telefone(request.getTelefone())
                .categoria(request.getCategoria())
                .taxaEntrega(request.getTaxaEntrega())
                .tempoEntregaMinutos(request.getTempoEntregaMinutos())
                .ativo(true)
                .build();
        Restaurante salvo = restauranteService.adicionarRestaurante(restaurante);
        logger.debug("Restaurante salvo com ID {}", salvo.getId());
        return ResponseEntity.ok(new RestauranteResponse(
                salvo.getId(), salvo.getNome(), salvo.getCategoria(), salvo.getTelefone(),
                salvo.getTaxaEntrega(), salvo.getTempoEntregaMinutos(), salvo.getAtivo()));
    }

    /**
     * Lista todos os restaurantes cadastrados no sistema
     * @return Lista de restaurantes
     */
    @GetMapping
    public List<RestauranteResponse> listarTodos() {
        logger.debug("Listando todos os restaurantes");
        return restauranteService.listarRestaurantes().stream()
                .map(r -> new RestauranteResponse(r.getId(), r.getNome(), r.getCategoria(), r.getTelefone(), r.getTaxaEntrega(), r.getTempoEntregaMinutos(), r.getAtivo()))
                .collect(Collectors.toList());
    }

    /**
     * Busca um restaurante específico pelo seu ID
     * @param id ID do restaurante a ser buscado
     * @return ResponseEntity com os dados do restaurante encontrado ou 404 se não encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<RestauranteResponse> buscarPorId(@PathVariable Long id) {
        logger.debug("Buscando restaurante por ID {}", id);
        return restauranteService.buscarRestaurantePorId(id)
                .map(r -> new RestauranteResponse(r.getId(), r.getNome(), r.getCategoria(), r.getTelefone(), r.getTaxaEntrega(), r.getTempoEntregaMinutos(), r.getAtivo()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Busca restaurantes por categoria específica
     * @param categoria Categoria dos restaurantes a serem buscados
     * @return Lista de restaurantes da categoria especificada
     */
    @GetMapping("/categoria/{categoria}")
    public List<RestauranteResponse> buscarPorCategoria(@PathVariable String categoria) {
        logger.debug("Buscando restaurantes por categoria {}", categoria);
        return restauranteService.buscarRestaurantePorCategoria(categoria).stream()
                .map(r -> new RestauranteResponse(r.getId(), r.getNome(), r.getCategoria(), r.getTelefone(), r.getTaxaEntrega(), r.getTempoEntregaMinutos(), r.getAtivo()))
                .collect(Collectors.toList());
    }

    /**
     * Atualiza completamente os dados de um restaurante existente
     * @param id ID do restaurante a ser atualizado
     * @param request Novos dados do restaurante
     * @return ResponseEntity com os dados atualizados do restaurante
     */
    @PutMapping("/{id}")
    public ResponseEntity<RestauranteResponse> atualizar(@PathVariable Long id, @Valid @RequestBody RestauranteRequest request) {
        Restaurante atualizado = Restaurante.builder()
                .nome(request.getNome())
                .telefone(request.getTelefone())
                .categoria(request.getCategoria())
                .taxaEntrega(request.getTaxaEntrega())
                .tempoEntregaMinutos(request.getTempoEntregaMinutos())
                .build();
        Restaurante salvo = restauranteService.atualizarRestaurante(id, atualizado);
        return ResponseEntity.ok(new RestauranteResponse(salvo.getId(), salvo.getNome(), salvo.getCategoria(), salvo.getTelefone(), salvo.getTaxaEntrega(), salvo.getTempoEntregaMinutos(), salvo.getAtivo()));
    }

    /**
     * Remove um restaurante do sistema
     * @param id ID do restaurante a ser removido
     * @return ResponseEntity vazio com status 204 (No Content)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        logger.debug("Removendo restaurante com ID {}", id);
        restauranteService.removerRestaurante(id);
        return ResponseEntity.noContent().build();
    }
}
