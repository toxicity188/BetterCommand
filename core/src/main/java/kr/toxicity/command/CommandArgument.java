package kr.toxicity.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public interface CommandArgument<W extends BetterCommandSource> {
    @NotNull String name();
    @NotNull MessageFunction<W> description();
    @NotNull <S> List<LiteralArgumentBuilder<S>> build(@NotNull Function<S, W> mapper);
    @NotNull String[] aliases();
    @Nullable Component usage(@NotNull W w);
    @Nullable String permission();
}
