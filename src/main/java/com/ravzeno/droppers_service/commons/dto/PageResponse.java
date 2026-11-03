package com.ravzeno.droppers_service.commons.dto;

import java.util.List;

public record PageResponse<T>(List<T> items, int totalPages, long totalElement) {

    public static <T> PageResponse<T> create(
            List<T> items, long totalElement, PageOption pageOption) {
        int totalPages = (int) Math.ceil((double) totalElement / pageOption.getPageSize());
        return new PageResponse<>(items, totalPages, totalElement);
    }
}
