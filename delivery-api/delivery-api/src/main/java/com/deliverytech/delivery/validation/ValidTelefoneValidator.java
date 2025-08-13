package com.deliverytech.delivery.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class ValidTelefoneValidator implements ConstraintValidator<ValidTelefone, String> {

    @Override
    public void initialize(ValidTelefone constraintAnnotation) {
        // Initialization if needed
    }

    @Override
    public boolean isValid(String telefone, ConstraintValidatorContext context) {
        if (telefone == null) {
            return true; // Let @NotBlank handle null validation
        }
        
        // Remove espaços, parênteses, hífens e outros caracteres
        String cleanTelefone = telefone.replaceAll("[\\s()+-]", "");
        
        // Verifica se tem 10 ou 11 dígitos (telefone brasileiro)
        if (cleanTelefone.length() < 10 || cleanTelefone.length() > 11) {
            return false;
        }
        
        // Verifica se todos são dígitos
        if (!cleanTelefone.matches("\\d+")) {
            return false;
        }
        
        // Verifica se começa com código de área válido (11-99)
        if (cleanTelefone.length() >= 2) {
            String codigoArea = cleanTelefone.substring(0, 2);
            int codigo = Integer.parseInt(codigoArea);
            if (codigo < 11 || codigo > 99) {
                return false;
            }
        }
        
        // Para celular (11 dígitos), o terceiro dígito deve ser 9
        if (cleanTelefone.length() == 11) {
            char terceiroDig = cleanTelefone.charAt(2);
            return terceiroDig == '9';
        }
        
        return true;
    }
}
