package ru.fozeton.chatmanager.messages.metadata;

import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@EqualsAndHashCode
public class Metadata {
    private final Map<Class<? extends MetadataType>, MetadataType> metadataSet = new HashMap<>();

    public void push(@NotNull MetadataType metadata) {
        metadataSet.merge(metadata.getClass(), metadata, MetadataType::merge);
    }

    public <T extends MetadataType> Optional<T> get(Class<T> type) {
        return Optional.ofNullable(type.cast(metadataSet.get(type)));
    }

    public boolean exists(Class<? extends MetadataType> type) {
        return metadataSet.containsKey(type);
    }
}