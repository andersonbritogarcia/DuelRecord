package com.duelrecord.app.identity.core.usecase;

import com.duelrecord.app.identity.persistence.model.User;
import com.duelrecord.app.identity.persistence.model.UserStatus;
import com.duelrecord.app.identity.persistence.repository.UserRepository;
import com.duelrecord.app.shared.exceptions.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindUserByIdUseCaseTest {

    @InjectMocks
    private FindUserByIdUseCase useCase;

    @Mock
    private UserRepository userRepository;

    @Test
    void shouldReturnUserWhenFound() {
        UUID id = UUID.randomUUID();
        User user = User.builder()
                .id(id)
                .email("user@duelrecord.com")
                .authProviderId("google-sub-123")
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        Optional<User> result = useCase.execute(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        Optional<User> result = useCase.execute(id);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowBusinessExceptionWhenIdIsNull() {
        var ex = assertThrows(BusinessException.class, () -> useCase.execute(null));
        assertEquals("validation.userId.notNull", ex.messageKey());
    }
}
