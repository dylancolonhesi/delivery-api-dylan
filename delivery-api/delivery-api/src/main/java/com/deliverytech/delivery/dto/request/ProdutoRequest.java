package com.deliverytech.delivery.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.deliverytech.delivery.validation.ValidCategoria;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para criação e atualização de produto")
public class ProdutoRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 50, message = "Nome deve ter entre 2 e 50 caracteres")
    @Schema(description = "Nome do produto", example = "Pizza Margherita", required = true)
    private String nome;

    @NotBlank(message = "Categoria é obrigatória")
    @ValidCategoria(message = "Categoria deve ser uma das opções válidas")
    @Schema(description = "Categoria do produto", example = "Pizzaria", required = true)
    private String categoria;

    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 10, message = "Descrição deve ter no mínimo 10 caracteres")
    @Schema(description = "Descrição detalhada do produto", example = "Pizza tradicional com molho de tomate, mussarela e manjericão fresco", required = true)
    private String descricao;

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    @DecimalMax(value = "500.00", message = "Preço deve ser no máximo R$ 500,00")
    @Schema(description = "Preço do produto", example = "25.90", required = true)
    private BigDecimal preco;

    @NotNull(message = "Restaurante é obrigatório")
    @Schema(description = "ID do restaurante ao qual o produto pertence", example = "1", required = true)
    private Long restauranteId;
}
