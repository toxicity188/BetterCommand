package kr.toxicity.command.impl;

import kr.toxicity.command.BetterCommandSource;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Gets a message by command source
 * @param <W> wrapper class of command source
 */
@RequiredArgsConstructor
public final class MessageFunction<W extends BetterCommandSource> {
    private final CommandMessage defaultMessage;
    private final List<ConditionalMessage> conditionalMessages = new ArrayList<>();

    /**
     * Gets default message
     * @return default message
     */
    public @NotNull CommandMessage defaultMessage() {
        return defaultMessage;
    }

    /**
     * Adds conditional message
     * @param predicate condition to match
     * @param message target message
     * @return self
     */
    public @NotNull MessageFunction<W> conditional(@NotNull Predicate<W> predicate, @NotNull CommandMessage message) {
        conditionalMessages.add(new ConditionalMessage(predicate, message));
        return this;
    }

    /**
     * Finds proper message by command source
     * @param w source
     * @return message
     */
    public @NotNull CommandMessage find(@NotNull W w) {
        for (ConditionalMessage conditionalMessage : conditionalMessages) {
            if (conditionalMessage.predicate.test(w)) return conditionalMessage.message;
        }
        return defaultMessage;
    }

    /**
     * Data class of conditional message
     */
    @RequiredArgsConstructor
    private final class ConditionalMessage {
        private final Predicate<W> predicate;
        private final CommandMessage message;
    }
}
