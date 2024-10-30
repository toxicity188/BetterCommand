package kr.toxicity.command.test;

import kr.toxicity.command.BetterCommandSource;
import kr.toxicity.command.SenderType;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class CommandConsole implements BetterCommandSource {
    @Override
    public @NotNull Audience audience() {
        return Bukkit.getServer().getConsoleSender();
    }

    @Override
    public @NotNull Locale locale() {
        return Locale.US;
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return true;
    }

    @Override
    public @NotNull SenderType type() {
        return SenderType.CONSOLE;
    }
}
