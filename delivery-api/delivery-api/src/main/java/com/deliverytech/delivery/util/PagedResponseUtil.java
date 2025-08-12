package com.deliverytech.delivery.util;

import com.deliverytech.delivery.dto.response.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public class PagedResponseUtil {
    
    public static <T> PagedResponse<T> createPagedResponse(Page<T> page) {
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .page(PagedResponse.PageInfo.builder()
                        .number(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .first(page.isFirst())
                        .last(page.isLast())
                        .build())
                .links(createPageLinks(page))
                .build();
    }
    
    private static <T> PagedResponse.PageLinks createPageLinks(Page<T> page) {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentRequest()
                .replaceQueryParam("page")
                .replaceQueryParam("size")
                .toUriString();
        
        PagedResponse.PageLinks.PageLinksBuilder linksBuilder = PagedResponse.PageLinks.builder();
        
        // First page link
        linksBuilder.first(baseUrl + "?page=0&size=" + page.getSize());
        
        // Last page link
        linksBuilder.last(baseUrl + "?page=" + (page.getTotalPages() - 1) + "&size=" + page.getSize());
        
        // Next page link
        if (page.hasNext()) {
            linksBuilder.next(baseUrl + "?page=" + (page.getNumber() + 1) + "&size=" + page.getSize());
        }
        
        // Previous page link
        if (page.hasPrevious()) {
            linksBuilder.prev(baseUrl + "?page=" + (page.getNumber() - 1) + "&size=" + page.getSize());
        }
        
        return linksBuilder.build();
    }
}
