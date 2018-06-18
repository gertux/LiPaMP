package be.hobbiton.maven.lipamp.plugin;

/**
 * @author <a href="mailto:gert@hobbiton.be">Gert Dewit</a>
 */
public class DebianMojoException extends RuntimeException {
    public DebianMojoException(String message, Throwable cause) {
        super(message, cause);
    }
}
