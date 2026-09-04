package com.duelrecord.app.match.web.controller;

import com.duelrecord.app.match.core.usecase.FindDeckIdentityByIdUseCase;
import com.duelrecord.app.match.core.usecase.GetOrCreateDeckIdentityInput;
import com.duelrecord.app.match.core.usecase.GetOrCreateDeckIdentityInput.CardRoleInput;
import com.duelrecord.app.match.core.usecase.GetOrCreateDeckIdentityUseCase;
import com.duelrecord.app.match.persistence.model.DeckIdentity;
import com.duelrecord.app.match.web.dto.CreateDeckIdentityRequest;
import com.duelrecord.app.match.web.dto.DeckIdentityResponse;
import com.duelrecord.app.shared.exceptions.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/decks/identities")
@RequiredArgsConstructor
public class DeckIdentityController {

    private final GetOrCreateDeckIdentityUseCase getOrCreateDeckIdentityUseCase;
    private final FindDeckIdentityByIdUseCase findDeckIdentityByIdUseCase;

    @ResponseStatus(code = HttpStatus.CREATED)
    @PostMapping
    public DeckIdentityResponse getOrCreateDeckIdentity(@Valid @RequestBody CreateDeckIdentityRequest request) {
        var items = request.items().stream().map(item -> new CardRoleInput(item.cardId(), item.role())).toList();
        DeckIdentity deckIdentity = getOrCreateDeckIdentityUseCase.execute(new GetOrCreateDeckIdentityInput(items, request.customName()));
        return DeckIdentityResponse.fromEntity(deckIdentity);
    }

    @GetMapping("/{id}")
    public DeckIdentityResponse getDeckIdentityById(@PathVariable UUID id) {
        var deckIdentity = findDeckIdentityByIdUseCase.execute(id)
                                                      .orElseThrow(() -> new EntityNotFoundException("problem.deckIdentity.notFound", id));

        return DeckIdentityResponse.fromEntity(deckIdentity);
    }
}
