package com.duelrecord.app.shared.exceptions;

/**
 * Signals that an entity already exists and cannot be created again. The {@code messageKey} is
 * resolved to a localized message (per request locale) by {@link AppExceptionHandler}.
 */
public class EntityAlreadyExistsException extends RuntimeException implements LocalizedException {

    private final transient Object[] args;

    public EntityAlreadyExistsException(String messageKey, Object... args) {
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
