package com.ravzeno.droppers_service.commons.exception.core;

public class SecurityViolationException extends RuntimeException {
    public SecurityViolationException(String message) {
        super(message);
    }
}
