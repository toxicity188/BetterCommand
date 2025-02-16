package kr.toxicity.command.impl;

import kr.toxicity.command.impl.exception.KeyAlreadyExistException;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Component with translatable key
 * @param key translatable key
 * @param defaultMessage default component if lang file is not found
 */
public record CommandMessage(@NotNull String key, @NotNull Component defaultMessage) implements Comparable<CommandMessage> {

    private static final Set<CommandMessage> ALL_MESSAGES = new TreeSet<>();

    /**
     * Creates instance.
     * @param key translatable key
     * @param defaultMessage default component if lang file is not found
     */
    public CommandMessage(@NotNull String key, @NotNull Component defaultMessage) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(defaultMessage, "defaultMessage");

        this.key = key;
        this.defaultMessage = defaultMessage;

        if (!ALL_MESSAGES.add(this)) throw new KeyAlreadyExistException("This key is already generated: " + key);
    }

    /**
     * Gets all generated message
     * @return all message
     */
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
