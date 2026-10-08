package ru.fozeton.chatmanager.utils.stt;

import ru.fozeton.chatmanager.exceptions.NotInitializedException;
import ru.fozeton.chatmanager.utils.DependencyLoader;

/**
 * Global singleton-like context holding the {@link VoskFactory} instance.
 * <p>
 * This context provides global access to the factory responsible for dynamically
 * creating Vosk proxies after the dependency is downloaded at runtime.
 */
public class VoskContext {
    private static volatile VoskFactory factory;

    /**
     * Retrieves the current VoskFactory.
     *
     * @return the active {@link VoskFactory} instance
     * @throws NotInitializedException if the factory has not been initialized yet
     */
    public static VoskFactory getFactory() {
        if (factory == null) throw new NotInitializedException("VoskFactory not available");
        return factory;
    }

    /**
     * Sets the global VoskFactory instance. Usually called once by the {@link DependencyLoader}.
     *
     * @param f the {@link VoskFactory} instance to set
     * @throws NotInitializedException if the factory is already initialized
     */
    public static void setFactory(VoskFactory f) {
        if (factory != null) throw new NotInitializedException("VoskFactory already exists");
        factory = f;
    }
}