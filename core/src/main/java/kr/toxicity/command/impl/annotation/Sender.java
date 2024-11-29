package kr.toxicity.command.impl.annotation;

import kr.toxicity.command.SenderType;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This command can be executed by some type of command source
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Sender {
    /**
     * Gets all applicable type
     * @return all type
     */
    @NotNull SenderType[] type();
}
