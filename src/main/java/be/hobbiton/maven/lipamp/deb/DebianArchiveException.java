package be.hobbiton.maven.lipamp.deb;

public class DebianArchiveException extends RuntimeException {
    private static final long serialVersionUID = -8423589795786510042L;

    public DebianArchiveException(String message, Throwable cause) {
        super(message, cause);
    }

    public DebianArchiveException(String message) {
        super(message);
    }

}
