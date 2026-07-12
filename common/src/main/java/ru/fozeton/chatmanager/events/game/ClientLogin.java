package ru.fozeton.chatmanager.events.game;

import com.ferra13671.megaevents.event.Event;
import com.mojang.authlib.GameProfile;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ClientLogin extends Event<ClientLogin> {
    private final GameProfile gameProfile;
}
