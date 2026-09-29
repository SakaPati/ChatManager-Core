package ru.fozeton.chatmanager.mixin;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.fozeton.chatmanager.utils.compat.handlers.AddedMessageHandler;

@Mixin(ChatComponent.class)
public class AddedMessageMixin26_2 {
    @Inject(method = "addMessage", at = @At(value = "HEAD"))
    public void addedMessage(
            Component contents,
            MessageSignature signature,
            GuiMessageSource source,
            GuiMessageTag tag,
            CallbackInfo ci
    ) {
        AddedMessageHandler.handleMessage(contents);
    }
}
