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
class FindPlayerByIdUseCaseTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private FindPlayerByIdUseCase useCase;

    @Test
    void shouldReturnEmptyWhenIdIsNull() {
        assertTrue(useCase.execute(null).isEmpty());
    }

    @Test
    void shouldReturnPlayerWhenFound() {
        var id = UUID.randomUUID();
        var player = Player.createRegistered(UUID.randomUUID(), "Alice", "Alice");

        when(playerRepository.findById(id)).thenReturn(Optional.of(player));

        var result = useCase.execute(id);

        assertTrue(result.isPresent());
        assertEquals("Alice", result.get().getName());
    }
}
