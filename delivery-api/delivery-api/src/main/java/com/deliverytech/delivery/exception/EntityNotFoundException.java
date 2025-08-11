package com.deliverytech.delivery.exception;

public class EntityNotFoundException extends BusinessException {
    
    public EntityNotFoundException(String entity, Long id) {
        super(String.format("%s com ID %d não encontrado", entity, id));
    }
    
    public EntityNotFoundException(String entity, String identifier) {
        super(String.format("%s com identificador %s não encontrado", entity, identifier));
    }
    
    public EntityNotFoundException(String message) {
        super(message);
    }
}
