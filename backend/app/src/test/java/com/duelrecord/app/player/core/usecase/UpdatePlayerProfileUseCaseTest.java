package com.duelrecord.app.player.core.usecase;

import com.duelrecord.app.geo.GeoApi;
import com.duelrecord.app.geo.persistence.model.City;
import com.duelrecord.app.geo.persistence.model.Country;
import com.duelrecord.app.player.persistence.model.Player;
import com.duelrecord.app.player.persistence.repository.PlayerRepository;
import com.duelrecord.app.shared.exceptions.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdatePlayerProfileUseCaseTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private GeoApi geoApi;

    @InjectMocks
    private UpdatePlayerProfileUseCase useCase;

    @Test
    void shouldThrowWhenPlayerNotFound() {
        var userId = UUID.randomUUID();
        var input = new UpdatePlayerProfileInput(userId, "New Name", null, null, null, null, null);

        when(playerRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> useCase.execute(input));
    }

    @Test
    void shouldUpdateProfileWithExistingCityId() {
        var userId = UUID.randomUUID();
        var cityId = UUID.randomUUID();
        var city = City.create(Country.create("BR", "Brazil"), "Brasília");
        var player = Player.createRegistered(userId, "Original Name", "Original");
        var input = new UpdatePlayerProfileInput(userId, "Updated Nick", "mtgo_user", "arena_user", cityId, null, null);

        when(playerRepository.findByUserId(userId)).thenReturn(Optional.of(player));
        when(geoApi.findCityById(cityId)).thenReturn(Optional.of(city));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(input);

        assertEquals("Updated Nick", result.getDisplayName());
        assertEquals("mtgo_user", result.getMtgoUsername());
        assertEquals("arena_user", result.getArenaUsername());
        assertEquals(city, result.getCity());
        verify(playerRepository).save(player);
    }

    @Test
    void shouldUpdateProfileWithNewDynamicCity() {
        var userId = UUID.randomUUID();
        var city = City.create(Country.create("BR", "Brazil"), "Manaus");
        var player = Player.createRegistered(userId, "Original Name", "Original");
        var input = new UpdatePlayerProfileInput(userId, "Updated Nick", "mtgo_user", null, null, "BR", "Manaus");

        when(playerRepository.findByUserId(userId)).thenReturn(Optional.of(player));
        when(geoApi.getOrCreateCity("BR", "Manaus")).thenReturn(city);
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(input);

        assertEquals("Updated Nick", result.getDisplayName());
        assertEquals(city, result.getCity());
    }
}
