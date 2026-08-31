package com.duelrecord.app.shared.exceptions;

/**
 * Signals an unexpected internal error. The {@code messageKey} is resolved to a localized
 * message (per request locale) by {@link AppExceptionHandler}; the raw key/args are also logged
 * for diagnostics.
 */
public class InternalServerException extends RuntimeException implements LocalizedException {

    private final transient Object[] args;

    public InternalServerException(String messageKey, Object... args) {
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
