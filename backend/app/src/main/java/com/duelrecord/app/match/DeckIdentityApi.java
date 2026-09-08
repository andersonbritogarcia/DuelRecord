package com.duelrecord.app.match;

import com.duelrecord.app.match.core.usecase.FindDeckIdentityByIdUseCase;
import com.duelrecord.app.match.core.usecase.GetOrCreateDeckIdentityInput;
import com.duelrecord.app.match.core.usecase.GetOrCreateDeckIdentityUseCase;
import com.duelrecord.app.match.persistence.model.DeckIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeckIdentityApi {

    private final GetOrCreateDeckIdentityUseCase getOrCreateDeckIdentityUseCase;
    private final FindDeckIdentityByIdUseCase findDeckIdentityByIdUseCase;

    public DeckIdentity getOrCreateDeckIdentity(GetOrCreateDeckIdentityInput input) {
        return getOrCreateDeckIdentityUseCase.execute(input);
    }

    public Optional<DeckIdentity> findDeckIdentityById(UUID id) {
        return findDeckIdentityByIdUseCase.execute(id);
    }
}
