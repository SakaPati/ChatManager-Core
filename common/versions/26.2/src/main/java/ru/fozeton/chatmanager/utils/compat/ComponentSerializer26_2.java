package ru.fozeton.chatmanager.utils.compat;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import ru.fozeton.chatmanager.utils.Logger;
import ru.fozeton.chatmanager.utils.compat.api.IComponentSerializer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ComponentSerializer26_2 implements IComponentSerializer {
    @Getter
    private static final ComponentSerializer26_2 instance = new ComponentSerializer26_2();
    private final Gson gson = new Gson();
    private final Logger log = new Logger(ComponentSerializer26_2.class);

    @Override
    public String toJson(Component component) {
        return gson.toJson(ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, component).getOrThrow());
    }

    @Override
    public Component fromJson(String json) {
        if (json == null || json.isBlank()) return null;
        return ComponentSerialization.CODEC
                .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
                .resultOrPartial(err -> log.warn("Invalid component JSON: " + err))
                .orElse(null);
    }
}
