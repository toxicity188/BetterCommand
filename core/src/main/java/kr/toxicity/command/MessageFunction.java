package kr.toxicity.command;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@RequiredArgsConstructor
public final class MessageFunction<W extends BetterCommandSource> {
    private final CommandMessage defaultMessage;
    private final List<ConditionalMessage> conditionalMessages = new ArrayList<>();

    public @NotNull CommandMessage defaultMessage() {
        return defaultMessage;
    }

    public @NotNull MessageFunction<W> conditional(@NotNull Predicate<W> predicate, @NotNull CommandMessage message) {
        conditionalMessages.add(new ConditionalMessage(predicate, message));
        return this;
    }

    public @NotNull CommandMessage find(@NotNull W w) {
        for (ConditionalMessage conditionalMessage : conditionalMessages) {
            if (conditionalMessage.predicate.test(w)) return conditionalMessage.message;
        }
        return defaultMessage;
    }

    @RequiredArgsConstructor
    private final class ConditionalMessage {
        private final Predicate<W> predicate;
        private final CommandMessage message;
    }
}
