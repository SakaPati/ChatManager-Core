package ru.fozeton.chatmanager.utils.compat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import ru.fozeton.chatmanager.utils.compat.api.IHoverEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HoverEvent26_2 implements IHoverEvent {
    @Getter
    private static final HoverEvent26_2 instance = new HoverEvent26_2();

    @Override
    public HoverEvent createShowText(Component text) {
        return new HoverEvent.ShowText(text);
    }
}
