package com.deliverytech.delivery.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    private int status;
    
    private String error;
    
    private String message;
    
    private String path;
    
    private List<ErrorDetail> details;
    
    private Map<String, Object> additionalInfo;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetail {
        private String field;
        
        private String message;
        
        private String rejectedValue;
        
        private String code;
    }
    
    public static ErrorResponse of(int status, String error, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(extractPath(path))
                .build();
    }
    
    public static ErrorResponse badRequest(String message, String path) {
        return of(400, "BAD_REQUEST", message, path);
    }
    
    public static ErrorResponse notFound(String message, String path) {
        return of(404, "NOT_FOUND", message, path);
    }
    
    public static ErrorResponse conflict(String message, String path) {
        return of(409, "CONFLICT", message, path);
    }
    
    public static ErrorResponse unprocessableEntity(String message, String path) {
        return of(422, "UNPROCESSABLE_ENTITY", message, path);
    }
    
    public static ErrorResponse internal(String message, String path) {
        return of(500, "INTERNAL_SERVER_ERROR", message, path);
    }
    
    public static ErrorResponse validation(String message, List<ErrorDetail> details, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(400)
                .error("VALIDATION_ERROR")
                .message(message)
                .path(extractPath(path))
                .details(details)
                .build();
    }
    
    public static ErrorResponse fieldValidation(String field, String message, String rejectedValue, String path) {
        List<ErrorDetail> details = new ArrayList<>();
        details.add(ErrorDetail.builder()
                .field(field)
                .message(message)
                .rejectedValue(rejectedValue)
                .code("INVALID_VALUE")
                .build());
                
        return validation("Erro de validação no campo " + field, details, path);
    }
    
    public ErrorResponse addInfo(String key, Object value) {
        if (this.additionalInfo == null) {
            this.additionalInfo = new HashMap<>();
        }
        this.additionalInfo.put(key, value);
        return this;
    }
    
    public ErrorResponse addDetail(ErrorDetail detail) {
        if (this.details == null) {
            this.details = new ArrayList<>();
        }
        this.details.add(detail);
        return this;
    }
    
    public ErrorResponse addDetail(String field, String message, String rejectedValue) {
        return addDetail(ErrorDetail.builder()
                .field(field)
                .message(message)
                .rejectedValue(rejectedValue)
                .build());
    }
    
    private static String extractPath(String fullPath) {
        if (fullPath == null) return null;
        
        if (fullPath.startsWith("uri=")) {
            fullPath = fullPath.substring(4);
        }
        
        int queryIndex = fullPath.indexOf('?');
        if (queryIndex != -1) {
            fullPath = fullPath.substring(0, queryIndex);
        }
        
        return fullPath;
    }
    
    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    public boolean hasDetails() {
        return details != null && !details.isEmpty();
    }
    
    public int getDetailsCount() {
        return details != null ? details.size() : 0;
    }
}
