package com.duelrecord.app.player.core.usecase;

import com.duelrecord.app.player.persistence.model.Player;
import com.duelrecord.app.player.persistence.repository.PlayerRepository;
import com.duelrecord.app.shared.exceptions.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnsurePlayerForUserUseCaseTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private EnsurePlayerForUserUseCase useCase;

    @Test
    void shouldThrowWhenInputIsNull() {
        assertThrows(BusinessException.class, () -> useCase.execute(null));
    }

    @Test
    void shouldThrowWhenUserIdIsNull() {
        var input = new EnsurePlayerForUserInput(null, "email@test.com", "Name");
        assertThrows(BusinessException.class, () -> useCase.execute(input));
    }

    @Test
    void shouldReturnExistingPlayerWhenFound() {
        var userId = UUID.randomUUID();
        var existing = Player.createRegistered(userId, "John", "John");
        var input = new EnsurePlayerForUserInput(userId, "john@test.com", "John Doe");

        when(playerRepository.findByUserId(userId)).thenReturn(Optional.of(existing));

        var result = useCase.execute(input);

        assertEquals(existing.getId(), result.getId());
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void shouldCreateNewPlayerWithNameWhenProvided() {
        var userId = UUID.randomUUID();
        var input = new EnsurePlayerForUserInput(userId, "alice@test.com", "Alice Smith");

        when(playerRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(input);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("Alice Smith", result.getName());
        assertEquals("Alice Smith", result.getDisplayName());
        assertFalse(result.isGhost());

        ArgumentCaptor<Player> captor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository).save(captor.capture());
        assertEquals("Alice Smith", captor.getValue().getName());
    }

    @Test
    void shouldDeriveNameFromEmailWhenNameIsBlank() {
        var userId = UUID.randomUUID();
        var input = new EnsurePlayerForUserInput(userId, "bob.builder@test.com", "");

        when(playerRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(input);

        assertNotNull(result);
        assertEquals("bob.builder", result.getName());
        assertEquals("bob.builder", result.getDisplayName());
    }
}
