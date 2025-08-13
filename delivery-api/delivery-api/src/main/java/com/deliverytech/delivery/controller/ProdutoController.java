package com.deliverytech.delivery.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.deliverytech.delivery.dto.request.ProdutoRequest;
import com.deliverytech.delivery.dto.response.ApiResponseDTO;
import com.deliverytech.delivery.dto.response.ProdutoResponse;
import com.deliverytech.delivery.model.Produto;
import com.deliverytech.delivery.service.ProdutoService;
import com.deliverytech.delivery.service.RestauranteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Operações relacionadas aos produtos")
public class ProdutoController {
    private static final Logger logger = LoggerFactory.getLogger(ProdutoController.class);

    private static final String MSG_PRODUTO_CADASTRADO = "Produto cadastrado com sucesso";
    private static final String MSG_PRODUTO_ATUALIZADO = "Produto atualizado com sucesso";
    private static final String MSG_RESTAURANTE_NAO_ENCONTRADO = "Restaurante não encontrado";

    private final ProdutoService produtoService;
    private final RestauranteService restauranteService;

    
    @PostMapping
    @Operation(summary = "Cadastrar produto",
               description = "Cria um novo produto no sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Produto criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Restaurante não encontrado")
    })
    public ResponseEntity<ApiResponseDTO<ProdutoResponse>> cadastrar(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados do produto a ser criado",
                required = true
            ) ProdutoRequest request) {
        logger.info("Cadastro de produto iniciado: {}", request.getNome());
        restauranteService.buscarRestaurantePorId(request.getRestauranteId())
                .orElseThrow(() -> new RuntimeException(MSG_RESTAURANTE_NAO_ENCONTRADO));

        ProdutoRequest novoRequest = new ProdutoRequest(
                request.getNome(),
                request.getCategoria(),
                request.getDescricao(),
                request.getPreco(),
                request.getRestauranteId()
        );

        Produto salvo = produtoService.cadastrarProduto(novoRequest);
        logger.debug("Produto salvo com ID {}", salvo.getId());
        
        ProdutoResponse response = new ProdutoResponse(
                salvo.getId(), salvo.getNome(), salvo.getCategoria(), salvo.getDescricao(), salvo.getPreco(), salvo.getDisponivel());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(response, MSG_PRODUTO_CADASTRADO));
    }

    @GetMapping("/restaurante/{restauranteId}")
    @Operation(summary = "Listar produtos por restaurante",
               description = "Lista todos os produtos de um restaurante específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Produtos do restaurante listados com sucesso"),
        @ApiResponse(responseCode = "404", description = "Restaurante não encontrado")
    })
    public ResponseEntity<ApiResponseDTO<List<ProdutoResponse>>> listarPorRestaurante(
            @PathVariable @Parameter(description = "ID do restaurante") Long restauranteId) {
        logger.debug("Listando produtos do restaurante com ID {}", restauranteId);
        List<ProdutoResponse> produtos = produtoService.buscarProdutosPorRestaurante(restauranteId).stream()
                .map(p -> new ProdutoResponse(p.getId(), p.getNome(), p.getCategoria(), p.getDescricao(), p.getPreco(), p.getDisponivel()))
                .collect(Collectors.toList());
        
        String mensagem = String.format("Produtos do restaurante ID %d listados com sucesso", restauranteId);
        return ResponseEntity.ok(ApiResponseDTO.success(produtos, mensagem));
    }

    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Listar produtos por categoria",
               description = "Lista todos os produtos de uma categoria específica")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Produtos da categoria listados com sucesso")
    })
    public ResponseEntity<ApiResponseDTO<List<ProdutoResponse>>> listarPorCategoria(
            @PathVariable @Parameter(description = "Categoria dos produtos") String categoria) {
        logger.debug("Listando produtos da categoria {}", categoria);
        List<ProdutoResponse> produtos = produtoService.buscarProdutosPorCategoria(categoria).stream()
                .map(p -> new ProdutoResponse(p.getId(), p.getNome(), p.getCategoria(), p.getDescricao(), p.getPreco(), p.getDisponivel()))
                .collect(Collectors.toList());
        
        String mensagem = String.format("Produtos da categoria %s listados com sucesso", categoria);
        return ResponseEntity.ok(ApiResponseDTO.success(produtos, mensagem));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar produto",
               description = "Atualiza os dados de um produto existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<ApiResponseDTO<ProdutoResponse>> atualizar(
            @PathVariable @Parameter(description = "ID do produto") Long id, 
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados atualizados do produto",
                required = true
            ) ProdutoRequest request) {
        logger.info("Atualização de produto iniciada para ID {}", id);
        Produto salvo = produtoService.atualizarProduto(id, request);
        logger.debug("Produto atualizado com ID {}", salvo.getId());
        
        ProdutoResponse response = new ProdutoResponse(salvo.getId(), salvo.getNome(), salvo.getCategoria(), salvo.getDescricao(), salvo.getPreco(), salvo.getDisponivel());
        return ResponseEntity.ok(ApiResponseDTO.success(response, MSG_PRODUTO_ATUALIZADO));
    }

    @PatchMapping("/{id}/disponibilidade")
    @Operation(summary = "Alterar disponibilidade do produto",
               description = "Altera a disponibilidade de um produto (ativo/inativo)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Disponibilidade alterada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<ApiResponseDTO<String>> alterarDisponibilidade(
            @PathVariable @Parameter(description = "ID do produto") Long id, 
            @RequestParam @Parameter(description = "Nova disponibilidade do produto") boolean disponivel) {
        produtoService.alterarDisponibilidade(id, disponivel);
        logger.info("Disponibilidade do produto com ID {} alterada para {}", id, disponivel);
        
        String mensagem = String.format("Produto %s com sucesso", disponivel ? "ativado" : "desativado");
        return ResponseEntity.ok(ApiResponseDTO.success(null, mensagem));
    }

}
