package ru.fozeton.chatmanager.utils.stt;

import java.lang.reflect.Proxy;

public class VoskFactory {
    private final ClassLoader loader;

    public VoskFactory(ClassLoader loader) {
        this.loader = loader;
    }

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
