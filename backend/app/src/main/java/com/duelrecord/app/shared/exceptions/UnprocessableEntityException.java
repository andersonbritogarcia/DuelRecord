package com.duelrecord.app.shared.exceptions;

/**
 * Signals that a request is syntactically valid but semantically unprocessable. The
 * {@code messageKey} is resolved to a localized message (per request locale) by
 * {@link AppExceptionHandler}.
 */
public class UnprocessableEntityException extends RuntimeException implements LocalizedException {

    private final transient Object[] args;

    public UnprocessableEntityException(String messageKey, Object... args) {
        super(messageKey);
        this.args = args;
    }

    @Override
    public String messageKey() {
        return getMessage();
    }

    @Override
    public Object[] args() {
        return args;
    }
}
