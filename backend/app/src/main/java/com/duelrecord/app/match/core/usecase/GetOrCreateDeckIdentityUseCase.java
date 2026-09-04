package com.duelrecord.app.match.core.usecase;

import com.duelrecord.app.card.CardApi;
import com.duelrecord.app.card.persistence.model.Card;
import com.duelrecord.app.match.core.model.DeckCardRole;
import com.duelrecord.app.match.core.usecase.GetOrCreateDeckIdentityInput.CardRoleInput;
import com.duelrecord.app.match.core.util.DeckColorIdentityUtils;
import com.duelrecord.app.match.core.util.DeckSignatureUtils;
import com.duelrecord.app.match.core.util.DeckSignatureUtils.SignatureItem;
import com.duelrecord.app.match.persistence.model.DeckIdentity;
import com.duelrecord.app.match.persistence.model.DeckIdentityCard;
import com.duelrecord.app.match.persistence.repository.DeckIdentityRepository;
import com.duelrecord.app.shared.exceptions.BusinessException;
import com.duelrecord.app.shared.usecase.UseCase;
import com.duelrecord.app.shared.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetOrCreateDeckIdentityUseCase implements UseCase<GetOrCreateDeckIdentityInput, DeckIdentity> {

    private final DeckIdentityRepository deckIdentityRepository;
    private final CardApi cardApi;

    @Override
    @Transactional
    public DeckIdentity execute(GetOrCreateDeckIdentityInput input) {
        ValidationUtils.requireNonNull(input, "problem.invalidDeckIdentity.detail");
        if (CollectionUtils.isEmpty(input.items())) {
            throw new BusinessException("problem.deckIdentity.emptyItems");
        }

        validateItems(input.items());

        // Check cards existence
        Set<UUID> cardIds = input.items().stream().map(CardRoleInput::cardId).collect(Collectors.toSet());
        Map<UUID, Card> cardsById = cardApi.findAllById(cardIds).stream().collect(Collectors.toMap(Card::getId, Function.identity()));

        for (UUID cardId : cardIds) {
            if (!cardsById.containsKey(cardId)) {
                throw new BusinessException("problem.deckIdentity.cardNotFound", cardId);
            }
        }

        // Validate Mechanics Rules
        validateMechanics(input.items(), cardsById);

        // Deduplication signature
        var signatureItems = input.items().stream().map(item -> new SignatureItem(item.cardId(), item.role())).toList();
        String signature = DeckSignatureUtils.generateSignature(signatureItems);

        Optional<DeckIdentity> existing = deckIdentityRepository.findBySignature(signature);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Calculate combined color identity
        var colorIdentities = input.items().stream().map(item -> cardsById.get(item.cardId()).getColorIdentity()).toList();
        String combinedColorIdentity = DeckColorIdentityUtils.combine(colorIdentities);

        // Determine name
        String name = resolveDeckName(input, cardsById);

        // Build entity
        DeckIdentity deckIdentity = DeckIdentity.create(name, combinedColorIdentity, signature);
        for (CardRoleInput item : input.items()) {
            deckIdentity.addCard(DeckIdentityCard.create(item.cardId(), item.role()));
        }

        return deckIdentityRepository.save(deckIdentity);
    }

    private void validateItems(List<CardRoleInput> items) {
        Set<UUID> seenCardIds = new HashSet<>();
        for (CardRoleInput item : items) {
            ValidationUtils.requireNonNull(item.cardId(), "validation.cardId.notNull");
            ValidationUtils.requireNonNull(item.role(), "validation.deckCardRole.notNull");

            if (!seenCardIds.add(item.cardId())) {
                throw new BusinessException("problem.deckIdentity.duplicateCard", item.cardId());
            }
        }

        long commanderCount = items.stream().filter(i -> i.role() == DeckCardRole.COMMANDER).count();
        if (commanderCount == 0) {
            throw new BusinessException("problem.deckIdentity.commanderRequired");
        }
        if (commanderCount > 1) {
            throw new BusinessException("problem.deckIdentity.tooManyCommanders");
        }

        long commandZoneCount = items.stream().filter(i -> i.role().isCommandZone()).count();
        if (commandZoneCount > 2) {
            throw new BusinessException("problem.deckIdentity.invalidCommandZone");
        }

        long partnerCount = items.stream().filter(i -> i.role() == DeckCardRole.PARTNER).count();
        long backgroundCount = items.stream().filter(i -> i.role() == DeckCardRole.BACKGROUND).count();
        if (partnerCount > 0 && backgroundCount > 0) {
            throw new BusinessException("problem.deckIdentity.invalidCommandZone");
        }

        long companionCount = items.stream().filter(i -> i.role() == DeckCardRole.COMPANION).count();
        if (companionCount > 1) {
            throw new BusinessException("problem.deckIdentity.tooManyCompanions");
        }
    }

    private void validateMechanics(List<CardRoleInput> items, Map<UUID, Card> cardsById) {
        CardRoleInput commanderInput = items
                .stream()
                .filter(i -> i.role() == DeckCardRole.COMMANDER)
                .findFirst()
                .orElseThrow(() -> new BusinessException("problem.deckIdentity.commanderRequired"));

        Card commanderCard = cardsById.get(commanderInput.cardId());

        if (!commanderCard.isCommanderLegal()) {
            throw new BusinessException("problem.deckIdentity.invalidCommanderLegal", commanderCard.getName());
        }

        Optional<CardRoleInput> partnerInput = items.stream().filter(i -> i.role() == DeckCardRole.PARTNER).findFirst();
        if (partnerInput.isPresent()) {
            Card partnerCard = cardsById.get(partnerInput.get().cardId());
            if (!commanderCard.isPartner() || !partnerCard.isPartner()) {
                throw new BusinessException("problem.deckIdentity.invalidPartner");
            }
        }

        Optional<CardRoleInput> backgroundInput = items.stream().filter(i -> i.role() == DeckCardRole.BACKGROUND).findFirst();
        if (backgroundInput.isPresent()) {
            Card backgroundCard = cardsById.get(backgroundInput.get().cardId());
            if (!backgroundCard.isBackground()) {
                throw new BusinessException("problem.deckIdentity.invalidBackground");
            }
        }

        Optional<CardRoleInput> companionInput = items.stream().filter(i -> i.role() == DeckCardRole.COMPANION).findFirst();
        if (companionInput.isPresent()) {
            Card companionCard = cardsById.get(companionInput.get().cardId());
            if (!companionCard.isCompanion()) {
                throw new BusinessException("problem.deckIdentity.invalidCompanion");
            }
        }
    }

    private String resolveDeckName(GetOrCreateDeckIdentityInput input, Map<UUID, Card> cardsById) {
        if (!ValidationUtils.isBlank(input.customName())) {
            return input.customName();
        }

        var commandZoneNames = new ArrayList<String>();
        items(input.items(), DeckCardRole.COMMANDER).forEach(item -> commandZoneNames.add(cardsById.get(item.cardId()).getName()));
        items(input.items(), DeckCardRole.PARTNER).forEach(item -> commandZoneNames.add(cardsById.get(item.cardId()).getName()));
        items(input.items(), DeckCardRole.BACKGROUND).forEach(item -> commandZoneNames.add(cardsById.get(item.cardId()).getName()));

        return String.join(" / ", commandZoneNames);
    }

    private List<CardRoleInput> items(List<CardRoleInput> items, DeckCardRole role) {
        return items.stream().filter(i -> i.role() == role).toList();
    }
}
