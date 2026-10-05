package ru.fozeton.chatmanager.utils.compat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import ru.fozeton.chatmanager.utils.compat.api.ILocalPlayer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LocalPlayer26_2 implements ILocalPlayer {
    @Getter
    private static final LocalPlayer26_2 instance = new LocalPlayer26_2();

    @Override
    public void displayClientMessage(Component component, boolean overlay) {
        LocalPlayer player = ILocalPlayer.player();
        if (player != null) {
            if (overlay) player.sendOverlayMessage(component);
            else player.sendSystemMessage(component);
        }
    }
}
