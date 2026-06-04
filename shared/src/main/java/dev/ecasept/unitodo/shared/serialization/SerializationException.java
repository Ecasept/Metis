package dev.ecasept.unitodo.shared.serialization;

public class SerializationException extends Exception {
    public SerializationException(String message) {
        super(message);
    }
    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }
    public SerializationException(Throwable cause) {
        super(cause);
    }
    public SerializationException() {
        super();
    }
}
