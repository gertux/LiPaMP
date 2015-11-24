package be.hobbiton.maven.lipamp.common;

public class ArchiveException extends RuntimeException {
    private static final long serialVersionUID = -3258724193440034859L;

    public ArchiveException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArchiveException(String message) {
        super(message);
    }
}
