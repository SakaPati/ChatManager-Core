package ru.fozeton.chatmanager;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class ChatManagerCoreFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        VersionInit.init(FabricLoader.getInstance().getConfigDir());
    }
}