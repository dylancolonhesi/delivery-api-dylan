package com.deliverytech.delivery.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

import com.deliverytech.delivery.validation.ValidCategoria;
import com.deliverytech.delivery.validation.ValidTelefone;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para criação e atualização de restaurante")
public class RestauranteRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    @Schema(description = "Nome do restaurante", example = "Pizza Express", required = true)
    private String nome;

    @NotBlank(message = "Categoria é obrigatória")
    @ValidCategoria(message = "Categoria deve ser uma das opções válidas: Pizzaria, Hamburgueria, Japonesa, Italiana, Brasileira, Chinesa, Mexicana, Lanchonete, Sorveteria, Padaria")
    @Schema(description = "Categoria do restaurante", example = "Pizzaria", required = true,
            allowableValues = {"Pizzaria", "Hamburgueria", "Japonesa", "Italiana", "Brasileira", "Chinesa", "Mexicana", "Lanchonete", "Sorveteria", "Padaria"})
    private String categoria;

    @NotBlank(message = "Telefone é obrigatório")
    @ValidTelefone(message = "Telefone deve ter formato válido (10-11 dígitos)")
    @Schema(description = "Telefone do restaurante", example = "(11) 99999-9999", required = true)
    private String telefone;

    @NotNull(message = "Taxa de entrega é obrigatória")
    @DecimalMin(value = "0.01", message = "Taxa de entrega deve ser um valor positivo")
    @DecimalMax(value = "50.00", message = "Taxa de entrega deve ser no máximo R$ 50,00")
    @Schema(description = "Taxa de entrega do restaurante", example = "5.50", required = true)
    private BigDecimal taxaEntrega;

    @NotNull(message = "Tempo de entrega é obrigatório")
    @Min(value = 10, message = "Tempo de entrega deve ser no mínimo 10 minutos")
    @Max(value = 120, message = "Tempo de entrega deve ser no máximo 120 minutos")
    @Schema(description = "Tempo estimado de entrega em minutos", example = "30", required = true)
    private Integer tempoEntregaMinutos;
}
