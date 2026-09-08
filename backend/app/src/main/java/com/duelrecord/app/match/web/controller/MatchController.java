package com.duelrecord.app.match.web.controller;

import com.duelrecord.app.identity.AuthenticatedUserDto;
import com.duelrecord.app.identity.IdentityApi;
import com.duelrecord.app.match.core.model.GameFormat;
import com.duelrecord.app.match.core.model.Platform;
import com.duelrecord.app.match.core.usecase.FindMatchByIdUseCase;
import com.duelrecord.app.match.core.usecase.GameInput;
import com.duelrecord.app.match.core.usecase.ListMatchesInput;
import com.duelrecord.app.match.core.usecase.ListMatchesUseCase;
import com.duelrecord.app.match.core.usecase.RecordMatchInput;
import com.duelrecord.app.match.core.usecase.RecordMatchUseCase;
import com.duelrecord.app.match.core.usecase.TournamentParticipationInput;
import com.duelrecord.app.match.persistence.model.Match;
import com.duelrecord.app.match.web.dto.MatchResponse;
import com.duelrecord.app.match.web.dto.RecordMatchRequest;
import com.duelrecord.app.shared.exceptions.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final RecordMatchUseCase recordMatchUseCase;
    private final FindMatchByIdUseCase findMatchByIdUseCase;
    private final ListMatchesUseCase listMatchesUseCase;
    private final IdentityApi identityApi;
    private final MessageSource messageSource;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public MatchResponse recordMatch(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody RecordMatchRequest request
    ) {
        AuthenticatedUserDto user = resolveAuthenticatedUser(jwt);

        TournamentParticipationInput tournamentData = null;
        if (request.tournament() != null) {
            tournamentData = new TournamentParticipationInput(
                    request.tournament().tournamentName(),
                    request.tournament().placement(),
                    request.tournament().storeName(),
                    request.tournament().swissRounds(),
                    request.tournament().notes(),
                    request.tournament().playedAt()
            );
        }

        List<GameInput> games = null;
        if (request.games() != null) {
            games = request.games().stream()
                    .map(g -> new GameInput(
                            g.gameNumber(),
                            g.winnerPlayerId(),
                            g.startingPlayerId(),
                            g.isDraw(),
                            g.durationSeconds(),
                            g.notes()
                    ))
                    .toList();
        }

        RecordMatchInput input = new RecordMatchInput(
                request.format(),
                request.platform(),
                request.matchStructure(),
                request.playedAt(),
                request.round(),
                request.notes(),
                request.source(),
                request.verificationStatus(),
                request.tournamentParticipationId(),
                tournamentData,
                request.seat1().playerId(),
                request.seat1().deckIdentityId(),
                request.seat2().playerId(),
                request.seat2().deckIdentityId(),
                request.scorePreset(),
                request.isIntentionalDraw(),
                games,
                user.id()
        );

        Match match = recordMatchUseCase.execute(input);
        return MatchResponse.fromEntity(match);
    }

    @GetMapping("/{id}")
    public MatchResponse getMatchById(@PathVariable UUID id) {
        Match match = findMatchByIdUseCase.execute(id)
                .orElseThrow(() -> new EntityNotFoundException("problem.match.notFound", id));
        return MatchResponse.fromEntity(match);
    }

    @GetMapping
    public Page<MatchResponse> listMatches(
            @RequestParam(required = false) UUID playerId,
            @RequestParam(required = false) GameFormat format,
            @RequestParam(required = false) Platform platform,
            Pageable pageable
    ) {
        return listMatchesUseCase.execute(new ListMatchesInput(playerId, format, platform, pageable))
                .map(MatchResponse::fromEntity);
    }

    private AuthenticatedUserDto resolveAuthenticatedUser(Jwt jwt) {
        if (Objects.isNull(jwt)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    messageSource.getMessage("problem.unauthenticated.detail", null, LocaleContextHolder.getLocale()));
        }

        var sub = jwt.getSubject();
        var email = jwt.getClaimAsString("email");
        var name = jwt.getClaimAsString("name");
        if (name == null || name.isBlank()) {
            name = jwt.getClaimAsString("given_name");
        }
        return identityApi.resolveUser(sub, email, name);
    }
}
