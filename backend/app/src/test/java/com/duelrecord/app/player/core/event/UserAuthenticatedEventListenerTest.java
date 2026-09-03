package com.duelrecord.app.player.core.event;

import com.duelrecord.app.identity.UserAuthenticatedEvent;
import com.duelrecord.app.player.core.usecase.EnsurePlayerForUserInput;
import com.duelrecord.app.player.core.usecase.EnsurePlayerForUserUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserAuthenticatedEventListenerTest {

    @Mock
    private EnsurePlayerForUserUseCase ensurePlayerForUserUseCase;

    @InjectMocks
    private UserAuthenticatedEventListener listener;

    @Test
    void shouldIgnoreNullEventOrNullUserId() {
        listener.onUserAuthenticated(null);
        listener.onUserAuthenticated(new UserAuthenticatedEvent(null, "email@test.com", "Name"));

        verify(ensurePlayerForUserUseCase, never()).execute(any());
    }

    @Test
    void shouldTriggerEnsurePlayerForUserOnEvent() {
        var userId = UUID.randomUUID();
        var event = new UserAuthenticatedEvent(userId, "user@duelrecord.com", "Test User");

        listener.onUserAuthenticated(event);

        ArgumentCaptor<EnsurePlayerForUserInput> captor = ArgumentCaptor.forClass(EnsurePlayerForUserInput.class);
        verify(ensurePlayerForUserUseCase).execute(captor.capture());
        assertEquals(userId, captor.getValue().userId());
        assertEquals("user@duelrecord.com", captor.getValue().email());
        assertEquals("Test User", captor.getValue().name());
    }
}
