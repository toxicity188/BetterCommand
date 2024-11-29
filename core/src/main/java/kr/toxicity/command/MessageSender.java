package kr.toxicity.command;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

/**
 * A sender of command message
 */
public interface MessageSender {
    /**
     * Sends message to source
     * @param level logger level
     * @param source target source
     * @param value parameters
     */
    void send(@NotNull SendLevel level, @NotNull BetterCommandSource source, @NotNull Map<String, Component> value);

    /**
     * Sends message to source
     * @param source target source
     * @param value parameters
     */
    default void send(@NotNull BetterCommandSource source, @NotNull Map<String, Component> value) {
        send(SendLevel.INFO, source, value);
    }

    /**
     * Sends message to source
     * @param source target source
     */
    default void send(@NotNull BetterCommandSource source) {
        send(source, Collections.emptyMap());
    }

    /**
     * Logger level
     */
    enum SendLevel {
        /**
         * Info level
         */
        INFO,
        /**
         * Warn level
         */
        WARN,
        /**
         * Error level
         */
        ERROR
    }
}
