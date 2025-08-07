package com.deliverytech.delivery.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurante {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String categoria;

    private String telefone;

    private BigDecimal taxaEntrega;

    private Integer tempoEntregaMinutos;

    private Integer avaliacao;

    private Boolean ativo = true;
}
