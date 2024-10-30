package kr.toxicity.command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

public sealed interface ReloadState {

    OnReload ON_RELOAD = new OnReload();

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class OnReload implements ReloadState {}

    record Success(long time) implements ReloadState {}
    record Failure(@NotNull Exception exception) implements ReloadState {}
}
