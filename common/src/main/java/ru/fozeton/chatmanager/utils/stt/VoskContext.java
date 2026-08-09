package ru.fozeton.chatmanager.utils.stt;

public class VoskContext {
    private static volatile VoskFactory factory;

    public static VoskFactory getFactory() {
        if (factory == null) throw new IllegalStateException("VoskFactory not available");
        return factory;
    }

    public static void setFactory(VoskFactory f) {
        if (factory != null) throw new IllegalStateException("VoskFactory already exists");
        factory = f;
    }
}
