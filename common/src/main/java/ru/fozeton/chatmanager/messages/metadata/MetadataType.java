package ru.fozeton.chatmanager.messages.metadata;

import ru.fozeton.chatmanager.exceptions.MetadataException;import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public interface MetadataType {
    default MetadataType merge(MetadataType mdt) {
        Class<?> type = this.getClass();
        if (mdt == null) return this;
        if (mdt.getClass() != this.getClass()) return mdt;

        try {
            Field[] fields = Arrays.stream(type.getDeclaredFields())
                    .filter(f -> !Modifier.isStatic(f.getModifiers()))
                    .toArray(Field[]::new);
            int length = fields.length;
            Class<?>[] types = new Class[length];
            Object[] values = new Object[length];

            for (int i = 0; i < length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                Object newValue = field.get(mdt);
                values[i] = newValue != null ? newValue : field.get(this);
                types[i] = field.getType();
            }

            Constructor<?> constructor = type.getDeclaredConstructor(types);
            constructor.setAccessible(true);
            return (MetadataType) constructor.newInstance(values);
        } catch (Exception e) {
            throw new MetadataException(e);
        }
    }
}
