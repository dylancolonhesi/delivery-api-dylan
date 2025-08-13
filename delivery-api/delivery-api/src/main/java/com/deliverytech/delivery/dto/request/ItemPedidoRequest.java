package com.deliverytech.delivery.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para item do pedido")
public class ItemPedidoRequest {

    @NotNull(message = "Produto ID é obrigatório")
    @Schema(description = "ID do produto", example = "1", required = true)
    private Long produtoId;

    @NotNull(message = "Quantidade é obrigatória")
    @Positive(message = "Quantidade deve ser um valor positivo")
    @Max(value = 50, message = "Quantidade máxima permitida é 50 unidades")
    @Schema(description = "Quantidade do produto", example = "2", required = true)
    private Integer quantidade;
}
