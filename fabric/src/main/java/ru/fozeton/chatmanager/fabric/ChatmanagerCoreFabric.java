package ru.fozeton.chatmanager.fabric;

import net.fabricmc.api.ClientModInitializer;
import ru.fozeton.chatmanager.ChatManagerCore;

public final class ChatmanagerCoreFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ChatManagerCore.init();
    }
}
