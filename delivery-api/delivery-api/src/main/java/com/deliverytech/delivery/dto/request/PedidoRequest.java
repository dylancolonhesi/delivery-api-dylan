package com.deliverytech.delivery.dto.request;

import com.deliverytech.delivery.model.Endereco;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para criação de pedido")
public class PedidoRequest {

    @NotNull(message = "Cliente ID é obrigatório")
    @Schema(description = "ID do cliente que está fazendo o pedido", example = "1", required = true)
    private Long clienteId;

    @NotNull(message = "Restaurante ID é obrigatório")
    @Schema(description = "ID do restaurante onde o pedido será feito", example = "1", required = true)
    private Long restauranteId;

    @NotNull(message = "Endereço de entrega é obrigatório")
    @Valid
    @Schema(description = "Endereço completo para entrega do pedido", required = true)
    private Endereco enderecoEntrega;

    @NotNull(message = "Lista de itens é obrigatória")
    @NotEmpty(message = "Lista de itens não pode estar vazia")
    @Valid
    @Schema(description = "Lista de itens do pedido", required = true)
    private List<ItemPedidoRequest> itens;
}
