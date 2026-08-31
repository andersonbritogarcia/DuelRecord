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
class FindUserByAuthProviderIdUseCaseTest {

    @InjectMocks
    private FindUserByAuthProviderIdUseCase useCase;

    @Mock
    private UserRepository userRepository;

    @Test
    void shouldReturnUserWhenFound() {
        String sub = "google-sub-123";
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("user@duelrecord.com")
                .authProviderId(sub)
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userRepository.findByAuthProviderId(sub)).thenReturn(Optional.of(user));

        Optional<User> result = useCase.execute(sub);

        assertTrue(result.isPresent());
        assertEquals(sub, result.get().getAuthProviderId());
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        String sub = "unknown-sub";
        when(userRepository.findByAuthProviderId(sub)).thenReturn(Optional.empty());

        Optional<User> result = useCase.execute(sub);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowBusinessExceptionWhenAuthProviderIdIsBlank() {
        var ex = assertThrows(BusinessException.class, () -> useCase.execute(" "));
        assertEquals("validation.authProviderId.notBlank", ex.messageKey());
    }
}
