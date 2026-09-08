package com.duelrecord.app.match.core.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum MatchScorePreset {
    TWO_ZERO("2-0", 2, 0, false, false),
    TWO_ONE("2-1", 2, 1, false, false),
    ONE_TWO("1-2", 1, 2, false, false),
    ZERO_TWO("0-2", 0, 2, false, false),
    ONE_ZERO("1-0", 1, 0, false, false),
    ZERO_ONE("0-1", 0, 1, false, false),
    ONE_ONE("1-1", 1, 1, true, false),
    ZERO_ZERO("0-0", 0, 0, true, false),
    INTENTIONAL_DRAW("ID", 0, 0, true, true);

    private final String code;
    private final int seat1Wins;
    private final int seat2Wins;
    private final boolean draw;
    private final boolean intentionalDraw;

    public static Optional<MatchScorePreset> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String clean = code.trim().toUpperCase();
        if ("INTENTIONAL_DRAW".equals(clean) || "ID".equals(clean)) {
            return Optional.of(INTENTIONAL_DRAW);
        }
        return Arrays.stream(values())
                .filter(p -> p.getCode().equalsIgnoreCase(clean) || p.name().equalsIgnoreCase(clean))
                .findFirst();
    }
}
