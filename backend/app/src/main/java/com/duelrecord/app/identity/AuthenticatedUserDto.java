package com.duelrecord.app.identity;

import java.util.UUID;

public record AuthenticatedUserDto(UUID id, String email, String authProviderId) {
}
