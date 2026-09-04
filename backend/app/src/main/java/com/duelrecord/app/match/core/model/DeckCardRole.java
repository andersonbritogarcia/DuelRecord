package com.duelrecord.app.match.core.model;

public enum DeckCardRole {
    COMMANDER,
    PARTNER,
    BACKGROUND,
    COMPANION;

    public boolean isCommandZone() {
        return this == COMMANDER || this == PARTNER || this == BACKGROUND;
    }
}
