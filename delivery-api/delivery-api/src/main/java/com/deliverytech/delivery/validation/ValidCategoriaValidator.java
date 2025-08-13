package com.deliverytech.delivery.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
public class ValidCategoriaValidator implements ConstraintValidator<ValidCategoria, String> {

    private static final Set<String> CATEGORIAS_VALIDAS = Set.of(
        "Pizzaria", "Hamburgueria", "Japonesa", "Italiana", "Brasileira", 
        "Chinesa", "Mexicana", "Lanchonete", "Sorveteria", "Padaria"
    );

    @Override
    public void initialize(ValidCategoria constraintAnnotation) {
        // Initialization if needed
    }

    @Override
    public boolean isValid(String categoria, ConstraintValidatorContext context) {
        if (categoria == null) {
            return true; // Let @NotBlank handle null validation
        }
        
        return CATEGORIAS_VALIDAS.contains(categoria);
    }
}
