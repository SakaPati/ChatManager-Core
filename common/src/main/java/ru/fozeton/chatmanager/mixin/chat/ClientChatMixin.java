package ru.fozeton.chatmanager.mixin.chat;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.config.AliasConfig;
import ru.fozeton.chatmanager.config.ChatConfigManager;
import ru.fozeton.chatmanager.events.MessageReceivedEvent;
import ru.fozeton.chatmanager.messages.Message;

@Mixin(ClientPacketListener.class)
public class ClientChatMixin {
    @Unique
    private final AliasConfig chatmanager_core$aliasConfig = ChatConfigManager.getInstance().load(AliasConfig.class);

    @Inject(method = "handlePlayerChat", at = @At(value = "HEAD"), cancellable = true)
    public void playerChatHandler(ClientboundPlayerChatPacket clientboundPlayerChatPacket, CallbackInfo ci) {
        Message msg = ChatManagerCore.getMessageParser().parsePlayerChat(clientboundPlayerChatPacket);
        ChatManagerCore.EVENT_BUS.activate(new MessageReceivedEvent(msg));
        ci.cancel();
    }

    @Inject(method = "handleSystemChat", at = @At(value = "HEAD"), cancellable = true)
    public void systemChatHandler(ClientboundSystemChatPacket clientboundSystemChatPacket, CallbackInfo ci) {
        Message msg = ChatManagerCore.getMessageParser().parseSystemChat(clientboundSystemChatPacket);
        ChatManagerCore.EVENT_BUS.activate(new MessageReceivedEvent(msg));
        ci.cancel();
    }

    @ModifyVariable(method = "sendChat", at = @At("HEAD"), argsOnly = true)
    public String onSendChatModify(String string) {
        if (chatmanager_core$aliasConfig.isEnabled()) {
            for (AliasConfig.Alias alias : chatmanager_core$aliasConfig.getAliases()) {
                string = string.replace(alias.getKey(), alias.getValue());
            }
        }
        return string;
    }

    @ModifyVariable(method = "sendCommand", at = @At("HEAD"), argsOnly = true)
    public String onSendCommandModify(String string) {
        if (chatmanager_core$aliasConfig.isEnabled() && chatmanager_core$aliasConfig.isApplyToCommands()) {
            for (AliasConfig.Alias alias : chatmanager_core$aliasConfig.getAliases()) {
                string = string.replace(alias.getKey(), alias.getValue());
            }
        }
        return string;
    }
}