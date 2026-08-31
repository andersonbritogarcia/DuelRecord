package com.duelrecord.app.shared.exceptions;

/**
 * Contract for exceptions whose user-facing message should be resolved from the application's
 * message bundles ({@code i18n/messages*.properties}) according to the current request locale,
 * instead of carrying a fixed, non-localizable message.
 */
public interface LocalizedException {

    /**
     * The message bundle key used to resolve the localized detail message.
     */
    String messageKey();

    /**
     * Optional arguments used to format the resolved message (see {@link java.text.MessageFormat}).
     */
    Object[] args();
}
