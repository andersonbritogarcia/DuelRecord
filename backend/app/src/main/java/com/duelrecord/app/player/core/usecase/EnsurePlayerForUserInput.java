package com.duelrecord.app.player.core.usecase;

import java.util.UUID;

public record EnsurePlayerForUserInput(UUID userId, String email, String name) {
}
