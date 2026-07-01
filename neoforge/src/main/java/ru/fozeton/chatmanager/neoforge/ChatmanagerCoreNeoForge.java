package ru.fozeton.chatmanager.neoforge;

import ru.fozeton.chatmanager.ChatManagerCore;
import net.neoforged.fml.common.Mod;

@Mod(ChatManagerCore.MOD_ID)
public final class ChatmanagerCoreNeoForge {
    public ChatmanagerCoreNeoForge() {
        // Run our common setup.
        ChatManagerCore.init();
    }
}
