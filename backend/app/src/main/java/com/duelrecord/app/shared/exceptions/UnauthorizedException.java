package com.duelrecord.app.shared.exceptions;

/**
 * Signals that the request is not authenticated (e.g. missing/invalid credentials or claims).
 * The {@code messageKey} is resolved to a localized message (per request locale) by
 * {@link AppExceptionHandler}, which maps this exception to {@code 401 Unauthorized}.
 */
public class UnauthorizedException extends RuntimeException implements LocalizedException {

    private final transient Object[] args;

    public UnauthorizedException(String messageKey, Object... args) {
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
