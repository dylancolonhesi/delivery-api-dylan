package com.deliverytech.delivery.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.deliverytech.delivery.dto.request.RestauranteRequest;
import com.deliverytech.delivery.dto.response.ApiResponseDTO;
import com.deliverytech.delivery.dto.response.PagedResponse;
import com.deliverytech.delivery.dto.response.RestauranteResponse;
import com.deliverytech.delivery.model.Restaurante;
import com.deliverytech.delivery.service.RestauranteService;
import com.deliverytech.delivery.util.PagedResponseUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/restaurantes")
@RequiredArgsConstructor
@Tag(name = "Restaurantes", description = "Operações relacionadas aos restaurantes")
public class RestauranteController {
    private static final Logger logger = LoggerFactory.getLogger(RestauranteController.class);

    private static final String MSG_RESTAURANTE_CADASTRADO = "Restaurante cadastrado com sucesso";
    private static final String MSG_RESTAURANTES_LISTADOS = "Restaurantes listados com sucesso";
    private static final String MSG_RESTAURANTE_ENCONTRADO = "Restaurante encontrado";
    private static final String MSG_RESTAURANTE_NAO_ENCONTRADO = "Restaurante não encontrado";
    private static final String MSG_RESTAURANTE_ATUALIZADO = "Restaurante atualizado com sucesso";
    private static final String MSG_TAXA_CALCULADA = "Taxa de entrega calculada com sucesso";

    private final RestauranteService restauranteService;

