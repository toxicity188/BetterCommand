package kr.toxicity.command.impl;

import kr.toxicity.command.BetterCommandSource;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Class serializer
 * @param <T> type of target class
 */
public abstract class ClassSerializer<T> {

    /**
     * Default null message
     */
    public static final CommandMessage DEFAULT = new CommandMessage("internal.null.object", Component.text("null."));

    private final CommandMessage required;
    private final CommandMessage optional;

    /**
     * initializer creates serializer message.
     */
    private ClassSerializer() {
        required = new CommandMessage("internal.type." + name() + ".required", Component.text("(" + name() + ")").color(NamedTextColor.RED));
        optional = new CommandMessage("internal.type." + name() + ".optional", Component.text("[" + name() + "]").color(NamedTextColor.DARK_AQUA));
    }

    /**
     * Gets optional argument message
     * @return argument message
     */
    public CommandMessage optional() {
        return optional;
    }

    /**
     * Gets required argument message
     * @return argument message
     */
    public CommandMessage required() {
        return required;
    }

    /**
     * Gets a name of this serializer
     * @return name
     */
    public abstract @NotNull String name();

    /**
     * Suggests a proper string by its source
     * @param source command source
     * @return list of string
     */
    public abstract @NotNull @Unmodifiable List<String> suggests(@NotNull BetterCommandSource source);

    /**
     * Deserialize string to target instance
     * @param source command source
     * @param raw raw string
     * @return an instance of this class or null if unavailable
     */
    public abstract @Nullable T deserialize(@NotNull BetterCommandSource source, @NotNull String raw);

    /**
     * Gets null argument message
     * @return argument message
     */
    public @NotNull CommandMessage nullMessage() {
        return DEFAULT;
    }

    /**
     * Creates builder class of ClassSerializer
     * @param function deserializer
     * @return builder
     * @param <R> target class type
     */
    public static <R> @NotNull Builder<R> builder(@NotNull BiFunction<BetterCommandSource, String, R> function) {
        return new Builder<>(function);
    }

    /**
     * Builder class of ClassSerializer
     * @param <R> target class type
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder<R> {
        private final BiFunction<BetterCommandSource, String, R> function;

        private String name;
        private Function<BetterCommandSource, List<String>> suggests;
        private CommandMessage nullMessage;

        /**
         * Sets name
         * @param name target name
         * @return self
         */
        public @NotNull Builder<R> name(@NotNull String name) {
            this.name = Objects.requireNonNull(name, "name");
            return this;
        }

        /**
         * Sets suggestion
         * @param suggests mapper
         * @return self
         */
        public @NotNull Builder<R> suggests(@NotNull Function<BetterCommandSource, List<String>> suggests) {
            this.suggests = Objects.requireNonNull(suggests, "suggests");
            return this;
        }

        /**
         * Sets null message
         * @param nullMessage null argument message
         * @return self
         */
        public @NotNull Builder<R> nullMessage(@Nullable CommandMessage nullMessage) {
            this.nullMessage = nullMessage;
            return this;
        }

        /**
         * Build ClassSerializer
         * @return ClassSerializer
         * @throws NullPointerException if name or suggests not set.
         */
        public @NotNull ClassSerializer<R> build() {
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(suggests, "suggests");
            return new ClassSerializer<>() {
                @Override
                public @NotNull String name() {
                    return name;
                }

                @Override
                public @NotNull List<String> suggests(@NotNull BetterCommandSource source) {
                    return suggests.apply(source);
                }

                @Override
                public @Nullable R deserialize(@NotNull BetterCommandSource source, @NotNull String raw) {
                    return function.apply(source, raw);
                }

                @Override
                public @NotNull CommandMessage nullMessage() {
                    if (nullMessage != null) return nullMessage;
                    return super.nullMessage();
                }
            };
        }
    }
}
