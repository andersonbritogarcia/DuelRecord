package com.duelrecord.app.player.core.usecase;

import org.springframework.data.domain.Pageable;

public record SearchPlayersInput(String query, Pageable pageable) {
}
