package com.duelrecord.app.shared.exceptions;

/**
 * Signals a business rule violation. The {@code messageKey} is resolved to a localized message
 * (per request locale) by {@link AppExceptionHandler}, rather than being shown to the client as-is.
 */
public class BusinessException extends RuntimeException implements LocalizedException {

    private final transient Object[] args;

    public BusinessException(String messageKey, Object... args) {
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
