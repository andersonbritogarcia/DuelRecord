package com.duelrecord.app.card;

import com.duelrecord.app.card.core.usecase.FindCardByIdUseCase;
import com.duelrecord.app.card.core.usecase.FindCardsByIdsUseCase;
import com.duelrecord.app.card.core.usecase.SearchCommandersInput;
import com.duelrecord.app.card.core.usecase.SearchCommandersUseCase;
import com.duelrecord.app.card.persistence.model.Card;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CardApi {

    private final SearchCommandersUseCase searchCommandersUseCase;
    private final FindCardByIdUseCase findCardByIdUseCase;
    private final FindCardsByIdsUseCase findCardsByIdsUseCase;

    public List<Card> searchCommanders(String query, String colorIdentity, Integer limit) {
        return searchCommandersUseCase.execute(new SearchCommandersInput(query, colorIdentity, limit));
    }

    public Optional<Card> findCardById(UUID id) {
        return findCardByIdUseCase.execute(id);
    }

    public List<Card> findAllById(Collection<UUID> ids) {
        return findCardsByIdsUseCase.execute(ids);
    }
}
