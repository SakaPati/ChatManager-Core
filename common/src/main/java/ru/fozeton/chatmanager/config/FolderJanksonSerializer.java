package ru.fozeton.chatmanager.config;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.api.SyntaxError;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.ConfigSerializer;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.exceptions.ConfigException;
import ru.fozeton.chatmanager.utils.Logger;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * AutoConfig serializer that stores each config as a separate JSON5 file in the mod's config directory.
 * <p>
 * The file name is taken from the {@link Config#name()} of the config class
 * (e.g. {@code network} becomes {@code network.json5}). Writing is done through a temporary file
 * so a crash in the middle of a save cannot corrupt the existing config.
 * <p>
 * If a config file is missing, it is created with default values. If it cannot be parsed,
 * it is renamed to {@code <name>.json5.broken} and replaced with defaults.
 *
 * @param <T> the config type
 */
public class FolderJanksonSerializer<T extends IConfig> implements ConfigSerializer<T> {
    private static final Jankson JANKSON = Jankson.builder().build();
    private static final Logger LOG = new Logger(FolderJanksonSerializer.class);

    private final Class<T> type;
    private final Path file;

    /**
     * Creates a serializer for the given config class.
     *
     * @param definition the {@link Config} annotation of the config class, provides the file name
     * @param type       the config class
     */
    public FolderJanksonSerializer(Config definition, Class<T> type) {
        this.type = type;
        this.file = ChatManagerCore.getConfigDir().resolve(definition.name() + ".json5");
    }

    /**
     * Writes the config to disk as JSON5.
     * <p>
     * The content is first written to a {@code .tmp} sibling file and then moved over the real file,
     * atomically when the file system supports it.
     *
     * @param config the config to save
     * @throws SerializationException if the file cannot be written
     */
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

    /**
     * Reads the config from disk.
     * <p>
     * If the file does not exist, a default config is created and saved. If the file is empty or
     * cannot be parsed, it is backed up as {@code .broken} and replaced with a default config.
     *
     * @return the loaded or newly created config, never {@code null}
     * @throws SerializationException if the default config cannot be saved
     */
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

    /**
     * Creates a config with default values using the no-arg constructor,
     * then lets it apply its own defaults via {@link IConfig#applyDefaults()}.
     *
     * @return a new default config
     * @throws ConfigException if the config class has no accessible no-arg constructor
     */
    @Override
    public T createDefault() {
        try {
            T config = type.getDeclaredConstructor().newInstance();
            config.applyDefaults();
            return config;
        } catch (ReflectiveOperationException e) {
            throw new ConfigException("Config " + type.getName() + " needs a public no-arg constructor", e);
        }
    }

    /**
     * Creates a default config and immediately writes it to disk.
     */
    private T createAndSaveDefault() throws SerializationException {
        T config = createDefault();
        serialize(config);
        return config;
    }

    /**
     * Renames the unreadable config file to {@code .broken} so the user does not lose its content.
     */
    private void backupBroken() {
        try {
            Files.move(file, file.resolveSibling(file.getFileName() + ".broken"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOG.error("Could not back up broken config " + file.getFileName() + ": " + e.getMessage());
        }
    }
}