package kr.toxicity.command;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class ClassSerializer<T> {

    public static final CommandMessage DEFAULT = new CommandMessage("internal.null.object", Component.text("null."));

    private final CommandMessage required;
    private final CommandMessage optional;

    private ClassSerializer() {
        required = new CommandMessage("internal.type." + name() + ".required", Component.text("(" + name() + ")").color(NamedTextColor.RED));
        optional = new CommandMessage("internal.type." + name() + ".optional", Component.text("[" + name() + "]").color(NamedTextColor.DARK_AQUA));
    }

    public CommandMessage optional() {
        return optional;
    }

    public CommandMessage required() {
        return required;
    }

    public abstract @NotNull String name();
    public abstract @NotNull List<String> suggests(@NotNull BetterCommandSource source);
    public abstract @Nullable T deserialize(@NotNull BetterCommandSource source, @NotNull String raw);
    public @NotNull CommandMessage nullMessage() {
        return DEFAULT;
    }

    public static <R> @NotNull Builder<R> builder(@NotNull BiFunction<BetterCommandSource, String, R> function) {
        return new Builder<>(function);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder<R> {
        private final BiFunction<BetterCommandSource, String, R> function;

        private String name;
        private Function<BetterCommandSource, List<String>> suggests;
        private CommandMessage nullMessage;

        public @NotNull Builder<R> name(@NotNull String name) {
            this.name = Objects.requireNonNull(name, "name");
            return this;
        }

        public @NotNull Builder<R> suggests(@NotNull Function<BetterCommandSource, List<String>> suggests) {
            this.suggests = Objects.requireNonNull(suggests, "suggests");
            return this;
        }

        public @NotNull Builder<R> nullMessage(@Nullable CommandMessage nullMessage) {
            this.nullMessage = nullMessage;
            return this;
        }

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
