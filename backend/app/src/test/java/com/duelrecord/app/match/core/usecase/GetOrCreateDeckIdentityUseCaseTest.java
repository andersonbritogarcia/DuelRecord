package com.duelrecord.app.match.core.usecase;

import com.duelrecord.app.card.CardApi;
import com.duelrecord.app.card.persistence.model.Card;
import com.duelrecord.app.match.core.model.DeckCardRole;
import com.duelrecord.app.match.core.usecase.GetOrCreateDeckIdentityInput.CardRoleInput;
import com.duelrecord.app.match.persistence.model.DeckIdentity;
import com.duelrecord.app.match.persistence.repository.DeckIdentityRepository;
import com.duelrecord.app.shared.exceptions.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetOrCreateDeckIdentityUseCaseTest {

    @Mock
    private DeckIdentityRepository deckIdentityRepository;

    @Mock
    private CardApi cardApi;

    @InjectMocks
    private GetOrCreateDeckIdentityUseCase useCase;

    private Card yoshimaru;
    private Card kraum;
    private Card tasigur;
    private Card lutri;
    private Card kozilek;

    @BeforeEach
    void setUp() {
        yoshimaru = Card.create("yoshi-id", "oracle-1", "Yoshimaru, Ever Faithful", "Legendary Creature", "{W}",
                BigDecimal.ONE, "W", null, null, null, true, true, false, false);
        kraum = Card.create("kraum-id", "oracle-2", "Kraum, Ludevic's Opus", "Legendary Creature", "{3}{U}{R}",
                BigDecimal.valueOf(5), "UR", null, null, null, true, true, false, false);
        tasigur = Card.create("tasigur-id", "oracle-3", "Tasigur, the Golden Fang", "Legendary Creature", "{5}{B}",
                BigDecimal.valueOf(6), "UBG", null, null, null, true, false, false, false);
        lutri = Card.create("lutri-id", "oracle-4", "Lutri, the Spellchaser", "Legendary Creature", "{1}{U/R}{U/R}",
                BigDecimal.valueOf(3), "UR", null, null, null, true, false, true, false);
        kozilek = Card.create("kozilek-id", "oracle-5", "Kozilek, the Great Distortion", "Legendary Creature", "{8}{C}{C}",
                BigDecimal.TEN, "", null, null, null, true, false, false, false);
    }

    @Test
    @DisplayName("Should create deck identity for single commander Tasigur with UBG color identity")
    void shouldCreateSingleCommanderDeckIdentity() {
        when(cardApi.findAllById(anyCollection())).thenReturn(List.of(tasigur));
        when(deckIdentityRepository.findBySignature(anyString())).thenReturn(Optional.empty());
        when(deckIdentityRepository.save(any(DeckIdentity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GetOrCreateDeckIdentityInput input = new GetOrCreateDeckIdentityInput(
                List.of(new CardRoleInput(tasigur.getId(), DeckCardRole.COMMANDER))
        );

        DeckIdentity result = useCase.execute(input);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Tasigur, the Golden Fang");
        assertThat(result.getColorIdentity()).isEqualTo("UBG");
        assertThat(result.getCards()).hasSize(1);
        assertThat(result.getCards().get(0).getCardId()).isEqualTo(tasigur.getId());
        assertThat(result.getCards().get(0).getRole()).isEqualTo(DeckCardRole.COMMANDER);
    }

    @Test
    @DisplayName("Should create deck identity for Yoshimaru + Kraum with WUR color identity")
    void shouldCreatePartnerDeckIdentity() {
        when(cardApi.findAllById(anyCollection())).thenReturn(List.of(yoshimaru, kraum));
        when(deckIdentityRepository.findBySignature(anyString())).thenReturn(Optional.empty());
        when(deckIdentityRepository.save(any(DeckIdentity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GetOrCreateDeckIdentityInput input = new GetOrCreateDeckIdentityInput(
                List.of(
                        new CardRoleInput(yoshimaru.getId(), DeckCardRole.COMMANDER),
                        new CardRoleInput(kraum.getId(), DeckCardRole.PARTNER)
                )
        );

        DeckIdentity result = useCase.execute(input);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Yoshimaru, Ever Faithful / Kraum, Ludevic's Opus");
        assertThat(result.getColorIdentity()).isEqualTo("WUR");
        assertThat(result.getCards()).hasSize(2);
    }

    @Test
    @DisplayName("Should create deck identity for Tasigur + Lutri with UBRG color identity")
    void shouldCreateCommanderWithCompanion() {
        when(cardApi.findAllById(anyCollection())).thenReturn(List.of(tasigur, lutri));
        when(deckIdentityRepository.findBySignature(anyString())).thenReturn(Optional.empty());
        when(deckIdentityRepository.save(any(DeckIdentity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GetOrCreateDeckIdentityInput input = new GetOrCreateDeckIdentityInput(
                List.of(
                        new CardRoleInput(tasigur.getId(), DeckCardRole.COMMANDER),
                        new CardRoleInput(lutri.getId(), DeckCardRole.COMPANION)
                )
        );

        DeckIdentity result = useCase.execute(input);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Tasigur, the Golden Fang");
        assertThat(result.getColorIdentity()).isEqualTo("UBRG");
        assertThat(result.getCards()).hasSize(2);
    }

    @Test
    @DisplayName("Should assign 'C' color identity for colorless commander Kozilek")
    void shouldAssignColorlessForKozilek() {
        when(cardApi.findAllById(anyCollection())).thenReturn(List.of(kozilek));
        when(deckIdentityRepository.findBySignature(anyString())).thenReturn(Optional.empty());
        when(deckIdentityRepository.save(any(DeckIdentity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GetOrCreateDeckIdentityInput input = new GetOrCreateDeckIdentityInput(
                List.of(new CardRoleInput(kozilek.getId(), DeckCardRole.COMMANDER))
        );

        DeckIdentity result = useCase.execute(input);

        assertThat(result).isNotNull();
        assertThat(result.getColorIdentity()).isEqualTo("C");
    }

    @Test
    @DisplayName("Should return existing DeckIdentity when signature matches (idempotency)")
    void shouldReturnExistingDeckIdentityWhenSignatureMatches() {
        when(cardApi.findAllById(anyCollection())).thenReturn(List.of(tasigur));
        DeckIdentity existing = DeckIdentity.create("Existing Tasigur", "UBG", "dummy-sig");
        when(deckIdentityRepository.findBySignature(anyString())).thenReturn(Optional.of(existing));

        GetOrCreateDeckIdentityInput input = new GetOrCreateDeckIdentityInput(
                List.of(new CardRoleInput(tasigur.getId(), DeckCardRole.COMMANDER))
        );

        DeckIdentity result = useCase.execute(input);

        assertThat(result).isSameAs(existing);
        verify(deckIdentityRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when card is not found")
    void shouldFailWhenCardNotFound() {
        when(cardApi.findAllById(anyCollection())).thenReturn(List.of());

        GetOrCreateDeckIdentityInput input = new GetOrCreateDeckIdentityInput(
                List.of(new CardRoleInput(UUID.randomUUID(), DeckCardRole.COMMANDER))
        );

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(BusinessException.class)
                .hasMessage("problem.deckIdentity.cardNotFound");
    }

    @Test
    @DisplayName("Should fail when there are duplicate cards in input")
    void shouldFailWhenDuplicateCard() {
        UUID cardId = UUID.randomUUID();
        GetOrCreateDeckIdentityInput input = new GetOrCreateDeckIdentityInput(
                List.of(
                        new CardRoleInput(cardId, DeckCardRole.COMMANDER),
                        new CardRoleInput(cardId, DeckCardRole.PARTNER)
                )
        );

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(BusinessException.class)
                .hasMessage("problem.deckIdentity.duplicateCard");
    }

    @Test
    @DisplayName("Should fail when commander card is not commander legal")
    void shouldFailWhenNotCommanderLegal() {
        Card illegalCard = Card.create("scry", "ora", "Non Legendary Spell", "Instant", "{U}",
                BigDecimal.ONE, "U", null, null, null, false, false, false, false);
        when(cardApi.findAllById(anyCollection())).thenReturn(List.of(illegalCard));

        GetOrCreateDeckIdentityInput input = new GetOrCreateDeckIdentityInput(
                List.of(new CardRoleInput(illegalCard.getId(), DeckCardRole.COMMANDER))
        );

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(BusinessException.class)
                .hasMessage("problem.deckIdentity.invalidCommanderLegal");
    }

    @Test
    @DisplayName("Should fail when attempting partner pairing without partner mechanic")
    void shouldFailWhenPartnerWithoutMechanic() {
        when(cardApi.findAllById(anyCollection())).thenReturn(List.of(tasigur, kraum));

        GetOrCreateDeckIdentityInput input = new GetOrCreateDeckIdentityInput(
                List.of(
                        new CardRoleInput(tasigur.getId(), DeckCardRole.COMMANDER),
                        new CardRoleInput(kraum.getId(), DeckCardRole.PARTNER)
                )
        );

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(BusinessException.class)
                .hasMessage("problem.deckIdentity.invalidPartner");
    }
}
