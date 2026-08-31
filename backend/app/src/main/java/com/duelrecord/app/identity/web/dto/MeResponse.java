package com.duelrecord.app.identity.web.dto;

import com.duelrecord.app.identity.persistence.model.User;
import com.duelrecord.app.identity.persistence.model.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record MeResponse(UUID id, String email, String authProviderId, UserStatus status, Instant createdAt, Instant updatedAt) {

    public static MeResponse fromDomain(User user) {
        return new MeResponse(user.getId(),
                              user.getEmail(),
                              user.getAuthProviderId(),
                              user.getStatus(),
                              user.getCreatedAt(),
                              user.getUpdatedAt());
    }
}
