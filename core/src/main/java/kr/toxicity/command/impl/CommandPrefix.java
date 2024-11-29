package kr.toxicity.command.impl;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Command prefix
 * @param info info message
 * @param warn warn message
 * @param error error message
 */
public record CommandPrefix(
        @NotNull CommandMessage info,
        @NotNull CommandMessage warn,
        @NotNull CommandMessage error
) {

    /**
     * Initializes command prefix
     * @param info info message
     * @param warn warn message
     * @param error error message
     */
    public CommandPrefix {
        Objects.requireNonNull(info, "info");
        Objects.requireNonNull(warn, "warn");
        Objects.requireNonNull(error, "error");
    }

    /**
     * Default command prefix
     */
    public static final CommandPrefix DEFAULT = new CommandPrefix(
            new CommandMessage(
                    "internal.info",
                    Component.text()
                            .content(" [!] ")
                            .color(NamedTextColor.GREEN)
                            .decorate(TextDecoration.BOLD)
                            .build()
            ),
            new CommandMessage(
                    "internal.warn",
                    Component.text()
                            .content(" [!] ")
                            .color(NamedTextColor.YELLOW)
                            .decorate(TextDecoration.BOLD)
                            .build()
            ),
            new CommandMessage(
                    "internal.error",
                    Component.text()
                            .content(" [!] ")
                            .color(NamedTextColor.RED)
                            .decorate(TextDecoration.BOLD)
                            .build()
            )
    );
}
