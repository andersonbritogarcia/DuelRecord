package com.duelrecord.app.card.core.util;


import com.duelrecord.app.shared.utils.ValidationUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class ColorIdentityUtils {

    private static final List<String> CANONICAL_ORDER = List.of("W", "U", "B", "R", "G");

    private ColorIdentityUtils() {
    }

    /**
     * Normalizes a collection of color codes (e.g., ["B", "G", "U"]) into a canonical WUBRG-ordered string (e.g., "UBG").
     * Returns an empty string for colorless cards.
     */
    public static String canonicalize(Collection<String> colors) {
        if (CollectionUtils.isEmpty(colors)) {
            return "";
        }
        return CANONICAL_ORDER.stream().filter(c -> colors.stream().anyMatch(c::equalsIgnoreCase)).collect(Collectors.joining());
    }

    /**
     * Normalizes a string representation of colors (e.g., "gbu" or "B,G,U") into canonical "UBG".
     */
    public static String canonicalize(String colorsString) {
        if (ValidationUtils.isBlank(colorsString)) {
            return "";
        }

        String clean = colorsString.replaceAll("[^wubrgWUBRG]", "").toUpperCase();
        return CANONICAL_ORDER.stream().filter(clean::contains).collect(Collectors.joining());
    }
}
