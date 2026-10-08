package ru.fozeton.chatmanager.utils.compat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import ru.fozeton.chatmanager.exceptions.NotInitializedException;
import ru.fozeton.chatmanager.utils.compat.api.IComponentSerializer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ComponentSerializer1_21_4 implements IComponentSerializer {
    @Getter
    private static final ComponentSerializer1_21_4 instance = new ComponentSerializer1_21_4();

    @Override
    public String toJson(Component component) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) throw new NotInitializedException("ClientLevel not initialized");
        return Component.Serializer.toJson(component, level.registryAccess());
    }
}
