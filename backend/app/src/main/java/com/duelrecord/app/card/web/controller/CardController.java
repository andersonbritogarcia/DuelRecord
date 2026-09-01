package com.duelrecord.app.card.web.controller;

import com.duelrecord.app.card.core.usecase.FindCardByIdUseCase;
import com.duelrecord.app.card.core.usecase.SearchCommandersInput;
import com.duelrecord.app.card.core.usecase.SearchCommandersUseCase;
import com.duelrecord.app.card.persistence.model.Card;
import com.duelrecord.app.card.web.dto.CardResponse;
import com.duelrecord.app.shared.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final SearchCommandersUseCase searchCommandersUseCase;
    private final FindCardByIdUseCase findCardByIdUseCase;

    @GetMapping("/commanders")
    public List<CardResponse> searchCommanders(@RequestParam(name = "search", required = false) String search,
                                               @RequestParam(name = "color", required = false) String color,
                                               @RequestParam(name = "limit", required = false, defaultValue = "20") Integer limit) {
        List<Card> cards = searchCommandersUseCase.execute(new SearchCommandersInput(search, color, limit));
        return cards.stream().map(CardResponse::fromDomain).toList();
    }

    @GetMapping("/{id}")
    public CardResponse getCardById(@PathVariable("id") UUID id) {
        return findCardByIdUseCase.execute(id)
                                  .map(CardResponse::fromDomain)
                                  .orElseThrow(() -> new EntityNotFoundException("problem.card.notFound", id));
    }
}
