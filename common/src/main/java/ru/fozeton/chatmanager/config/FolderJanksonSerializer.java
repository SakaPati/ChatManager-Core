package ru.fozeton.chatmanager.config;

import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.ConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Jankson;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonGrammar;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonObject;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.api.SyntaxError;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.utils.Logger;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FolderJanksonSerializer<T extends IConfig> implements ConfigSerializer<T> {
    private static final Jankson JANKSON = Jankson.builder().build();
    private static final Logger LOG = new Logger(FolderJanksonSerializer.class);

    private final Class<T> type;
    private final Path file;

    public FolderJanksonSerializer(Config definition, Class<T> type) {
        this.type = type;
        this.file = ChatManagerCore.getConfigDir().resolve(definition.name() + ".json5");
    }

    @Override
    public void serialize(T config) throws SerializationException {
        try {
            Files.createDirectories(file.getParent());
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(tmp, JANKSON.toJson(config).toJson(JsonGrammar.JSON5));
            try {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new SerializationException(e);
        }
    }

    @Override
    public T deserialize() throws SerializationException {
        if (!Files.exists(file)) {
            LOG.info("Creating default config " + file.getFileName());
            return createAndSaveDefault();
        }

        try {
            JsonObject json = JANKSON.load(Files.readString(file));
            T config = JANKSON.fromJson(json, type);
            if (config != null) return config;
            LOG.warn(file.getFileName() + " is empty, restoring defaults");
        } catch (IOException | SyntaxError | RuntimeException e) {
            LOG.error("Failed to read " + file.getFileName() + ": " + e.getMessage());
        }

        backupBroken();
        return createAndSaveDefault();
    }

    @Override
    public T createDefault() {
        try {
            T config = type.getDeclaredConstructor().newInstance();
            config.applyDefaults();
            return config;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Config " + type.getName() + " needs a public no-arg constructor", e);
        }
    }

    private T createAndSaveDefault() throws SerializationException {
        T config = createDefault();
        serialize(config);
        return config;
    }

    private void backupBroken() {
        try {
            Files.move(file, file.resolveSibling(file.getFileName() + ".broken"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOG.error("Could not back up broken config " + file.getFileName() + ": " + e.getMessage());
        }
    }
}