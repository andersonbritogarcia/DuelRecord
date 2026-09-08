package com.duelrecord.app.card.core.usecase;

import com.duelrecord.app.card.persistence.model.Card;
import com.duelrecord.app.card.persistence.repository.CardRepository;
import com.duelrecord.app.shared.usecase.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class FindCardsByIdsUseCase implements UseCase<Collection<UUID>, List<Card>> {

    private final CardRepository cardRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Card> execute(Collection<UUID> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return List.of();
        }
        return cardRepository.findAllById(ids);
    }
}
