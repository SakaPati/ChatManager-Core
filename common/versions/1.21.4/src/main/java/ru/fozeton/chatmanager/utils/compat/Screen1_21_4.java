package ru.fozeton.chatmanager.utils.compat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import ru.fozeton.chatmanager.utils.compat.api.IScreen;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Screen1_21_4 implements IScreen {
    @Getter
    private static final Screen1_21_4 instance = new Screen1_21_4();

    @Override
    public Screen getScreen() {
        return Minecraft.getInstance().screen;
    }
}
