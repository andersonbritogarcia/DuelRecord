package com.duelrecord.app.identity.core.usecase;

import com.duelrecord.app.identity.UserAuthenticatedEvent;
import com.duelrecord.app.identity.persistence.model.GoogleUserPrincipal;
import com.duelrecord.app.identity.persistence.model.User;
import com.duelrecord.app.identity.persistence.repository.UserRepository;
import com.duelrecord.app.shared.exceptions.UnauthorizedException;
import com.duelrecord.app.shared.usecase.UseCase;
import com.duelrecord.app.shared.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@RequiredArgsConstructor
@Service
public class ResolveAuthenticatedUserUseCase implements UseCase<GoogleUserPrincipal, User> {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public User execute(GoogleUserPrincipal principal) {
        if (Objects.isNull(principal)) {
            throw new UnauthorizedException("problem.invalidJwtClaims.detail");
        }

        if (ValidationUtils.isBlank(principal.authProviderId()) || ValidationUtils.isBlank(principal.email())) {
            throw new UnauthorizedException("problem.invalidJwtClaims.detail");
        }

        User user = userRepository.findByAuthProviderId(principal.authProviderId()).map(existingUser -> {
            if (!existingUser.hasSameEmail(principal.email())) {
                existingUser.updateEmail(principal.email());
                return userRepository.save(existingUser);
            }
            return existingUser;
        }).orElseGet(() -> {
            var newUser = new User(principal.email(), principal.authProviderId());
            return userRepository.save(newUser);
        });

        eventPublisher.publishEvent(new UserAuthenticatedEvent(user.getId(), user.getEmail(), principal.name()));

        return user;
    }
}
