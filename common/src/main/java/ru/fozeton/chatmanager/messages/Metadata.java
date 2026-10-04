package ru.fozeton.chatmanager.messages;

import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@EqualsAndHashCode
public class Metadata {
    private final Map<Class<? extends Metadata>, Metadata> metadataSet = new HashMap<>();

    public void pushMetadata(Metadata metadata) {
        metadataSet.put(metadata.getClass(), metadata);
    }

    public <T extends Metadata> Optional<T> getMetadata(Class<T> type) {
        return Optional.ofNullable(type.cast(metadataSet.get(type)));
    }

    public boolean existsMetadata(Class<? extends Metadata> type) {
        return metadataSet.containsKey(type);
    }
}