package ru.fozeton.chatmanager.utils;

import com.ferra13671.megaevents.eventbus.EventSubscriber;
import lombok.Getter;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.events.ClientTickEvent;
import ru.fozeton.chatmanager.events.game.SecondElapsedEvent;

public class TickCounter {
    @Getter
    private static final TickCounter instance = new TickCounter();
    @Getter
    private int tick;

    public TickCounter() {
        ChatManagerCore.EVENT_BUS.register(this);
    }

    @EventSubscriber(event = ClientTickEvent.class)
    public void tickCounter() {
        if (tick != 20) tick++;
        else {
            tick = 0;
            ChatManagerCore.EVENT_BUS.activate(new SecondElapsedEvent());
        }
    }
}
