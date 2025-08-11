package com.deliverytech.delivery.service.validation;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

import com.deliverytech.delivery.model.Endereco;
import com.deliverytech.delivery.model.Restaurante;

@Component
public class EntregaValidator {

    public boolean restauranteEntregaNoEndereco(Restaurante restaurante, Endereco endereco) {
        return endereco != null && endereco.getCidade() != null && !endereco.getCidade().trim().isEmpty();
    }

    public BigDecimal calcularTaxaEntrega(Restaurante restaurante, Endereco endereco) {
        return restaurante.getTaxaEntrega() != null ? restaurante.getTaxaEntrega() : BigDecimal.ZERO;
    }
}
