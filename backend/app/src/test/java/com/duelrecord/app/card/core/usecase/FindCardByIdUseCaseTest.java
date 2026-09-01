package com.duelrecord.app.card.core.usecase;

import com.duelrecord.app.card.persistence.model.Card;
import com.duelrecord.app.card.persistence.repository.CardRepository;
import com.duelrecord.app.shared.exceptions.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindCardByIdUseCaseTest {

    @Mock
    private CardRepository cardRepository;

    private FindCardByIdUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindCardByIdUseCase(cardRepository);
    }

    @Test
    void shouldFindCardById() {
        UUID id = UUID.randomUUID();
        Card card = Card.create("scryfall-1", "oracle-1", "Tasigur, the Golden Fang",
                "Legendary Creature — Human Shaman", "{5}{B}", BigDecimal.valueOf(6),
                "UBG", null, null, null, true, false, false, false);

        when(cardRepository.findById(id)).thenReturn(Optional.of(card));

        Optional<Card> result = useCase.execute(id);

        assertTrue(result.isPresent());
        assertEquals("Tasigur, the Golden Fang", result.get().getName());
        verify(cardRepository).findById(id);
    }

    @Test
    void shouldThrowValidationExceptionWhenIdIsNull() {
        BusinessException exception = assertThrows(BusinessException.class, () -> useCase.execute(null));
        assertEquals("validation.cardId.notNull", exception.messageKey());
    }
}
