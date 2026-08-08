package ru.fozeton.chatmanager.events.game;

import com.ferra13671.megaevents.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;

@Getter
@RequiredArgsConstructor
public class SendPosEvent extends Event<SendPosEvent> {
    private final LocalPlayer player;
    private final Level world;
    private final double x, y, z;
}
