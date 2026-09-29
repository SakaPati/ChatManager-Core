package ru.fozeton.chatmanager.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Screen.class)
public interface ScreenInvoker {

    @Invoker("defaultHandleGameClickEvent")
    static void chatmanager$handleGameClickEvent(ClickEvent event, Minecraft minecraft, Screen screen) {
        throw new AssertionError();
    }
}