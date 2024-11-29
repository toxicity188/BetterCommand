package kr.toxicity.command.impl.exception;

/**
 * No serializer found matching this parameter type
 */
public class NotSerializerRegisteredException extends RuntimeException {
    /**
     * No serializer found matching this parameter type
     * @param message message
     */
    public NotSerializerRegisteredException(String message) {
        super(message);
    }
}
