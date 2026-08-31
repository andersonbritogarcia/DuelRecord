package com.duelrecord.app.infrastructure.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Objects;

/**
 * Resolves the request locale from the {@code Accept-Language} header, restricted to the
 * languages supported by the application ({@code en}, {@code pt}, {@code fr}).
 * <p>
 * Any Portuguese variant (e.g. {@code pt-PT}, {@code pt-AO}, or bare {@code pt}) is normalized
 * to {@code pt-BR}, since that is the only Portuguese bundle maintained by the application.
 * When the header is missing or none of the requested languages are supported, falls back to
 * {@link Locale#ENGLISH}.
 */
public class AppLocaleResolver extends AcceptHeaderLocaleResolver {

    public static final Locale PORTUGUESE_BRAZIL = Locale.of("pt", "BR");

    public AppLocaleResolver() {
        setDefaultLocale(Locale.ENGLISH);
        setSupportedLocales(java.util.List.of(Locale.ENGLISH, PORTUGUESE_BRAZIL, Locale.FRENCH));
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        Enumeration<Locale> requestedLocales = request.getLocales();
        if (Objects.isNull(requestedLocales)) {
            return getDefaultLocale();
        }

        for (Locale requested : Collections.list(requestedLocales)) {
            var supported = matchSupportedLocale(requested);
            if (Objects.nonNull(supported)) {
                return supported;
            }
        }

        return getDefaultLocale();
    }

    private Locale matchSupportedLocale(Locale requested) {
        return switch (requested.getLanguage()) {
            case "pt" -> PORTUGUESE_BRAZIL;
            case "en" -> Locale.ENGLISH;
            case "fr" -> Locale.FRENCH;
            default -> null;
        };
    }
}
