package ru.fozeton.chatmanager.utils.compat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import ru.fozeton.chatmanager.utils.compat.api.IHoverEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HoverEvent1_21_4 implements IHoverEvent {
    @Getter
    private static final HoverEvent1_21_4 instance = new HoverEvent1_21_4();

    @Override
    public HoverEvent createShowText(Component text) {
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, text);
    }
}
