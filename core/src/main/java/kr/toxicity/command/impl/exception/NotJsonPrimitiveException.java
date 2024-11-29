package kr.toxicity.command.impl.exception;

/**
 * Given json is not json primitive
 */
public class NotJsonPrimitiveException extends RuntimeException {
    /**
     * Given json is not json primitive
     * @param message message
     */
    public NotJsonPrimitiveException(String message) {
        super(message);
    }
}
