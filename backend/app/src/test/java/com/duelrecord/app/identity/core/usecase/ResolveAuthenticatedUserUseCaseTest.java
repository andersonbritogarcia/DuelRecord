package com.duelrecord.app.identity.core.usecase;

import com.duelrecord.app.identity.UserAuthenticatedEvent;
import com.duelrecord.app.identity.persistence.model.GoogleUserPrincipal;
import com.duelrecord.app.identity.persistence.model.User;
import com.duelrecord.app.identity.persistence.model.UserStatus;
import com.duelrecord.app.identity.persistence.repository.UserRepository;
import com.duelrecord.app.shared.exceptions.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResolveAuthenticatedUserUseCaseTest {

    @Test
    void shouldRejectSuspendedUserBeforeUpdatingEmailOrPublishingEvent() {
        var user = User.builder().id(UUID.randomUUID()).authProviderId("blocked")
                .email("old@example.com").status(UserStatus.SUSPENDED).build();
        when(userRepository.findByAuthProviderId("blocked")).thenReturn(Optional.of(user));
        assertThrows(UnauthorizedException.class, () -> useCase.execute(new GoogleUserPrincipal("blocked", "new@example.com")));
        verify(userRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @InjectMocks
    private ResolveAuthenticatedUserUseCase useCase;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    void shouldThrowExceptionWhenPrincipalIsNull() {
        var ex = assertThrows(UnauthorizedException.class, () -> useCase.execute(null));
        assertEquals("problem.invalidJwtClaims.detail", ex.messageKey());
    }

    @Test
    void shouldThrowExceptionWhenAuthProviderIdIsBlank() {
        var principal = new GoogleUserPrincipal("", "player@duelrecord.com");
        var ex = assertThrows(UnauthorizedException.class, () -> useCase.execute(principal));
        assertEquals("problem.invalidJwtClaims.detail", ex.messageKey());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsBlank() {
        var principal = new GoogleUserPrincipal("google-sub-123", "");
        var ex = assertThrows(UnauthorizedException.class, () -> useCase.execute(principal));
        assertEquals("problem.invalidJwtClaims.detail", ex.messageKey());
    }

    @Test
    void shouldCreateNewUserWhenNotFound() {
        var sub = "google-sub-123";
        var email = "player@duelrecord.com";
        var principal = new GoogleUserPrincipal(sub, email, "Player One");

        when(userRepository.findByAuthProviderId(sub)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(principal);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(sub, result.getAuthProviderId());
        assertEquals(UserStatus.ACTIVE, result.getStatus());
        assertNotNull(result.getId());
        assertNotNull(result.getCreatedAt());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals(email, captor.getValue().getEmail());

        ArgumentCaptor<UserAuthenticatedEvent> eventCaptor = ArgumentCaptor.forClass(UserAuthenticatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(result.getId(), eventCaptor.getValue().userId());
        assertEquals(email, eventCaptor.getValue().email());
        assertEquals("Player One", eventCaptor.getValue().name());
    }

    @Test
    void shouldReturnExistingUserWhenFoundAndEmailUnchanged() {
        var sub = "google-sub-456";
        var email = "existing@duelrecord.com";
        var principal = new GoogleUserPrincipal(sub, email);

        var existingUser = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .authProviderId(sub)
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now().minusSeconds(3600))
                .updatedAt(Instant.now().minusSeconds(3600))
                .build();

        when(userRepository.findByAuthProviderId(sub)).thenReturn(Optional.of(existingUser));

        var result = useCase.execute(principal);

        assertEquals(existingUser.getId(), result.getId());
        assertEquals(email, result.getEmail());
        verify(userRepository, never()).save(any(User.class));
        verify(eventPublisher).publishEvent(any(UserAuthenticatedEvent.class));
    }

    @Test
    void shouldUpdateEmailWhenFoundAndEmailChanged() {
        var sub = "google-sub-789";
        var oldEmail = "old@duelrecord.com";
        var newEmail = "new@duelrecord.com";
        var principal = new GoogleUserPrincipal(sub, newEmail);

        var existingUser = User.builder()
                .id(UUID.randomUUID())
                .email(oldEmail)
                .authProviderId(sub)
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now().minusSeconds(3600))
                .updatedAt(Instant.now().minusSeconds(3600))
                .build();

        when(userRepository.findByAuthProviderId(sub)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(principal);

        assertEquals(newEmail, result.getEmail());
        verify(userRepository).save(existingUser);
        verify(eventPublisher).publishEvent(any(UserAuthenticatedEvent.class));
    }
}
