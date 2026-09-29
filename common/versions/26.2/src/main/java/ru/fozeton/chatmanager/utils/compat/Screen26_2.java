package ru.fozeton.chatmanager.utils.compat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import ru.fozeton.chatmanager.utils.compat.api.IScreen;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Screen26_2 implements IScreen {
    @Getter
    private static final Screen26_2 instance = new Screen26_2();

    @Override
    public Screen getScreen() {
        return Minecraft.getInstance().gui.screen();
    }
}
