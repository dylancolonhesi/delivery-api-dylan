package com.deliverytech.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta paginada da API")
public class PagedResponse<T> {
    
    @Schema(description = "Lista de itens da página atual")
    private List<T> content;
    
    @Schema(description = "Informações sobre a paginação")
    private PageInfo page;
    
    @Schema(description = "Links de navegação")
    private PageLinks links;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Informações da página")
    public static class PageInfo {
        @Schema(description = "Número da página atual (0-indexed)", example = "0")
        private int number;
        
        @Schema(description = "Tamanho da página", example = "10")
        private int size;
        
        @Schema(description = "Total de elementos", example = "50")
        private long totalElements;
        
        @Schema(description = "Total de páginas", example = "5")
        private int totalPages;
        
        @Schema(description = "Indica se é a primeira página", example = "true")
        private boolean first;
        
        @Schema(description = "Indica se é a última página", example = "false")
        private boolean last;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Links de navegação da paginação")
    public static class PageLinks {
        @Schema(description = "Link para a primeira página")
        private String first;
        
        @Schema(description = "Link para a última página")
        private String last;
        
        @Schema(description = "Link para a próxima página")
        private String next;
        
        @Schema(description = "Link para a página anterior")
        private String prev;
    }
}
