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
        ComponentSerializerProvider.setInstance(ComponentSerializer1_21_4.getInstance());
        HoverEventProvider.setInstance(HoverEvent1_21_4.getInstance());
        ClickEventProvider.setInstance(ClickEvent1_21_4.getInstance());
        PacketCompatProvider.setInstance(PacketCompat1_21_4.getInstance());
        ScreenProvider.setInstance(Screen1_21_4.getInstance());
    }
}