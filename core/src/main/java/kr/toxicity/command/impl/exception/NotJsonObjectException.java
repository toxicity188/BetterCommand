package kr.toxicity.command.impl.exception;

/**
 * Given json is not json object
 */
public class NotJsonObjectException extends RuntimeException {

    /**
     * Given json is not json object
     * @param message message
     */
    public NotJsonObjectException(String message) {
        super(message);
    }
}
