package kr.toxicity.command.exception;

public class KeyAlreadyExistException extends RuntimeException {
    public KeyAlreadyExistException(String message) {
        super(message);
    }
}
