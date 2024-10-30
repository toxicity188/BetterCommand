package kr.toxicity.command;

import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public interface BetterCommandSource {
    @NotNull Audience audience();
    @NotNull Locale locale();
    boolean hasPermission(@NotNull String permission);
    @NotNull SenderType type();
}
