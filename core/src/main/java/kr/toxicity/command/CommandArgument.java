package kr.toxicity.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import kr.toxicity.command.impl.MessageFunction;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

/**
 * Refers command argument
 * @param <W> source
 */
public interface CommandArgument<W extends BetterCommandSource> {
    /**
     * Gets command name
     * @return name
     */
    @NotNull String name();

    /**
     * Gets command description
     * @return description
     */
    @NotNull MessageFunction<W> description();

    /**
     * Build a brigadier command
     * @param mapper class wrapper
     * @return brigadier command
     * @param <S> source class of W (normally it is a CommandSourceStack)
     */
    @NotNull <S> List<LiteralArgumentBuilder<S>> build(@NotNull Function<S, W> mapper);

    /**
     * Gets command aliases
     * @return aliases
     */
    @NotNull String[] aliases();

    /**
     * Gets usage of this command
     * @param w source
     * @return usage component
     */
    @Nullable Component usage(@NotNull W w);

    /**
     * Gets command permission
     * @return permission key or null if not set
     */
    @Nullable String permission();
}
