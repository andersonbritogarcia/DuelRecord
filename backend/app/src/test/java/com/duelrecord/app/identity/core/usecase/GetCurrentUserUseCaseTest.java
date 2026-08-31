package com.duelrecord.app.identity.core.usecase;

import com.duelrecord.app.identity.persistence.model.User;
import com.duelrecord.app.identity.persistence.model.UserStatus;
import com.duelrecord.app.shared.exceptions.BusinessException;
import com.duelrecord.app.shared.exceptions.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCurrentUserUseCaseTest {

    @Mock
    private FindUserByAuthProviderIdUseCase findUserByAuthProviderIdUseCase;

    private GetCurrentUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetCurrentUserUseCase(findUserByAuthProviderIdUseCase);
    }

    @Test
    void shouldReturnUserWhenExists() {
        String sub = "google-sub-123";
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("user@duelrecord.com")
                .authProviderId(sub)
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(findUserByAuthProviderIdUseCase.execute(sub)).thenReturn(Optional.of(user));

        User result = useCase.execute(sub);

        assertEquals(user.getId(), result.getId());
        assertEquals("user@duelrecord.com", result.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenNotFound() {
        String sub = "unknown-sub";
        when(findUserByAuthProviderIdUseCase.execute(sub)).thenReturn(Optional.empty());

        var ex = assertThrows(EntityNotFoundException.class, () -> useCase.execute(sub));
        assertEquals("problem.user.notFound", ex.messageKey());
        assertEquals(sub, ex.args()[0]);
    }

    @Test
    void shouldThrowBusinessExceptionWhenAuthProviderIdIsBlank() {
        var ex = assertThrows(BusinessException.class, () -> useCase.execute(" "));
        assertEquals("validation.authProviderId.notBlank", ex.messageKey());
    }
}

