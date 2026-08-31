package com.duelrecord.app.shared.exceptions;

/**
 * Signals that a domain entity could not be found. The {@code messageKey} is resolved to a
 * localized message (per request locale) by {@link AppExceptionHandler}, which maps this
 * exception to {@code 404 Not Found}.
 * <p>
 * Distinct from {@link jakarta.persistence.EntityNotFoundException}, which is thrown by the
 * persistence layer (e.g. JPA) and always maps to a generic, non-parameterized 404 message.
 */
public class EntityNotFoundException extends RuntimeException implements LocalizedException {

    private final transient Object[] args;

    public EntityNotFoundException(String messageKey, Object... args) {
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
