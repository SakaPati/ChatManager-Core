package ru.fozeton.chatmanager.utils.compat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import ru.fozeton.chatmanager.utils.compat.api.IPacketCompat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PacketCompat1_21_4 implements IPacketCompat {
    @Getter
    private static final PacketCompat1_21_4 instance = new PacketCompat1_21_4();

    @Override
    public <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> packet, T listener) {
        Minecraft mc = Minecraft.getInstance();
        PacketUtils.ensureRunningOnSameThread(packet, listener, mc);
    }
}
