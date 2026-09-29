package ru.fozeton.chatmanager.utils.compat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import ru.fozeton.chatmanager.utils.compat.api.IComponentSerializer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ComponentSerializer1_21_4 implements IComponentSerializer {
    @Getter
    private static final ComponentSerializer1_21_4 instance = new ComponentSerializer1_21_4();
    private final ClientLevel level = Minecraft.getInstance().level;

    @Override
    public String toJson(Component component) {
        if (level == null) throw new RuntimeException("ClientLevel not initialized");
        return Component.Serializer.toJson(component, level.registryAccess());
    }
}
