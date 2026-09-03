package com.duelrecord.app.player.core.usecase;

import com.duelrecord.app.player.persistence.model.Player;
import com.duelrecord.app.player.persistence.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchPlayersUseCaseTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private SearchPlayersUseCase useCase;

    @Test
    void shouldSearchPlayersWithPagination() {
        var pageable = PageRequest.of(0, 10);
        var player = Player.createRegistered(UUID.randomUUID(), "Tasigur Player", "Tasigur");
        var page = new PageImpl<>(List.of(player), pageable, 1);

        when(playerRepository.search("Tasigur", pageable)).thenReturn(page);

        var result = useCase.execute(new SearchPlayersInput("Tasigur", pageable));

        assertEquals(1, result.getTotalElements());
        assertEquals("Tasigur Player", result.getContent().get(0).getName());
    }
}
