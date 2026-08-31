package com.duelrecord.app.identity.core.usecase;

import com.duelrecord.app.identity.persistence.model.User;
import com.duelrecord.app.identity.persistence.repository.UserRepository;
import com.duelrecord.app.shared.usecase.UseCase;
import com.duelrecord.app.shared.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class FindUserByIdUseCase implements UseCase<UUID, Optional<User>> {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<User> execute(UUID id) {
        ValidationUtils.requireNonNull(id, "validation.userId.notNull");
        return userRepository.findById(id);
    }
}
