package com.duelrecord.app.match.core.usecase;

import com.duelrecord.app.match.persistence.model.DeckIdentity;
import com.duelrecord.app.match.persistence.repository.DeckIdentityRepository;
import com.duelrecord.app.shared.usecase.UseCase;
import com.duelrecord.app.shared.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindDeckIdentityByIdUseCase implements UseCase<UUID, Optional<DeckIdentity>> {

    private final DeckIdentityRepository deckIdentityRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<DeckIdentity> execute(UUID id) {
        ValidationUtils.requireNonNull(id, "validation.deckIdentityId.notNull");
        return deckIdentityRepository.findWithCardsById(id);
    }
}
