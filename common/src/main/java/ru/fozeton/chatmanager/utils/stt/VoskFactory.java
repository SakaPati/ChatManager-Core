package ru.fozeton.chatmanager.utils.stt;

import java.lang.reflect.Proxy;

/**
 * Factory for creating dynamic proxies of Vosk native classes (Model and Recognizer).
 * <p>
 * Because the Vosk JAR is downloaded and loaded at runtime via a custom ClassLoader,
 * direct compile-time references to org.vosk.* are impossible. This factory uses
 * reflection and Java Proxies to bridge the custom ClassLoader classes into
 * standard interfaces used by the application.
 */
public class VoskFactory {
    private final ClassLoader loader;

    /**
     * Initializes the factory with a specific ClassLoader containing the Vosk library.
     *
     * @param loader the ClassLoader that loaded the Vosk classes
     */
    public VoskFactory(ClassLoader loader) {
        this.loader = loader;
    }

    /**
     * Dynamically instantiates {@code org.vosk.Model} and wraps it in a {@link VoskModel} proxy.
     *
     * @param path the filesystem path to the extracted language model
     * @return a dynamically proxied {@link VoskModel}
     * @throws Exception if the class is not found or reflection fails
     */
    public VoskModel createModel(String path) throws Exception {
        Class<?> clazz = loader.loadClass("org.vosk.Model");
        Object target = clazz.getDeclaredConstructor(String.class).newInstance(path);
        Object proxy = Proxy.newProxyInstance(
                VoskFactory.class.getClassLoader(),
                new Class<?>[]{VoskModel.class},
                new VoskHandler(target)
        );
        return (VoskModel) proxy;
    }

    /**
     * Dynamically instantiates {@code org.vosk.Recognizer} and wraps it in a {@link VoskRecognizer} proxy.
     *
     * @param voskModel  the proxied model instance created by {@link #createModel(String)}
     * @param sampleRate the audio sample rate in Hz (e.g., 16000.0f)
     * @return a dynamically proxied {@link VoskRecognizer}
     * @throws Exception if the class is not found or reflection fails
     */
    public VoskRecognizer createRecognizer(VoskModel voskModel, float sampleRate) throws Exception {
        Class<?> clazz = loader.loadClass("org.vosk.Recognizer");
        Object model = ((VoskHandler) Proxy.getInvocationHandler(voskModel)).target();
        Object target = clazz.getDeclaredConstructor(model.getClass(), float.class).newInstance(model, sampleRate);
        Object proxy = Proxy.newProxyInstance(
                VoskFactory.class.getClassLoader(),
                new Class<?>[]{VoskRecognizer.class},
                new VoskHandler(target)
        );
        return (VoskRecognizer) proxy;
    }
}