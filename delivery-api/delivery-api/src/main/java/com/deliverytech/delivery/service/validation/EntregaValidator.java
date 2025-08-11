package com.deliverytech.delivery.service.validation;

import org.springframework.stereotype.Component;

import com.deliverytech.delivery.model.Endereco;
import com.deliverytech.delivery.model.Restaurante;

@Component
public class EntregaValidator {

    public boolean restauranteEntregaNoEndereco(Restaurante restaurante, Endereco endereco) {
        if (endereco == null || endereco.getCidade() == null) {
            return false;
        }
        return true; 
    }

    public java.math.BigDecimal calcularTaxaEntrega(Restaurante restaurante, Endereco endereco) {
        return restaurante.getTaxaEntrega();
    }
}
