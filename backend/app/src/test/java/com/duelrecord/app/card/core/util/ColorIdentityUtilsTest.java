package com.duelrecord.app.card.core.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ColorIdentityUtilsTest {

    @Test
    void shouldCanonicalizeCollectionInWubrgOrder() {
        assertEquals("UBG", ColorIdentityUtils.canonicalize(List.of("G", "B", "U")));
        assertEquals("WUR", ColorIdentityUtils.canonicalize(List.of("R", "W", "U")));
        assertEquals("WUBRG", ColorIdentityUtils.canonicalize(List.of("G", "R", "B", "U", "W")));
        assertEquals("W", ColorIdentityUtils.canonicalize(List.of("w")));
        assertEquals("", ColorIdentityUtils.canonicalize(List.of()));
        assertEquals("", ColorIdentityUtils.canonicalize((List<String>) null));
    }

    @Test
    void shouldCanonicalizeStringInWubrgOrder() {
        assertEquals("UBG", ColorIdentityUtils.canonicalize("gbu"));
        assertEquals("WUR", ColorIdentityUtils.canonicalize("r, w, u"));
        assertEquals("WUBRG", ColorIdentityUtils.canonicalize("WUBRG"));
        assertEquals("", ColorIdentityUtils.canonicalize(""));
        assertEquals("", ColorIdentityUtils.canonicalize((String) null));
    }
}
