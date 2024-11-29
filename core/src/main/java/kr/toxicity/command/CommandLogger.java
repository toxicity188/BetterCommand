package kr.toxicity.command;

import org.jetbrains.annotations.NotNull;

/**
 * Command logger
 */
public interface CommandLogger {
    /**
     * Sends messages to info level
     * @param messages message
     */
    void info(@NotNull String... messages);

    /**
     * Sends messages to warn level
     * @param messages message
     */
    void warn(@NotNull String... messages);

    /**
     * Default logger
     */
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
