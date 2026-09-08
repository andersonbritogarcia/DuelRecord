package com.duelrecord.app.card.core.usecase;

import com.duelrecord.app.card.infrastructure.scryfall.ScryfallClient;
import com.duelrecord.app.card.infrastructure.scryfall.dto.ScryfallCardDto;
import com.duelrecord.app.card.persistence.model.Card;
import com.duelrecord.app.card.persistence.repository.CardRepository;
import com.duelrecord.app.shared.usecase.UseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SearchCommandersUseCase implements UseCase<SearchCommandersInput, List<Card>> {

    private static final Logger log = LoggerFactory.getLogger(SearchCommandersUseCase.class);

    private final CardRepository cardRepository;
    private final ScryfallClient scryfallClient;
    private final TransactionTemplate transactionTemplate;
    private final Object persistLock = new Object();

    @Autowired
    public SearchCommandersUseCase(CardRepository cardRepository,
                                  ScryfallClient scryfallClient,
                                  PlatformTransactionManager transactionManager) {
        this(cardRepository, scryfallClient, new TransactionTemplate(transactionManager));
    }

    SearchCommandersUseCase(CardRepository cardRepository,
                           ScryfallClient scryfallClient,
                           TransactionTemplate transactionTemplate) {
        this.cardRepository = cardRepository;
        this.scryfallClient = scryfallClient;
        this.transactionTemplate = transactionTemplate;
    }

    SearchCommandersUseCase(CardRepository cardRepository, ScryfallClient scryfallClient) {
        this(cardRepository, scryfallClient, (TransactionTemplate) null);
    }

    @Override
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
        if (Objects.isNull(scryfallCards) || scryfallCards.isEmpty()) {
            return;
        }

        List<ScryfallCardDto> validDtos = scryfallCards.stream()
                .filter(ScryfallCardDto::isValid)
                .toList();

        if (validDtos.isEmpty()) {
            return;
        }

        // Deduplicate by Scryfall ID to avoid inserting duplicates in the same batch
        Map<String, ScryfallCardDto> uniqueDtos = validDtos.stream()
                .collect(Collectors.toMap(
                        ScryfallCardDto::getId,
                        Function.identity(),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        // Synchronize on persistLock to prevent concurrent threads from racing on inserts
        synchronized (persistLock) {
            try {
                runInTransaction(status -> persistUniqueCards(uniqueDtos.values()));
            } catch (DataIntegrityViolationException e) {
                log.warn("Concurrent insert or unique constraint violation while persisting cards from Scryfall for query '{}': {}",
                        input.query(), e.getMessage());
            }
        }
    }

    private void runInTransaction(Consumer<TransactionStatus> action) {
        if (Objects.nonNull(transactionTemplate)) {
            transactionTemplate.executeWithoutResult(action);
        } else {
            action.accept(null);
        }
    }

    private void persistUniqueCards(Collection<ScryfallCardDto> dtos) {
        Set<String> scryfallIds = dtos.stream()
                .map(ScryfallCardDto::getId)
                .collect(Collectors.toSet());

        Map<String, Card> existingCardsByScryfallId = cardRepository.findByScryfallIdIn(scryfallIds)
                .stream()
                .collect(Collectors.toMap(Card::getScryfallId, Function.identity()));

        List<Card> cardsToPersist = dtos.stream().map(dto -> {
            Card existing = existingCardsByScryfallId.get(dto.getId());
            return Objects.nonNull(existing) ? dto.updateDomain(existing) : dto.toDomain();
        }).toList();

        cardRepository.saveAll(cardsToPersist);
    }
}
