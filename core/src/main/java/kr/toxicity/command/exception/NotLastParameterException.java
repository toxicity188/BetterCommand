package kr.toxicity.command.exception;

public class NotLastParameterException extends RuntimeException {
    public NotLastParameterException(String message) {
        super(message);
    }
}
