package com.deliverytech.delivery.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.deliverytech.delivery.exception.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex, WebRequest request) {
                log.warn("Entidade não encontrada: {} - URI: {}", ex.getMessage(), request.getDescription(false));
                ErrorResponse errorResponse = ErrorResponse.notFound(ex.getMessage(), request.getDescription(false));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

    @ExceptionHandler(ConflictException.class)
        public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex, WebRequest request) {
                log.warn("Erro de conflito: {} - URI: {}", ex.getMessage(), request.getDescription(false));
                ErrorResponse errorResponse = ErrorResponse.conflict(ex.getMessage(), request.getDescription(false));
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }

    @ExceptionHandler(ValidationException.class)
        public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex, WebRequest request) {
                log.warn("Erro de validação: {} - URI: {}", ex.getMessage(), request.getDescription(false));
                ErrorResponse errorResponse = ErrorResponse.of(400, "VALIDATION_ERROR", ex.getMessage(), request.getDescription(false));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

    @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, WebRequest request) {
                log.warn("Erro de negócio: {} - URI: {}", ex.getMessage(), request.getDescription(false));
                ErrorResponse errorResponse = ErrorResponse.of(422, "UNPROCESSABLE_ENTITY", ex.getMessage(), request.getDescription(false));
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
        }

    @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
                log.warn("Erro de validação de argumentos - URI: {} - Erros: {}", request.getDescription(false), ex.getBindingResult().getErrorCount());
                List<ErrorResponse.ErrorDetail> errorDetails = ex.getBindingResult().getAllErrors().stream()
                                .map(error -> {
                                        String fieldName = ((FieldError) error).getField();
                                        String errorMessage = error.getDefaultMessage();
                                        String rejectedValue = ((FieldError) error).getRejectedValue() != null ? ((FieldError) error).getRejectedValue().toString() : null;
                                        return ErrorResponse.ErrorDetail.builder()
                                                        .field(fieldName)
                                                        .message(errorMessage)
                                                        .rejectedValue(rejectedValue)
                                                        .build();
                                })
                                .collect(Collectors.toList());
                ErrorResponse errorResponse = ErrorResponse.validation("Erro de validação nos campos", errorDetails, request.getDescription(false));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

    @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
                log.warn("Violação de integridade de dados: {} - URI: {}", ex.getMessage(), request.getDescription(false));
                String message = "Erro de integridade de dados. Verifique se os dados não estão duplicados ou violam restrições.";
                ErrorResponse errorResponse = ErrorResponse.conflict(message, request.getDescription(false));
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }

    @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
                log.warn("Erro de parsing JSON: {} - URI: {}", ex.getMessage(), request.getDescription(false));
                String message = "Formato de dados inválido. Verifique se o JSON está bem formado.";
                ErrorResponse errorResponse = ErrorResponse.of(400, "INVALID_FORMAT", message, request.getDescription(false));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, WebRequest request) {
                log.warn("Método HTTP não suportado: {} - URI: {}", ex.getMethod(), request.getDescription(false));
                String message = String.format("Método '%s' não é suportado para este endpoint. Métodos permitidos: %s", ex.getMethod(), String.join(", ", ex.getSupportedMethods()));
                ErrorResponse errorResponse = ErrorResponse.of(405, "METHOD_NOT_ALLOWED", message, request.getDescription(false));
                return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
        }

    @ExceptionHandler(MissingServletRequestParameterException.class)
        public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex, WebRequest request) {
                log.warn("Parâmetro obrigatório ausente: {} - URI: {}", ex.getParameterName(), request.getDescription(false));
                String message = String.format("Parâmetro obrigatório '%s' está ausente", ex.getParameterName());
                ErrorResponse errorResponse = ErrorResponse.of(400, "MISSING_PARAMETER", message, request.getDescription(false));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
                log.warn("Erro de tipo de argumento: {} - URI: {}", ex.getName(), request.getDescription(false));
                String message = String.format("Valor '%s' não é válido para o parâmetro '%s'. Tipo esperado: %s", ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());
                ErrorResponse errorResponse = ErrorResponse.of(400, "TYPE_MISMATCH", message, request.getDescription(false));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

    @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, WebRequest request) {
                log.error("Erro interno do servidor - URI: {} - Erro: {}", request.getDescription(false), ex.getMessage(), ex);
                ErrorResponse errorResponse = ErrorResponse.internal("Erro interno do servidor. Tente novamente mais tarde.", request.getDescription(false));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
}
