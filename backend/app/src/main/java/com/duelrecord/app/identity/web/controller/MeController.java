package com.duelrecord.app.identity.web.controller;

import com.duelrecord.app.identity.core.usecase.ResolveAuthenticatedUserUseCase;
import com.duelrecord.app.identity.persistence.model.GoogleUserPrincipal;
import com.duelrecord.app.identity.web.dto.MeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/me")
public class MeController {

    private final ResolveAuthenticatedUserUseCase resolveAuthenticatedUserUseCase;
    private final MessageSource messageSource;

    @GetMapping
    public MeResponse getMe(@AuthenticationPrincipal Jwt jwt) {
        if (Objects.isNull(jwt)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, message());
        }

        var sub = jwt.getSubject();
        var email = jwt.getClaimAsString("email");
        var name = jwt.getClaimAsString("name");
        if (name == null || name.isBlank()) {
            name = jwt.getClaimAsString("given_name");
        }
        var user = resolveAuthenticatedUserUseCase.execute(new GoogleUserPrincipal(sub, email, name));
        return MeResponse.fromDomain(user);
    }

    private String message() {
        return messageSource.getMessage("problem.unauthenticated.detail", null, LocaleContextHolder.getLocale());
    }
}
