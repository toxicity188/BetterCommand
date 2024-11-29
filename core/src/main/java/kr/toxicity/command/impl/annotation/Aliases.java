package kr.toxicity.command.impl.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Aliases of that command
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Aliases {
    /**
     * Gets aliases
     * @return aliases
     */
    @NotNull String[] aliases() default {};
}
