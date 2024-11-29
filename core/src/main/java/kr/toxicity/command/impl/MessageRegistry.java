package kr.toxicity.command.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import kr.toxicity.command.BetterCommandSource;
import kr.toxicity.command.impl.exception.NotJsonPrimitiveException;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * A registry of component
 * @see Component
 */
@RequiredArgsConstructor
final class MessageRegistry {
    private final Map<Locale, Map<String, Component>> localeMap = new HashMap<>();
    private final ComponentSerializer<Component, Component, String> serializer;

    /**
     * Register lang file
     * @param locale target locale
     * @param object json object
     */
    void register(@NotNull Locale locale, @NotNull JsonObject object) {
        Objects.requireNonNull(locale, "locale");
        Objects.requireNonNull(object, "object");
        localeMap.computeIfAbsent(locale, l -> {
            var map = new HashMap<String, Component>();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                var value = entry.getValue();
                if (!value.isJsonPrimitive()) throw new NotJsonPrimitiveException("The key " + entry.getKey() + " in " + locale + " is not a json primitive.");
                map.put(entry.getKey(), serializer.deserialize(value.getAsString()));
            }
            return map;
        });
    }

    /**
     * Clears all lang
     */
    void clear() {
        localeMap.clear();
    }

    /**
     * Checks whether to contain
     * @param locale target location
     * @return whether to contain
     */
    boolean contains(@NotNull Locale locale) {
        return localeMap.containsKey(locale);
    }

    /**
     * Finds component by source and message
     * @param source target source
     * @param message target message
     * @return component
     */
    @NotNull Component find(@NotNull BetterCommandSource source, @NotNull CommandMessage message) {
        var map = localeMap.get(source.locale());
        if (map == null) return message.defaultMessage();
        var component = map.get(message.key());
        if (component == null) return message.defaultMessage();
        return component;
    }
}
