package com.duelrecord.app.card.core.usecase;

import com.duelrecord.app.card.infrastructure.scryfall.ScryfallClient;
import com.duelrecord.app.card.infrastructure.scryfall.dto.ScryfallCardDto;
import com.duelrecord.app.card.persistence.model.Card;
import com.duelrecord.app.card.persistence.repository.CardRepository;
import com.duelrecord.app.shared.usecase.UseCase;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class SearchCommandersUseCase implements UseCase<SearchCommandersInput, List<Card>> {

    private static final Logger log = LoggerFactory.getLogger(SearchCommandersUseCase.class);

    private final CardRepository cardRepository;
    private final ScryfallClient scryfallClient;

    @Override
    @Transactional
    public List<Card> execute(SearchCommandersInput input) {
        if (Objects.isNull(input)) {
            return List.of();
        }

        List<Card> localResults = searchLocal(input);
        if (!localResults.isEmpty() || input.normalizedQuery().isBlank()) {
            return localResults;
        }

        log.info("Local card cache miss for query '{}'. Fetching from Scryfall...", input.query());
        fetchAndPersistFromScryfall(input);

        return searchLocal(input);
    }

    private List<Card> searchLocal(SearchCommandersInput input) {
        String query = input.normalizedQuery();
        return cardRepository.searchJpa(query.isBlank() ? null : query, input.canonicalColor(), true, input.toPageRequest());
    }

    private void fetchAndPersistFromScryfall(SearchCommandersInput input) {
        List<ScryfallCardDto> scryfallCards = scryfallClient.searchCards(input.toScryfallQuery());

        List<ScryfallCardDto> validDtos = scryfallCards.stream().filter(ScryfallCardDto::isValid).toList();

        if (validDtos.isEmpty()) {
            return;
        }

        Set<String> scryfallIds = validDtos.stream()
                                           .map(ScryfallCardDto::getId)
                                           .collect(Collectors.toSet());

        Map<String, Card> existingCardsByScryfallId = cardRepository.findByScryfallIdIn(scryfallIds)
                                                                    .stream()
                                                                    .collect(Collectors.toMap(Card::getScryfallId, Function.identity()));

        List<Card> cardsToPersist = validDtos.stream().map(dto -> {
            Card existing = existingCardsByScryfallId.get(dto.getId());
            return Objects.nonNull(existing) ? dto.updateDomain(existing) : dto.toDomain();
        }).toList();

        cardRepository.saveAll(cardsToPersist);
    }
}
