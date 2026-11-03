package com.ravzeno.droppers_service.commons.dto;

public record SuccessResponse<T>(String status, T data) {
    public static <T> SuccessResponse<T> create(T data) {
        return new SuccessResponse<>("SUCCESS", data);
    }

    public static SuccessResponse<Void> create() {
        return create(null);
    }
}
