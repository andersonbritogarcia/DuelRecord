package com.duelrecord.app.identity;

import com.duelrecord.app.identity.persistence.model.GoogleUserPrincipal;
import com.duelrecord.app.identity.persistence.model.User;
import com.duelrecord.app.identity.core.usecase.FindUserByAuthProviderIdUseCase;
import com.duelrecord.app.identity.core.usecase.FindUserByIdUseCase;
import com.duelrecord.app.identity.core.usecase.ResolveAuthenticatedUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class IdentityApi {

    private final ResolveAuthenticatedUserUseCase resolveAuthenticatedUserUseCase;
    private final FindUserByIdUseCase findUserByIdUseCase;
    private final FindUserByAuthProviderIdUseCase findUserByAuthProviderIdUseCase;

    public User resolveUser(GoogleUserPrincipal principal) {
        return resolveAuthenticatedUserUseCase.execute(principal);
    }

    public Optional<User> findUserById(UUID id) {
        return findUserByIdUseCase.execute(id);
    }

    public Optional<User> findUserByAuthProviderId(String authProviderId) {
        return findUserByAuthProviderIdUseCase.execute(authProviderId);
    }
}
