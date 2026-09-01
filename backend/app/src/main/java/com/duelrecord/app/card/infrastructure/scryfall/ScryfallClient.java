package com.duelrecord.app.card.infrastructure.scryfall;

import com.duelrecord.app.card.infrastructure.scryfall.dto.ScryfallCardDto;
import com.duelrecord.app.card.infrastructure.scryfall.dto.ScryfallSearchResponseDto;
import com.duelrecord.app.shared.exceptions.BusinessException;
import com.duelrecord.app.shared.utils.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

@Component
public class ScryfallClient {

    private static final Logger log = LoggerFactory.getLogger(ScryfallClient.class);
    private static final long MIN_REQUEST_INTERVAL_MS = 100;

    private final RestClient restClient;
    private final AtomicLong lastRequestTime = new AtomicLong(0);

    public ScryfallClient(@Value("${app.scryfall.base-url:https://api.scryfall.com}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", "DuelRecord/1.0 (competitive MTG record app)")
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .defaultStatusHandler(
                        status -> status.value() == 429,
                        (req, resp) -> { throw new BusinessException("problem.scryfall.rateLimit"); }
                )
                .defaultStatusHandler(
                        HttpStatusCode::is5xxServerError,
                        (req, resp) -> { throw new BusinessException("problem.scryfall.unavailable"); }
                )
                .build();
    }

    public List<ScryfallCardDto> searchCards(String query) {
        if (ValidationUtils.isBlank(query)) {
            return Collections.emptyList();
        }
        return execute(
                () -> restClient.get()
                        .uri(uri -> uri.path("/cards/search").queryParam("q", query).build())
                        .retrieve()
                        .body(ScryfallSearchResponseDto.class),
                "searchCards: " + query
        ).map(ScryfallSearchResponseDto::getData).orElse(Collections.emptyList());
    }

    public Optional<ScryfallCardDto> findCardByScryfallId(String scryfallId) {
        if (ValidationUtils.isBlank(scryfallId)) {
            return Optional.empty();
        }
        return execute(
                () -> restClient.get()
                        .uri("/cards/{id}", scryfallId)
                        .retrieve()
                        .body(ScryfallCardDto.class),
                "findCardById: " + scryfallId
        );
    }

    private <T> Optional<T> execute(Supplier<T> requestSupplier, String context) {
        enforceRateLimit();
        try {
            return Optional.ofNullable(requestSupplier.get());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                log.debug("Scryfall 404 (not found) for {}", context);
                return Optional.empty();
            }
            log.warn("Scryfall client error for {}: {}", context, e.getMessage());
            return Optional.empty();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error contacting Scryfall for {}: {}", context, e.getMessage(), e);
            throw new BusinessException("problem.scryfall.unavailable");
        }
    }

    private void enforceRateLimit() {
        long now = System.currentTimeMillis();
        long previous = lastRequestTime.get();
        long elapsed = now - previous;
        if (elapsed < MIN_REQUEST_INTERVAL_MS) {
            try {
                Thread.sleep(MIN_REQUEST_INTERVAL_MS - elapsed);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        lastRequestTime.set(System.currentTimeMillis());
    }
}
