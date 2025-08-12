package com.deliverytech.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta de erro da API")
public class ErrorResponse {
    
    @Schema(description = "Código de status HTTP", example = "400")
    private int status;
    
    @Schema(description = "Código de erro interno", example = "VALIDATION_ERROR")
    private String code;
    
    @Schema(description = "Mensagem de erro principal", example = "Dados inválidos")
    private String message;
    
    @Schema(description = "Detalhes específicos do erro")
    private List<ErrorDetail> details;
    
    @Schema(description = "Timestamp do erro", example = "2024-01-15T10:30:00")
    private LocalDateTime timestamp;
    
    @Schema(description = "Caminho da requisição", example = "/api/v1/restaurantes")
    private String path;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Detalhe específico do erro")
    public static class ErrorDetail {
        @Schema(description = "Campo relacionado ao erro", example = "email")
        private String field;
        
        @Schema(description = "Valor rejeitado", example = "invalid-email")
        private String rejectedValue;
        
        @Schema(description = "Mensagem do erro", example = "Email deve ter formato válido")
        private String message;
    }
    
    // Métodos de conveniência para criação rápida de ErrorResponse
    public static ErrorResponse of(int status, String code, String message, String path) {
        return ErrorResponse.builder()
                .status(status)
                .code(code)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse validation(String message, List<ErrorDetail> details, String path) {
        return ErrorResponse.builder()
                .status(400)
                .code("VALIDATION_ERROR")
                .message(message)
                .details(details)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse notFound(String message, String path) {
        return ErrorResponse.builder()
                .status(404)
                .code("NOT_FOUND")
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse conflict(String message, String path) {
        return ErrorResponse.builder()
                .status(409)
                .code("CONFLICT")
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse internal(String message, String path) {
        return ErrorResponse.builder()
                .status(500)
                .code("INTERNAL_ERROR")
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
