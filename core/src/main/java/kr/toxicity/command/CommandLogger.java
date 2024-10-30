package kr.toxicity.command;

import org.jetbrains.annotations.NotNull;

public interface CommandLogger {
    void info(@NotNull String... messages);
    void warn(@NotNull String... messages);

    CommandLogger DEFAULT = new CommandLogger() {
        @Override
        public void info(@NotNull String... messages) {
            synchronized (System.out) {
                for (@NotNull String message : messages) {
                    System.out.println(message);
                }
            }
        }

        @Override
        public void warn(@NotNull String... messages) {
            synchronized (System.err) {
                for (@NotNull String message : messages) {
                    System.err.println(message);
                }
            }
        }
    };
}
