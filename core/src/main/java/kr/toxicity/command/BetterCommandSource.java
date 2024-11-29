package kr.toxicity.command;

import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * A wrapped class of platform-side command source
 */
public interface BetterCommandSource {
    /**
     * Gets a kyori audience
     * @return audience
     */
    @NotNull Audience audience();

    /**
     * Gets a locale of source
     * @return locale
     */
    @NotNull Locale locale();

    /**
     * Checks whether this source has permission
     * @param permission target permission
     * @return whether this source has permission
     */
    boolean hasPermission(@NotNull String permission);

    /**
     * Gets source type
     * @return type
     */
    @NotNull SenderType type();
}
