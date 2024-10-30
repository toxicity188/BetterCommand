package kr.toxicity.command.exception;

public class NotJsonObjectException extends RuntimeException {
    public NotJsonObjectException(String message) {
        super(message);
    }
}
