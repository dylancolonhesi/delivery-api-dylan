package com.deliverytech.delivery.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidCategoriaValidator.class)
public @interface ValidCategoria {
    String message() default "Categoria deve ser uma das opções válidas";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
