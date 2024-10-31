package kr.toxicity.command;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

public interface MessageSender {
    void send(@NotNull SendLevel level, @NotNull BetterCommandSource source, @NotNull Map<String, Component> value);

    default void send(@NotNull BetterCommandSource source, @NotNull Map<String, Component> value) {
        send(SendLevel.INFO, source, value);
    }
    default void send(@NotNull BetterCommandSource source) {
        send(source, Collections.emptyMap());
    }

    enum SendLevel {
        INFO,
        WARN,
        ERROR
    }
}
