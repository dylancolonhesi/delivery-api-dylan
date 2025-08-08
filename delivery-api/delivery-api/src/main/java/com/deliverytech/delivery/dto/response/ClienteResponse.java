package com.deliverytech.delivery.dto.response;

import com.deliverytech.delivery.model.Endereco;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponse {
    private Long id;
    private String nome;
    private String email;
    private Endereco endereco;
    private Boolean ativo;
}
