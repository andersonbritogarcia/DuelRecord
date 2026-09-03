package com.duelrecord.app.identity;

import com.duelrecord.app.identity.core.usecase.FindUserByAuthProviderIdUseCase;
import com.duelrecord.app.identity.core.usecase.FindUserByIdUseCase;
import com.duelrecord.app.identity.core.usecase.ResolveAuthenticatedUserUseCase;
import com.duelrecord.app.identity.persistence.model.GoogleUserPrincipal;
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

    public AuthenticatedUserDto resolveUser(String authProviderId, String email, String name) {
        var user = resolveAuthenticatedUserUseCase.execute(new GoogleUserPrincipal(authProviderId, email, name));
        return new AuthenticatedUserDto(user.getId(), user.getEmail(), user.getAuthProviderId());
    }

    public Optional<AuthenticatedUserDto> findUserById(UUID id) {
        return findUserByIdUseCase.execute(id)
                .map(user -> new AuthenticatedUserDto(user.getId(), user.getEmail(), user.getAuthProviderId()));
    }

    public Optional<AuthenticatedUserDto> findUserByAuthProviderId(String authProviderId) {
        return findUserByAuthProviderIdUseCase.execute(authProviderId)
                .map(user -> new AuthenticatedUserDto(user.getId(), user.getEmail(), user.getAuthProviderId()));
    }
}
