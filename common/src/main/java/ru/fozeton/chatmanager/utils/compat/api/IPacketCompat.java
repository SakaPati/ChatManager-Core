package ru.fozeton.chatmanager.utils.compat.api;

import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;

public interface IPacketCompat {
    <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> packet, T listener);
}