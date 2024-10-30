package kr.toxicity.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record CommandPrefix(
        @NotNull CommandMessage info,
        @NotNull CommandMessage warn,
        @NotNull CommandMessage error
) {
    public CommandPrefix {
        Objects.requireNonNull(info, "info");
        Objects.requireNonNull(warn, "warn");
        Objects.requireNonNull(error, "error");
    }

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
