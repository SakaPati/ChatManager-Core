package ru.fozeton.chatmanager.utils.compat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import ru.fozeton.chatmanager.utils.compat.api.ILocalPlayer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LocalPlayer1_21_4 implements ILocalPlayer {
    @Getter
    private static final LocalPlayer1_21_4 instance = new LocalPlayer1_21_4();

    @Override
    public void displayClientMessage(Component component, boolean overlay) {
        LocalPlayer player = ILocalPlayer.player();
        if (player != null) player.displayClientMessage(component, overlay);
    }
}
