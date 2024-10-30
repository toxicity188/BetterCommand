package kr.toxicity.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public interface CommandArgument<S, W extends BetterCommandSource> {
    @NotNull String name();
    @NotNull Function<? super W, CommandMessage> description();
    @NotNull List<LiteralArgumentBuilder<S>> build();
    @NotNull String[] aliases();
    @Nullable Component usage(@NotNull W w);
    @Nullable String permission();
}
