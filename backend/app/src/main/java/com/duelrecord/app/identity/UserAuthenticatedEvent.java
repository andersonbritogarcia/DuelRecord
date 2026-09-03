package com.duelrecord.app.identity;

import java.util.UUID;

public record UserAuthenticatedEvent(UUID userId, String email, String name) {
}
