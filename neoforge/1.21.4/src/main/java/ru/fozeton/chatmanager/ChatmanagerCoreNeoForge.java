package ru.fozeton.chatmanager.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import ru.fozeton.chatmanager.ChatManagerCore;

@Mod(value = ChatManagerCore.MOD_ID, dist = Dist.CLIENT)
public final class ChatmanagerCoreNeoForge {
    public ChatmanagerCoreNeoForge() {
        ChatManagerCore.init(FMLPaths.CONFIGDIR.get());
    }
}