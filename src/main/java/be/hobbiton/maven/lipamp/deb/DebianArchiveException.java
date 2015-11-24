package be.hobbiton.maven.lipamp.deb;

import be.hobbiton.maven.lipamp.common.ArchiveException;

public class DebianArchiveException extends ArchiveException {
    private static final long serialVersionUID = -8423589795786510042L;

    public DebianArchiveException(String message, Throwable cause) {
        super(message, cause);
    }

    public DebianArchiveException(String message) {
        super(message);
    }

}
