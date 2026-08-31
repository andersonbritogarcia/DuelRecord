package com.duelrecord.app.shared.exceptions;

public record FieldError(String name, String message) {

    public FieldError(org.springframework.validation.FieldError fieldError) {
        this(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
