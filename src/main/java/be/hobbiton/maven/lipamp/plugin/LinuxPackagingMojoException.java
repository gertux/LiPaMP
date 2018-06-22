package be.hobbiton.maven.lipamp.plugin;

/**
 * @author <a href="mailto:gert@hobbiton.be">Gert Dewit</a>
 */
public class LinuxPackagingMojoException extends RuntimeException {
    public LinuxPackagingMojoException(String message) {
        super(message);
    }

    public LinuxPackagingMojoException(String message, Throwable cause) {
        super(message, cause);
    }
}
