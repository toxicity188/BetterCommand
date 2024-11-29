package kr.toxicity.command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * A state of reload
 */
public sealed interface ReloadState {

    /**
     * Singleton instance of OnReload
     */
    OnReload ON_RELOAD = new OnReload();

    /**
     * Still on reload
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class OnReload implements ReloadState {}

    /**
     * Reload success
     * @param time reload time
     */
    record Success(long time) implements ReloadState {}

    /**
     * Reload failure
     * @param exception reason
     */
    record Failure(@NotNull Exception exception) implements ReloadState {}
}
