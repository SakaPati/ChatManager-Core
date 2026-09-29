package ru.fozeton.chatmanager.utils.compat.providers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import ru.fozeton.chatmanager.utils.compat.api.IPacketCompat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PacketCompatProvider {
    @Setter
    private static IPacketCompat instance;

    public static <T extends PacketListener> void ensureRunningOnSameThread(
            Packet<T> packet,
            T listener
    ) {
        if (instance == null) {
            throw new IllegalStateException(
                    """
                            IPacketCompat has not been initialized!
                            Please call PacketCompatProvider.setInstance(...) during client initialization.
                            """.stripIndent());
        }

        instance.ensureRunningOnSameThread(packet, listener);
    }
}
