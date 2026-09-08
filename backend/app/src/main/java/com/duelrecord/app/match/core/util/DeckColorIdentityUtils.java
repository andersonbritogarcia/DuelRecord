package com.duelrecord.app.match.core.util;

import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DeckColorIdentityUtils {

    private static final List<Character> CANONICAL_ORDER = List.of('W', 'U', 'B', 'R', 'G');
    public static final String COLORLESS = "C";

    private DeckColorIdentityUtils() {
    }

    public static String combine(Collection<String> colorIdentities) {
        if (CollectionUtils.isEmpty(colorIdentities)) {
            return COLORLESS;
        }

        Set<Character> foundColors = new HashSet<>();
        for (String ci : colorIdentities) {
            if (ci != null) {
                for (char c : ci.toUpperCase().toCharArray()) {
                    if (CANONICAL_ORDER.contains(c)) {
                        foundColors.add(c);
                    }
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (char c : CANONICAL_ORDER) {
            if (foundColors.contains(c)) {
                sb.append(c);
            }
        }

        return sb.length() > 0 ? sb.toString() : COLORLESS;
    }
}
