package ru.fozeton.chatmanager.utils.compat;

import com.mojang.authlib.GameProfile;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import ru.fozeton.chatmanager.utils.compat.api.IGameProfile;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GameProfile26_2 implements IGameProfile {
    @Getter
    private static final GameProfile26_2 instance = new GameProfile26_2();

    private GameProfile profile() {
        return Minecraft.getInstance().getGameProfile();
    }

    @Override
    public String name() {
        return profile().name();
    }

    @Override
    public UUID id() {
        return profile().id();
    }
}
