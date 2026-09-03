package com.duelrecord.app.player.core.event;

import com.duelrecord.app.identity.UserAuthenticatedEvent;
import com.duelrecord.app.player.core.usecase.EnsurePlayerForUserInput;
import com.duelrecord.app.player.core.usecase.EnsurePlayerForUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAuthenticatedEventListener {

    private final EnsurePlayerForUserUseCase ensurePlayerForUserUseCase;

    @EventListener
    public void onUserAuthenticated(UserAuthenticatedEvent event) {
        if (event == null || event.userId() == null) {
            return;
        }
        ensurePlayerForUserUseCase.execute(new EnsurePlayerForUserInput(
                event.userId(),
                event.email(),
                event.name()
        ));
    }
}
