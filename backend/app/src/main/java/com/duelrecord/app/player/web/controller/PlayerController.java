package com.duelrecord.app.player.web.controller;

import com.duelrecord.app.player.core.usecase.*;
import com.duelrecord.app.player.web.dto.CreateGhostPlayerRequest;
import com.duelrecord.app.player.web.dto.PlayerResponse;
import com.duelrecord.app.shared.exceptions.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

    private final SearchPlayersUseCase searchPlayersUseCase;
    private final FindPlayerByIdUseCase findPlayerByIdUseCase;
    private final CreateGhostPlayerUseCase createGhostPlayerUseCase;

    @GetMapping
    public Page<PlayerResponse> searchPlayers(@RequestParam(name = "q", required = false) String query,
                                              @PageableDefault(size = 20) Pageable pageable) {
        return searchPlayersUseCase.execute(new SearchPlayersInput(query, pageable)).map(PlayerResponse::fromDomain);
    }

    @GetMapping("/{id}")
    public PlayerResponse getPlayerById(@PathVariable("id") UUID id) {
        return findPlayerByIdUseCase.execute(id)
                                    .map(PlayerResponse::fromDomain)
                                    .orElseThrow(() -> new EntityNotFoundException("problem.playerNotFound.detail", id));
    }

    @PostMapping("/ghost")
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerResponse createGhostPlayer(@Valid @RequestBody CreateGhostPlayerRequest request) {
        var player = createGhostPlayerUseCase.execute(new CreateGhostPlayerInput(request.name(),
                                                                                 request.displayName(),
                                                                                 request.mtgoUsername(),
                                                                                 request.arenaUsername(),
                                                                                 request.cityId()));
        return PlayerResponse.fromDomain(player);
    }
}
