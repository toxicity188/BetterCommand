package kr.toxicity.command.impl.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description of command
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Description {
    /**
     * Gets key
     * @return key
     */
    @NotNull String key();

    /**
     * Gets default message
     * @return default message
     */
    @NotNull String defaultValue() default "Unknown description.";
}
