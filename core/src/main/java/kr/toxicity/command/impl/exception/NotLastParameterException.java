package kr.toxicity.command.impl.exception;

/**
 * These annotations can't be exists if this parameter is not last one.
 * @see kr.toxicity.command.impl.annotation.Vararg
 * @see kr.toxicity.command.impl.annotation.CanBeNull
 */
public class NotLastParameterException extends RuntimeException {
    /**
     * These annotations can't be exists if this parameter is not last one.
     * @param message message
     */
    public NotLastParameterException(String message) {
        super(message);
    }
}
