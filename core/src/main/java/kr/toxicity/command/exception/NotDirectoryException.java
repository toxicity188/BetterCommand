package kr.toxicity.command.exception;

public class NotDirectoryException extends RuntimeException {
    public NotDirectoryException(String message) {
        super(message);
    }
}
