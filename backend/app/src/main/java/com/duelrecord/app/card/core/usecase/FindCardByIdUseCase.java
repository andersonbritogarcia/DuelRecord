package com.duelrecord.app.card.core.usecase;

import com.duelrecord.app.card.persistence.model.Card;
import com.duelrecord.app.card.persistence.repository.CardRepository;
import com.duelrecord.app.shared.usecase.UseCase;
import com.duelrecord.app.shared.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class FindCardByIdUseCase implements UseCase<UUID, Optional<Card>> {

    private final CardRepository cardRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Card> execute(UUID id) {
        ValidationUtils.requireNonNull(id, "validation.cardId.notNull");
        return cardRepository.findById(id);
    }
}
