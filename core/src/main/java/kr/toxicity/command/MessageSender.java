package kr.toxicity.command;

import org.jetbrains.annotations.NotNull;

public interface MessageSender {
    void send(@NotNull SendLevel level, @NotNull BetterCommandSource source);

    default void send(@NotNull BetterCommandSource source) {
        send(SendLevel.INFO, source);
    }

    enum SendLevel {
        INFO,
        WARN,
        ERROR
    }
}
