package ru.fozeton.chatmanager.config.defaults;

public interface Default<T> {
        T createDefault();
        boolean isEmpty(T config);
}
