package kr.toxicity.command;

import kr.toxicity.command.exception.KeyAlreadyExistException;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

public record CommandMessage(@NotNull String key, @NotNull Component defaultMessage) implements Comparable<CommandMessage> {

    private static final Set<CommandMessage> ALL_MESSAGES = new TreeSet<>();

    public CommandMessage(@NotNull String key, @NotNull Component defaultMessage) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(defaultMessage, "defaultMessage");

        this.key = key;
        this.defaultMessage = defaultMessage;

        if (!ALL_MESSAGES.add(this)) throw new KeyAlreadyExistException("This key is already generated: " + key);
    }

    @NotNull
    @Unmodifiable
    public static Set<CommandMessage> allMessages() {
        return Collections.unmodifiableSet(ALL_MESSAGES);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandMessage that = (CommandMessage) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int compareTo(@NotNull CommandMessage o) {
        return key.compareTo(o.key);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key);
    }
}
