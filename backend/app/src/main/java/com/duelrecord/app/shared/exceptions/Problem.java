package com.duelrecord.app.shared.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Problem(int status, String title, String detail, List<FieldError> fieldErrors) {

    public Problem(HttpStatus status, String title, String detail) {
        this(status.value(), title, detail, null);
    }

    public Problem(HttpStatus status, String title, String detail, List<FieldError> fieldErrors) {
        this(status.value(), title, detail, fieldErrors);
    }
}
