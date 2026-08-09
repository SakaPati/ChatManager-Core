package ru.fozeton.chatmanager.utils.stt;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public record VoskHandler(Object target) implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method real = target.getClass().getMethod(method.getName(), method.getParameterTypes());
        return real.invoke(target, args);
    }
}
