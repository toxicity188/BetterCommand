package kr.toxicity.command.impl.exception;

/**
 * Message key already exists
 */
public class KeyAlreadyExistException extends RuntimeException {
    /**
     * Message key already exists
     * @param message message
     */
    public KeyAlreadyExistException(String message) {
        super(message);
    }
}
