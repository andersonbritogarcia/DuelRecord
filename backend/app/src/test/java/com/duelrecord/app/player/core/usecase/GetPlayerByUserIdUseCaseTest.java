package com.duelrecord.app.player.core.usecase;

import com.duelrecord.app.player.persistence.model.Player;
import com.duelrecord.app.player.persistence.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPlayerByUserIdUseCaseTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private GetPlayerByUserIdUseCase useCase;

    @Test
    void shouldReturnEmptyWhenUserIdIsNull() {
        assertTrue(useCase.execute(null).isEmpty());
    }

    @Test
    void shouldReturnPlayerWhenFound() {
        var userId = UUID.randomUUID();
        var player = Player.createRegistered(userId, "Bob", "Bob");

        when(playerRepository.findByUserId(userId)).thenReturn(Optional.of(player));

        var result = useCase.execute(userId);

        assertTrue(result.isPresent());
        assertEquals("Bob", result.get().getName());
    }
}
