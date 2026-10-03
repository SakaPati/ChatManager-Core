package ru.fozeton.chatmanager;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;

@Mod(value = ChatManagerCore.MOD_ID, dist = Dist.CLIENT)
public final class ChatmanagerCoreNeoForge {
    public ChatmanagerCoreNeoForge() {
        VersionInit.init(FMLPaths.CONFIGDIR.get());
    }
}