    @PostMapping
    @Operation(summary = "Cadastrar restaurante",
               description = "Cria um novo restaurante no sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Restaurante criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "Restaurante já existe")
    })
    public ResponseEntity<ApiResponseDTO<RestauranteResponse>> cadastrar(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados do restaurante a ser criado",
                required = true
            ) RestauranteRequest request) {
        logger.info("Cadastro de restaurante iniciado: {}", request.getNome());
        try {
            Restaurante salvo = restauranteService.cadastrarRestaurante(request);
            logger.debug("Restaurante salvo com ID {}", salvo.getId());
            
            RestauranteResponse response = new RestauranteResponse(
                    salvo.getId(), salvo.getNome(), salvo.getCategoria(), salvo.getTelefone(),
                    salvo.getTaxaEntrega(), salvo.getTempoEntregaMinutos(), salvo.getAtivo());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDTO.success(response, MSG_RESTAURANTE_CADASTRADO));
        } catch (RuntimeException e) {
            logger.error("Erro ao cadastrar restaurante: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponseDTO.error(e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "Listar restaurantes com paginação",
               description = "Lista restaurantes cadastrados no sistema com suporte a paginação e ordenação")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista paginada de restaurantes retornada com sucesso")
    })
    public ResponseEntity<ApiResponseDTO<PagedResponse<RestauranteResponse>>> listarTodos(
            @Parameter(description = "Número da página (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo para ordenação") @RequestParam(defaultValue = "nome") String sortBy,
            @Parameter(description = "Direção da ordenação (asc ou desc)") @RequestParam(defaultValue = "asc") String sortDir) {
        
        logger.debug("Listando restaurantes - página: {}, tamanho: {}, ordenação: {} {}", page, size, sortBy, sortDir);
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Restaurante> restaurantePage = restauranteService.listarRestaurantesPaginados(pageable);
        
        // Mapear para RestauranteResponse
        Page<RestauranteResponse> responsePage = restaurantePage.map(r -> 
            new RestauranteResponse(r.getId(), r.getNome(), r.getCategoria(), r.getTelefone(), 
                    r.getTaxaEntrega(), r.getTempoEntregaMinutos(), r.getAtivo()));
        
        PagedResponse<RestauranteResponse> pagedResponse = PagedResponseUtil.createPagedResponse(responsePage);
        
        return ResponseEntity.ok(ApiResponseDTO.success(pagedResponse, MSG_RESTAURANTES_LISTADOS));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar restaurante por ID",
               description = "Busca um restaurante específico pelo seu identificador")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Restaurante encontrado"),
        @ApiResponse(responseCode = "404", description = "Restaurante não encontrado")
    })
    public ResponseEntity<ApiResponseDTO<RestauranteResponse>> buscarPorId(
            @PathVariable @Parameter(description = "ID do restaurante") Long id) {
        logger.debug("Buscando restaurante por ID {}", id);
        return restauranteService.buscarRestaurantePorId(id)
                .map(r -> {
                    RestauranteResponse response = new RestauranteResponse(r.getId(), r.getNome(), r.getCategoria(), 
                            r.getTelefone(), r.getTaxaEntrega(), r.getTempoEntregaMinutos(), r.getAtivo());
                    return ResponseEntity.ok(ApiResponseDTO.success(response, MSG_RESTAURANTE_ENCONTRADO));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error(MSG_RESTAURANTE_NAO_ENCONTRADO)));
    }

    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Buscar restaurantes por categoria",
               description = "Lista restaurantes filtrados por categoria específica")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Restaurantes da categoria listados com sucesso")
    })
    public ResponseEntity<ApiResponseDTO<List<RestauranteResponse>>> buscarPorCategoria(
            @PathVariable @Parameter(description = "Categoria dos restaurantes") String categoria) {
        logger.debug("Buscando restaurantes por categoria {}", categoria);
        List<RestauranteResponse> restaurantes = restauranteService.buscarRestaurantesPorCategoria(categoria).stream()
                .map(r -> new RestauranteResponse(r.getId(), r.getNome(), r.getCategoria(), r.getTelefone(), 
                         r.getTaxaEntrega(), r.getTempoEntregaMinutos(), r.getAtivo()))
                .collect(Collectors.toList());
        
        String mensagem = String.format("Restaurantes da categoria %s listados com sucesso", categoria);
        return ResponseEntity.ok(ApiResponseDTO.success(restaurantes, mensagem));
    }

    @GetMapping("/{id}/taxa-entrega/{cep}")
    @Operation(summary = "Calcular taxa de entrega",
               description = "Calcula a taxa de entrega de um restaurante para um CEP específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Taxa de entrega calculada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Restaurante não encontrado")
    })
    public ResponseEntity<ApiResponseDTO<BigDecimal>> calcularTaxaEntrega(
            @PathVariable @Parameter(description = "ID do restaurante") Long id, 
            @PathVariable @Parameter(description = "CEP de entrega") String cep) {
        logger.debug("Calculando taxa de entrega para restaurante ID {} e CEP {}", id, cep);
        try {
            BigDecimal taxa = restauranteService.calcularTaxaEntrega(id, cep);
            return ResponseEntity.ok(ApiResponseDTO.success(taxa, MSG_TAXA_CALCULADA));
        } catch (RuntimeException e) {
            logger.error("Erro ao calcular taxa de entrega: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar restaurante",
               description = "Atualiza os dados de um restaurante existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Restaurante atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Restaurante não encontrado")
    })
    public ResponseEntity<ApiResponseDTO<RestauranteResponse>> atualizar(
            @PathVariable @Parameter(description = "ID do restaurante") Long id, 
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados atualizados do restaurante",
                required = true
            ) RestauranteRequest request) {
        try {
            Restaurante salvo = restauranteService.atualizarRestaurante(id, request);
            RestauranteResponse response = new RestauranteResponse(salvo.getId(), salvo.getNome(), salvo.getCategoria(), 
                    salvo.getTelefone(), salvo.getTaxaEntrega(), salvo.getTempoEntregaMinutos(), salvo.getAtivo());
            return ResponseEntity.ok(ApiResponseDTO.success(response, MSG_RESTAURANTE_ATUALIZADO));
        } catch (RuntimeException e) {
            logger.error("Erro ao atualizar restaurante: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover restaurante",
               description = "Remove um restaurante do sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Restaurante removido com sucesso"),
        @ApiResponse(responseCode = "404", description = "Restaurante não encontrado")
    })
    public ResponseEntity<Void> remover(
            @PathVariable @Parameter(description = "ID do restaurante") Long id) {
        logger.debug("Removendo restaurante com ID {}", id);
        try {
            restauranteService.removerRestaurante(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Erro ao remover restaurante: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
