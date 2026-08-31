package com.duelrecord.app.identity.core.usecase;

import com.duelrecord.app.identity.persistence.model.User;
import com.duelrecord.app.shared.usecase.UseCase;
import com.duelrecord.app.shared.exceptions.EntityNotFoundException;
import com.duelrecord.app.shared.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class GetCurrentUserUseCase implements UseCase<String, User> {

    private final FindUserByAuthProviderIdUseCase findUserByAuthProviderIdUseCase;

    @Override
    @Transactional(readOnly = true)
    public User execute(String authProviderId) {
        ValidationUtils.requireNonBlank(authProviderId, "validation.authProviderId.notBlank");
        return findUserByAuthProviderIdUseCase.execute(authProviderId)
                                              .orElseThrow(() -> new EntityNotFoundException("problem.user.notFound", authProviderId));
    }
}
