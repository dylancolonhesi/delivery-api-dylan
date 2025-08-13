package com.deliverytech.delivery.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidTelefoneValidator.class)
public @interface ValidTelefone {
    String message() default "Telefone deve ter formato válido (10-11 dígitos)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
