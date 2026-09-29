package ru.fozeton.chatmanager;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.fozeton.chatmanager.utils.compat.*;
import ru.fozeton.chatmanager.utils.compat.providers.*;

import java.nio.file.Path;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VersionInit {
    public static void init(Path configDir) {
        ChatManagerCore.init(configDir);
        ComponentSerializerProvider.setInstance(ComponentSerializer26_2.getInstance());
        HoverEventProvider.setInstance(HoverEvent26_2.getInstance());
        ClickEventProvider.setInstance(ClickEvent26_2.getInstance());
        PacketCompatProvider.setInstance(PacketCompat26_2.getInstance());
        ScreenProvider.setInstance(Screen26_2.getInstance());
    }
}