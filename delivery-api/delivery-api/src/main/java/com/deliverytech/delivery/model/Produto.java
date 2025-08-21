package com.deliverytech.delivery.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produto {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String categoria;

    private String descricao;

    private BigDecimal preco;

    @Builder.Default
    private Boolean disponivel = true;

    @Builder.Default
    @Column(nullable = false)
    private Integer estoque = 0;

    @ManyToOne
    @JoinColumn(name = "restaurante_id")
    private Restaurante restaurante;
}
