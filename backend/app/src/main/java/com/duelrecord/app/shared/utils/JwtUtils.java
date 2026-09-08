package com.duelrecord.app.shared.utils;

import org.springframework.security.oauth2.jwt.Jwt;

public interface JwtUtils {

    record JwtUserInfo(String sub, String email, String name) {
    }

    static JwtUserInfo extractUserInfo(Jwt jwt) {
        if (jwt == null) {
            return null;
        }

        var sub = jwt.getSubject();
        var email = jwt.getClaimAsString("email");
        var name = jwt.getClaimAsString("name");
        if (ValidationUtils.isBlank(name)) {
            name = jwt.getClaimAsString("given_name");
        }
        return new JwtUserInfo(sub, email, name);
    }
}
