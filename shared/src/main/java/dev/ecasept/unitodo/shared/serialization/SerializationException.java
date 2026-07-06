package dev.ecasept.unitodo.shared.serialization;

/** When something goes wrong during deserialization */
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
