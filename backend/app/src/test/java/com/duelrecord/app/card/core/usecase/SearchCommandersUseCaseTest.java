package com.duelrecord.app.card.core.usecase;

import com.duelrecord.app.card.infrastructure.scryfall.ScryfallClient;
import com.duelrecord.app.card.infrastructure.scryfall.dto.ScryfallCardDto;
import com.duelrecord.app.card.infrastructure.scryfall.dto.ScryfallImageUrisDto;
import com.duelrecord.app.card.persistence.model.Card;
import com.duelrecord.app.card.persistence.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchCommandersUseCaseTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private ScryfallClient scryfallClient;

    private SearchCommandersUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SearchCommandersUseCase(cardRepository, scryfallClient);
    }

    @Test
    void shouldReturnFromLocalDatabaseWhenCacheHits() {
        Card card = Card.create("scryfall-1", "oracle-1", "Tasigur, the Golden Fang",
                "Legendary Creature — Human Shaman", "{5}{B}", BigDecimal.valueOf(6),
                "UBG", null, null, null, true, false, false, false);

        when(cardRepository.searchJpa("Tasigur", "UBG", true, PageRequest.of(0, 20)))
                .thenReturn(List.of(card));

        List<Card> results = useCase.execute(new SearchCommandersInput("Tasigur", "gbu", 20));

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals("Tasigur, the Golden Fang", results.get(0).getName());
        verify(scryfallClient, never()).searchCards(anyString());
    }

    @Test
    void shouldFetchFromScryfallWhenLocalCacheMisses() {
        String query = "Yoshimaru";
        when(cardRepository.searchJpa(eq(query), eq(null), eq(true), eq(PageRequest.of(0, 20))))
                .thenReturn(Collections.emptyList())
                .thenReturn(List.of(Card.create("scryfall-yoshi", "oracle-yoshi", "Yoshimaru, Ever Faithful",
                        "Legendary Creature — Dog", "{W}", BigDecimal.valueOf(1),
                        "W", "http://small", "http://normal", "http://art", true, true, false, false)));

        ScryfallCardDto dto = new ScryfallCardDto();
        dto.setId("scryfall-yoshi");
        dto.setOracleId("oracle-yoshi");
        dto.setName("Yoshimaru, Ever Faithful");
        dto.setTypeLine("Legendary Creature — Dog");
        dto.setManaCost("{W}");
        dto.setCmc(BigDecimal.ONE);
        dto.setColorIdentity(List.of("W"));
        dto.setKeywords(List.of("Partner"));
        ScryfallImageUrisDto images = new ScryfallImageUrisDto();
        images.setSmall("http://small");
        images.setNormal("http://normal");
        images.setArtCrop("http://art");
        dto.setImageUris(images);

        when(scryfallClient.searchCards(eq("Yoshimaru"))).thenReturn(List.of(dto));
        when(cardRepository.findByScryfallIdIn(Set.of("scryfall-yoshi"))).thenReturn(Collections.emptyList());
        when(cardRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        List<Card> results = useCase.execute(new SearchCommandersInput(query, null, 20));

        assertEquals(1, results.size());
        assertEquals("Yoshimaru, Ever Faithful", results.get(0).getName());
        verify(scryfallClient).searchCards("Yoshimaru");
        verify(cardRepository).findByScryfallIdIn(Set.of("scryfall-yoshi"));
        verify(cardRepository).saveAll(anyList());
    }

    @Test
    void shouldDeduplicateCardsByScryfallIdBeforePersisting() {
        String query = "Brigid";
        when(cardRepository.searchJpa(eq(query), eq(null), eq(true), eq(PageRequest.of(0, 20))))
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.emptyList());

        ScryfallCardDto dto1 = new ScryfallCardDto();
        dto1.setId("cb7d5bbb-4f68-4e38-8bb0-a95af21b24c8");
        dto1.setName("Brigid, Clachan's Heart // Brigid, Doun's Mind");
        dto1.setTypeLine("Legendary Creature — Kithkin Warrior // Legendary Creature — Kithkin Soldier");

        ScryfallCardDto dto2 = new ScryfallCardDto();
        dto2.setId("cb7d5bbb-4f68-4e38-8bb0-a95af21b24c8");
        dto2.setName("Brigid, Clachan's Heart // Brigid, Doun's Mind");
        dto2.setTypeLine("Legendary Creature — Kithkin Warrior // Legendary Creature — Kithkin Soldier");

        when(scryfallClient.searchCards(eq("Brigid"))).thenReturn(List.of(dto1, dto2));
        when(cardRepository.findByScryfallIdIn(Set.of("cb7d5bbb-4f68-4e38-8bb0-a95af21b24c8"))).thenReturn(Collections.emptyList());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Card>> captor = ArgumentCaptor.forClass(List.class);

        useCase.execute(new SearchCommandersInput(query, null, 20));

        verify(cardRepository).saveAll(captor.capture());
        assertEquals(1, captor.getValue().size(), "Should have deduplicated the DTOs with the same Scryfall ID");
        assertEquals("cb7d5bbb-4f68-4e38-8bb0-a95af21b24c8", captor.getValue().get(0).getScryfallId());
    }

    @Test
    void shouldHandleDataIntegrityViolationGracefullyWhenConcurrentInsertOccurs() {
        String query = "Brigid";
        Card persistedByOtherThread = Card.create("cb7d5bbb-4f68-4e38-8bb0-a95af21b24c8", "oracle-brigid",
                "Brigid, Clachan's Heart // Brigid, Doun's Mind",
                "Legendary Creature — Kithkin Warrior // Legendary Creature — Kithkin Soldier",
                "{2}{W}", BigDecimal.valueOf(3), "GW", null, null, null, true, false, false, false);

        when(cardRepository.searchJpa(eq(query), eq(null), eq(true), eq(PageRequest.of(0, 20))))
                .thenReturn(Collections.emptyList())
                .thenReturn(List.of(persistedByOtherThread));

        ScryfallCardDto dto = new ScryfallCardDto();
        dto.setId("cb7d5bbb-4f68-4e38-8bb0-a95af21b24c8");
        dto.setName("Brigid, Clachan's Heart // Brigid, Doun's Mind");
        dto.setTypeLine("Legendary Creature — Kithkin Warrior // Legendary Creature — Kithkin Soldier");

        when(scryfallClient.searchCards(eq("Brigid"))).thenReturn(List.of(dto));
        when(cardRepository.findByScryfallIdIn(Set.of("cb7d5bbb-4f68-4e38-8bb0-a95af21b24c8"))).thenReturn(Collections.emptyList());
        when(cardRepository.saveAll(anyList())).thenThrow(new DataIntegrityViolationException("uk_cards_scryfall_id"));

        List<Card> results = useCase.execute(new SearchCommandersInput(query, null, 20));

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals("Brigid, Clachan's Heart // Brigid, Doun's Mind", results.get(0).getName());
    }
}
