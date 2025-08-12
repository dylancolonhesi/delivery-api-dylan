package com.deliverytech.delivery.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta padrão da API")
public class ApiResponseDTO<T> {
    
    @Schema(description = "Indica se a operação foi bem-sucedida", example = "true")
    private boolean success;
    
    @Schema(description = "Dados retornados pela operação")
    private T data;
    
    @Schema(description = "Mensagem sobre a operação", example = "Operação realizada com sucesso")
    private String message;
    
    @Schema(description = "Timestamp da resposta", example = "2024-01-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    public static <T> ApiResponseDTO<T> success(T data, String message) {
        return ApiResponseDTO.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> ApiResponseDTO<T> success(T data) {
        return success(data, "Operação realizada com sucesso");
    }
    
    public static <T> ApiResponseDTO<T> error(String message) {
        return ApiResponseDTO.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
