package ru.fozeton.chatmanager.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.architectury.platform.Platform;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class ChatConfigManager {
    @Getter
    private static final ChatConfigManager instance = new ChatConfigManager();
    private final Path configDir = Platform.getConfigFolder().resolve("ChatManager-Core");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public <T> T load(Class<T> type) {
        String fileName = type.getSimpleName().toLowerCase().replace("config", "") + ".json";
        Path file = configDir.resolve(fileName);
        try {
            if (Files.exists(file)) return gson.fromJson(Files.readString(file), type);
            else {
                T defaults = type.getDeclaredConstructor().newInstance();
                save(defaults);
                return defaults;
            }
        } catch (IOException
                 | InvocationTargetException
                 | InstantiationException
                 | IllegalAccessException
                 | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public <T> T loadResource(Class<T> type, String modId, String path) {
        String fileName = type.getSimpleName().toLowerCase().replace("config", "") + ".json";
        try {
            Optional<Path> pathOptional = Platform.getMod(modId).findResource(path + fileName);

            if (pathOptional.isPresent()) {
                try (var reader = Files.newBufferedReader(pathOptional.get(), StandardCharsets.UTF_8)) {
                    return gson.fromJson(reader, type);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void save(Object config) {
        String fileName = config.getClass().getSimpleName().toLowerCase().replace("config", "") + ".json";
        Path file = configDir.resolve(fileName);
        try {
            Files.createDirectories(configDir);
            Files.writeString(file, gson.toJson(config));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
