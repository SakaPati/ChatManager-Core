package ru.fozeton.chatmanager.utils.stt;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * An {@link InvocationHandler} used to route method calls from our local interfaces
 * ({@link VoskModel}, {@link VoskRecognizer}) to the actual dynamically loaded
 * {@code org.vosk.*} instances.
 *
 * @param target the actual instance of the Vosk class loaded via the custom ClassLoader
 */
public record VoskHandler(Object target) implements InvocationHandler {

    /**
     * Intercepts method calls on the proxy interface and forwards them to the target native object via reflection.
     *
     * @param proxy  the proxy instance
     * @param method the method invoked on the proxy
     * @param args   the arguments passed to the method
     * @return the result returned by the target method
     * @throws Throwable if the underlying reflection call throws an exception
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method real = target.getClass().getMethod(method.getName(), method.getParameterTypes());
        return real.invoke(target, args);
    }
}