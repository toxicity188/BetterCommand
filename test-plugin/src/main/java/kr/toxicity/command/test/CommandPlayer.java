package kr.toxicity.command.test;

import kr.toxicity.command.BetterCommandSource;
import kr.toxicity.command.SenderType;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@RequiredArgsConstructor
public class CommandPlayer implements BetterCommandSource {
    private final Player source;

    @Override
    public @NotNull Audience audience() {
        return source;
    }

    @Override
    public @NotNull Locale locale() {
        return source.locale();
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return source.hasPermission(permission);
    }

    @Override
    public @NotNull SenderType type() {
        return SenderType.PLAYER;
    }
}
