package com.duelrecord.app.player.web.controller;

import com.duelrecord.app.identity.AuthenticatedUserDto;
import com.duelrecord.app.identity.IdentityApi;
import com.duelrecord.app.player.core.usecase.GetPlayerByUserIdUseCase;
import com.duelrecord.app.player.core.usecase.UpdatePlayerProfileInput;
import com.duelrecord.app.player.core.usecase.UpdatePlayerProfileUseCase;
import com.duelrecord.app.player.web.dto.PlayerResponse;
import com.duelrecord.app.player.web.dto.UpdatePlayerProfileRequest;
import com.duelrecord.app.shared.exceptions.EntityNotFoundException;
import com.duelrecord.app.shared.utils.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@RestController
@RequestMapping("/api/players/me")
@RequiredArgsConstructor
public class PlayerMeController {

    private final IdentityApi identityApi;
    private final GetPlayerByUserIdUseCase getPlayerByUserIdUseCase;
    private final UpdatePlayerProfileUseCase updatePlayerProfileUseCase;
    private final MessageSource messageSource;

    @GetMapping
    public PlayerResponse getMyPlayer(@AuthenticationPrincipal Jwt jwt) {
        AuthenticatedUserDto user = resolveAuthenticatedUser(jwt);
        return getPlayerByUserIdUseCase
                .execute(user.id())
                .map(PlayerResponse::fromDomain)
                .orElseThrow(() -> new EntityNotFoundException("problem.playerNotFound.detail", user.id()));
    }

    @PutMapping
    public PlayerResponse updateMyPlayer(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdatePlayerProfileRequest request) {
        AuthenticatedUserDto user = resolveAuthenticatedUser(jwt);
        var updated = updatePlayerProfileUseCase.execute(new UpdatePlayerProfileInput(user.id(),
                                                                                      request.displayName(),
                                                                                      request.mtgoUsername(),
                                                                                      request.arenaUsername(),
                                                                                      request.cityId(),
                                                                                      request.newCountryCode(),
                                                                                      request.newCityName()));
        return PlayerResponse.fromDomain(updated);
    }

    private AuthenticatedUserDto resolveAuthenticatedUser(Jwt jwt) {
        if (Objects.isNull(jwt)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                                              messageSource.getMessage("problem.unauthenticated.detail", null, LocaleContextHolder.getLocale()));
        }

        var userInfo = JwtUtils.extractUserInfo(jwt);
        return identityApi.resolveUser(userInfo.sub(), userInfo.email(), userInfo.name());
    }
}
