package com.deliverytech.delivery.exception;

public class ConflictException extends BusinessException {
    
    public ConflictException(String message) {
        super(message);
    }
    
    public ConflictException(String entity, String field, String value) {
        super(String.format("%s com %s '%s' já existe", entity, field, value));
    }
    
    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
