package com.duelrecord.app.player.core.usecase;

import com.duelrecord.app.geo.GeoApi;
import com.duelrecord.app.geo.persistence.model.City;
import com.duelrecord.app.geo.persistence.model.Country;
import com.duelrecord.app.player.persistence.model.Player;
import com.duelrecord.app.player.persistence.repository.PlayerRepository;
import com.duelrecord.app.shared.exceptions.BusinessException;
import com.duelrecord.app.shared.exceptions.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateGhostPlayerUseCaseTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private GeoApi geoApi;

    @InjectMocks
    private CreateGhostPlayerUseCase useCase;

    @Test
    void shouldThrowWhenInputIsNull() {
        assertThrows(BusinessException.class, () -> useCase.execute(null));
    }

    @Test
    void shouldThrowWhenNameIsBlank() {
        var input = new CreateGhostPlayerInput("", "Display", null, null, null);
        assertThrows(BusinessException.class, () -> useCase.execute(input));
    }

    @Test
    void shouldThrowWhenCityIdIsInvalid() {
        var cityId = UUID.randomUUID();
        var input = new CreateGhostPlayerInput("Ghost", "Ghost", null, null, cityId);

        when(geoApi.findCityById(cityId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> useCase.execute(input));
    }

    @Test
    void shouldCreateGhostPlayerWithCity() {
        var cityId = UUID.randomUUID();
        var city = City.create(Country.create("BR", "Brazil"), "São Paulo", "SP");
        var input = new CreateGhostPlayerInput("Ghost Oponente", "Ghost", "mtgo_ghost", "arena_ghost", cityId);

        when(geoApi.findCityById(cityId)).thenReturn(Optional.of(city));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(input);

        assertNotNull(result);
        assertNull(result.getUserId());
        assertTrue(result.isGhost());
        assertEquals("Ghost Oponente", result.getName());
        assertEquals("Ghost", result.getDisplayName());
        assertEquals("mtgo_ghost", result.getMtgoUsername());
        assertEquals("arena_ghost", result.getArenaUsername());
        assertEquals(city, result.getCity());

        ArgumentCaptor<Player> captor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository).save(captor.capture());
        assertNull(captor.getValue().getUserId());
    }
}
