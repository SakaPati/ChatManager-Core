package ru.fozeton.chatmanager.events.game;

import com.ferra13671.megaevents.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.player.LocalPlayer;

@Getter
@RequiredArgsConstructor
public class PlayerDeathEvent extends Event<PlayerDeathEvent> {
    private final LocalPlayer player;
    private final double x, y, z;
}
