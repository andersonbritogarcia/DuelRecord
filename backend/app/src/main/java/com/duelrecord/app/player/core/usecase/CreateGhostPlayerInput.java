package com.duelrecord.app.player.core.usecase;

import java.util.UUID;

public record CreateGhostPlayerInput(String name, String displayName, String mtgoUsername, String arenaUsername, UUID cityId) {
}
