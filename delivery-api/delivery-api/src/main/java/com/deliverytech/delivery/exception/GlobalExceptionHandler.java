package com.deliverytech.delivery.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.deliverytech.delivery.dto.response.ApiResponseDTO;
import com.deliverytech.delivery.dto.response.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleEntityNotFound(EntityNotFoundException ex, WebRequest request) {
        log.warn("Entidade não encontrada: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.notFound(ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDTO.error(errorResponse.getMessage()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleValidation(ValidationException ex, WebRequest request) {
        log.warn("Erro de validação: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(400, "VALIDATION_ERROR", ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.error(errorResponse.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleBusiness(BusinessException ex, WebRequest request) {
        log.warn("Erro de negócio: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(422, "BUSINESS_ERROR", ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponseDTO.error(errorResponse.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Erro de validação de argumentos");
        
        List<ErrorResponse.ErrorDetail> errorDetails = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    String rejectedValue = ((FieldError) error).getRejectedValue() != null 
                            ? ((FieldError) error).getRejectedValue().toString() 
                            : null;
                    return ErrorResponse.ErrorDetail.builder()
                            .field(fieldName)
                            .message(errorMessage)
                            .rejectedValue(rejectedValue)
                            .build();
                })
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.validation("Erro de validação nos campos", errorDetails, request.getDescription(false));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.error(errorResponse.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleGeneral(Exception ex, WebRequest request) {
        log.error("Erro interno do servidor", ex);
        
        ErrorResponse errorResponse = ErrorResponse.internal("Erro interno do servidor", request.getDescription(false));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.error(errorResponse.getMessage()));
    }
}
