package com.duelrecord.app.identity.persistence.model;

public record GoogleUserPrincipal(String authProviderId, String email, String name) {

    public GoogleUserPrincipal(String authProviderId, String email) {
        this(authProviderId, email, null);
    }
}
