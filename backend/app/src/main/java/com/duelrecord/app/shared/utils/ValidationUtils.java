package com.duelrecord.app.shared.utils;

import com.duelrecord.app.shared.exceptions.BusinessException;

import java.util.Objects;

public interface ValidationUtils {

    /**
     * @param messageKey message bundle key resolved (per request locale) by
     *                   {@code AppExceptionHandler} when the resulting {@link BusinessException}
     *                   is handled.
     */
    static void requireNonNull(Object obj, String messageKey, Object... args) {
        if (Objects.isNull(obj)) {
            throw new BusinessException(messageKey, args);
        }
    }

    /**
     * @param messageKey message bundle key resolved (per request locale) by
     *                   {@code AppExceptionHandler} when the resulting {@link BusinessException}
     *                   is handled.
     */
    static void requireNonBlank(String str, String messageKey, Object... args) {
        if (Objects.isNull(str) || str.trim().isEmpty()) {
            throw new BusinessException(messageKey, args);
        }
    }

    static boolean isBlank(String str) {
        return Objects.isNull(str) || str.trim().isEmpty();
    }
}
