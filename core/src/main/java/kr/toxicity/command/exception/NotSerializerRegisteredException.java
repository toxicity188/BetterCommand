package kr.toxicity.command.exception;

public class NotSerializerRegisteredException extends RuntimeException {
    public NotSerializerRegisteredException(String message) {
        super(message);
    }
}
