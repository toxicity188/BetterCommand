package kr.toxicity.command.impl.exception;

/**
 * Given file is not directory
 */
public class NotDirectoryException extends RuntimeException {
    /**
     * Given file is not directory
     * @param message message
     */
    public NotDirectoryException(String message) {
        super(message);
    }
}
